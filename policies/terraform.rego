package main
import rego.v1
# Validate the resolved Terraform plan JSON, not merely source HCL.
deny contains msg if {
  some rc in input.resource_changes
  rc.type == "terraform_data"
  rc.name == "mock_aws_provision"
  not rc.change.after.input.encryption_enabled
  msg := "Cloud requests must enable encryption"
}
deny contains msg if {
  some rc in input.resource_changes
  rc.type == "terraform_data"
  rc.name == "mock_aws_provision"
  rc.change.after.input.public_access == true
  msg := "Cloud requests cannot enable public access"
}
deny contains msg if {
  some rc in input.resource_changes
  rc.type == "terraform_data"
  rc.name == "mock_aws_provision"
  not rc.change.after.input.environment in {"dev", "test", "prod"}
  msg := "Environment must be dev, test, or prod"
}
