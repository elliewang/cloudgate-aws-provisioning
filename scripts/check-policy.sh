#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
terraform -chdir=infrastructure/demo init -input=false -no-color
terraform -chdir=infrastructure/demo validate -no-color
terraform -chdir=infrastructure/demo plan -input=false -out=demo.tfplan -no-color
terraform -chdir=infrastructure/demo show -json demo.tfplan > infrastructure/demo/plan.json
conftest test infrastructure/demo/plan.json --policy policies
printf '\nTesting deliberately insecure input (expected to fail)...\n'
terraform -chdir=infrastructure/demo plan -input=false -var='encryption_enabled=false' -var='public_access=true' -out=insecure.tfplan -no-color > /dev/null
terraform -chdir=infrastructure/demo show -json insecure.tfplan > infrastructure/demo/insecure.json
if conftest test infrastructure/demo/insecure.json --policy policies; then
  echo 'ERROR: insecure plan unexpectedly passed policy'; exit 1
fi
echo 'PASS: secure plan accepted and insecure plan rejected.'
