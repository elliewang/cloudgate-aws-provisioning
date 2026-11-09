package com.example.portal;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="audit_events")
public class AuditEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long requestId;
 public Instant createdAt=Instant.now();
 public String role;
 public String action;
 public String fromStatus;
 public String toStatus;
 @Column(length=2000) public String detail;
 public AuditEvent(){}
 public AuditEvent(Long requestId,String role,String action,String fromStatus,String toStatus,String detail){ this.requestId=requestId;this.role=role;this.action=action;this.fromStatus=fromStatus;this.toStatus=toStatus;this.detail=detail; }
}
