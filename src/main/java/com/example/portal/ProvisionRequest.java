package com.example.portal;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="provision_requests")
public class ProvisionRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String title;
 @Column(nullable=false) public String requester;
 @Column(nullable=false) public String businessUnit;
 @Column(nullable=false) public String service;
 @Column(length=2000) public String justification;
 @Column(nullable=false) public String environment;
 @Column(nullable=false) public String status="INTAKE";
 public boolean encryptionEnabled=true;
 public boolean publicAccess=false;
 public String ownerTeam="Platform Engineering";
 public Instant createdAt=Instant.now();
 public Instant updatedAt=Instant.now();
 @PreUpdate void updateTimestamp(){ updatedAt=Instant.now(); }
}
