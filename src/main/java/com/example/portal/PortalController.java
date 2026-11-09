package com.example.portal;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController @RequestMapping("/api")
public class PortalController {
 private final RequestRepository requests; private final AuditRepository audits;
 PortalController(RequestRepository requests,AuditRepository audits){this.requests=requests;this.audits=audits;}
 public record NewRequest(@NotBlank String title,@NotBlank String requester,@NotBlank String businessUnit,
   @NotBlank String service,String justification,@NotBlank String environment,boolean encryptionEnabled,boolean publicAccess){}
 public record Transition(String action,String comment){}
 private static final List<String> PIPELINE=List.of("INTAKE","BUSINESS_APPROVED","TECH_APPROVED","CODE_PUSHED","BUILT_TESTED","SECURITY_PASSED","INFRA_VALIDATED","RELEASE_APPROVED","DEPLOYED_DEMO","VERIFIED");
 private static final List<String> ROLES=List.of("BUSINESS_USER","PRODUCT_OWNER","TECH_OWNER","ENGINEER","RELEASE_MANAGER","AUDITOR","EXECUTIVE");
 private void require(String actual,String expected){if(!expected.equals(actual))throw new ApiException(HttpStatus.FORBIDDEN,"Required role: "+expected);}
 private ProvisionRequest get(Long id){return requests.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Request not found"));}
 private void log(ProvisionRequest r,String role,String action,String before,String detail){audits.save(new AuditEvent(r.id,role,action,before,r.status,detail));}
 @GetMapping("/roles") public List<String> roles(){return ROLES;}
 @GetMapping("/requests") public List<ProvisionRequest> all(){return requests.findAll().stream().sorted(Comparator.comparing((ProvisionRequest r)->r.id).reversed()).toList();}
 @GetMapping("/requests/{id}") public ProvisionRequest one(@PathVariable Long id){return get(id);}
 @GetMapping("/requests/{id}/audit") public List<AuditEvent> history(@PathVariable Long id){get(id);return audits.findByRequestIdOrderByCreatedAtAscIdAsc(id);}
 @GetMapping("/audit") public List<AuditEvent> audit(){return audits.findAll().stream().sorted(Comparator.comparing((AuditEvent e)->e.id).reversed()).limit(200).toList();}
 @GetMapping("/dashboard") public Map<String,Object> dashboard(){var rs=requests.findAll();var map=new LinkedHashMap<String,Object>();map.put("total",rs.size());map.put("pendingBusiness",rs.stream().filter(r->r.status.equals("INTAKE")).count());map.put("pendingTechnical",rs.stream().filter(r->r.status.equals("BUSINESS_APPROVED")).count());map.put("verified",rs.stream().filter(r->r.status.equals("VERIFIED")).count());map.put("rejected",rs.stream().filter(r->r.status.equals("REJECTED")).count());map.put("demoMode",true);return map;}
 @PostMapping("/requests") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public ProvisionRequest create(@RequestHeader(value="X-Demo-Role",defaultValue="") String role,@Valid @RequestBody NewRequest input){
  require(role,"BUSINESS_USER"); validateEnums(input);
  var r=new ProvisionRequest(); apply(r,input);r=requests.save(r);log(r,role,"CREATED",null,"New self-service cloud request");return r;
 }
 @PutMapping("/requests/{id}") @Transactional
 public ProvisionRequest edit(@PathVariable Long id,@RequestHeader(value="X-Demo-Role",defaultValue="") String role,@Valid @RequestBody NewRequest input){
  require(role,"BUSINESS_USER");validateEnums(input);var r=get(id);if(!r.status.equals("INTAKE")&&!r.status.equals("CHANGES_REQUESTED"))throw new ApiException(HttpStatus.CONFLICT,"Only intake or change-requested records can be edited");apply(r,input);r.status="INTAKE";r=requests.save(r);log(r,role,"UPDATED","INTAKE","Request details revised");return r;
 }
 @DeleteMapping("/requests/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
 public void delete(@PathVariable Long id,@RequestHeader(value="X-Demo-Role",defaultValue="") String role){
  require(role,"BUSINESS_USER");var r=get(id);if(!r.status.equals("INTAKE"))throw new ApiException(HttpStatus.CONFLICT,"Only unapproved intake can be cancelled");var before=r.status;r.status="CANCELLED";requests.save(r);log(r,role,"CANCELLED",before,"Soft delete: record retained for audit");
 }
 @PostMapping("/requests/{id}/advance") @Transactional
 public ProvisionRequest advance(@PathVariable Long id,@RequestHeader(value="X-Demo-Role",defaultValue="") String role,@RequestBody Transition input){
  var r=get(id);String action=input.action()==null?"":input.action().toUpperCase(Locale.ROOT),before=r.status;
  if(action.equals("REJECT")||action.equals("REQUEST_CHANGES")){
   String needed=switch(r.status){case "INTAKE"->"PRODUCT_OWNER";case "BUSINESS_APPROVED"->"TECH_OWNER";default->"";};
   if(needed.isEmpty())throw new ApiException(HttpStatus.CONFLICT,"No approval is pending");require(role,needed);
   r.status=action.equals("REJECT")?"REJECTED":"CHANGES_REQUESTED";
  }else if(action.equals("ADVANCE")){
   int i=PIPELINE.indexOf(r.status);if(i<0||i==PIPELINE.size()-1)throw new ApiException(HttpStatus.CONFLICT,"Cannot advance from "+r.status);
   String required=switch(i){case 0->"PRODUCT_OWNER";case 1->"TECH_OWNER";case 2,3,4,5,6,8,9->"ENGINEER";case 7->"RELEASE_MANAGER";default->"";};
   require(role,required);
   // Demo guard: no deployment unless required controls are green.
   if(i==5 && (!r.encryptionEnabled||r.publicAccess))throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,"OPA policy FAILED: encryption required and public access denied");
   r.status=PIPELINE.get(i+1);
  } else throw new ApiException(HttpStatus.BAD_REQUEST,"Use ADVANCE, REJECT, or REQUEST_CHANGES");
  r=requests.save(r);log(r,role,action,before,(input.comment()==null?"":input.comment())+(r.status.equals("DEPLOYED_DEMO")?" [SIMULATED AWS provisioning; no AWS resources created]":""));return r;
 }
 private void apply(ProvisionRequest r,NewRequest i){r.title=i.title().trim();r.requester=i.requester().trim();r.businessUnit=i.businessUnit().trim();r.service=i.service();r.justification=i.justification();r.environment=i.environment();r.encryptionEnabled=i.encryptionEnabled();r.publicAccess=i.publicAccess();}
 private void validateEnums(NewRequest i){if(!List.of("S3","RDS","ECS","VPC").contains(i.service()))throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid service");if(!List.of("dev","test","prod").contains(i.environment()))throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid environment");}
 @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,String>> apiError(ApiException e){return ResponseEntity.status(e.status).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> validation(Exception e){return ResponseEntity.badRequest().body(Map.of("error","Missing required fields"));}
 static class ApiException extends RuntimeException{final HttpStatus status; ApiException(HttpStatus status,String message){super(message);this.status=status;}}
}
