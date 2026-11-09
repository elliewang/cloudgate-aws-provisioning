terraform {
  required_version = ">= 1.5.0"
}
variable "service" { type = string; default = "RDS" }
variable "environment" { type = string; default = "dev" }
variable "encryption_enabled" { type = bool; default = true }
variable "public_access" { type = bool; default = false }
resource "terraform_data" "mock_aws_provision" {
  input = {
    service = var.service
    environment = var.environment
    encryption_enabled = var.encryption_enabled
    public_access = var.public_access
    mode = "SIMULATED_ONLY_NO_AWS"
  }
}
output "simulated_resource" { value = terraform_data.mock_aws_provision.output }
