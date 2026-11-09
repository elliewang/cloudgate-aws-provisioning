package com.example.portal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class PortalApplicationTests {
 @LocalServerPort int port;
 @Autowired TestRestTemplate client;
 private String url(String path){return "http://localhost:"+port+path;}
 @Test void approvalSequenceAndRoleChecks(){
  var request=Map.of("title","Create encrypted RDS","requester","Taylor","businessUnit","Finance","service","RDS","environment","dev","encryptionEnabled",true,"publicAccess",false);
  var headers=new HttpHeaders();headers.set("X-Demo-Role","BUSINESS_USER");
  var created=client.exchange(url("/api/requests"),HttpMethod.POST,new HttpEntity<>(request,headers),Map.class);
  assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  var id=created.getBody().get("id");
  var wrong=client.exchange(url("/api/requests/"+id+"/advance"),HttpMethod.POST,new HttpEntity<>(Map.of("action","ADVANCE"),headers),Map.class);
  assertThat(wrong.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  headers.set("X-Demo-Role","PRODUCT_OWNER");
  var ok=client.exchange(url("/api/requests/"+id+"/advance"),HttpMethod.POST,new HttpEntity<>(Map.of("action","ADVANCE"),headers),Map.class);
  assertThat(ok.getBody().get("status")).isEqualTo("BUSINESS_APPROVED");
 }
}
