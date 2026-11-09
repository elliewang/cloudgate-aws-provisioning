# CloudGate — GitHub Pages + Java/PostgreSQL Demo

**Team-facing browser demo:** deploy the static app in [`docs/`](docs/) via GitHub Pages. See **[GITHUB_PAGES_SETUP.md](GITHUB_PAGES_SETUP.md)**.

**Important:** The Pages site has browser-local CRUD only; there is no shared PostgreSQL or Spring Boot API on Pages. The Java/PostgreSQL application is retained for local Docker deployment. GitHub Actions performs real CI, Terraform and OPA checks, but is not triggered by the Pages UI. No AWS resources are created.

---

# Hosted Deployment (Team Demo)

This version adds `render.yaml`, shared-password protection for the hosted site, a PostgreSQL connection adapter for Render and a health check. See **[HOSTED_DEPLOYMENT.md](HOSTED_DEPLOYMENT.md)** for exact GitHub and Render steps. No AWS account needed.

**IMPORTANT:** This is a demonstration, not production authentication or a genuine GitHub-integrated workflow. UI role switching is simulated, and no AWS resources are created.

---

# CloudGate — Self-Service AWS Provisioning Portal (DEMO MODE)

A GitHub-ready Java Spring Boot + PostgreSQL web portal with a responsive HTML/JS frontend, approval workflow, audit history, Terraform and OPA policy-as-code gates, and GitHub Actions ephemeral deployment. **No AWS account, infrastructure, or credentials are needed.**

## Quick start

Prerequisite: Docker Engine / Docker Desktop with Docker Compose.

```bash
docker compose up --build -d
```

Open **http://localhost:8080**. Database state persists in a Docker volume. Stop with `docker compose down`; reset all data with `docker compose down -v`.

The example binds the frontend to `127.0.0.1` only. Do not expose this project to the public internet.

## Demo sequence

1. Choose **Business User** and submit an intake (RDS, S3, ECS, or VPC).
2. Choose **Product Owner** and complete business approval.
3. Choose **Tech Owner** and complete technical approval.
4. Choose **Engineering** and complete code push, build, security, infrastructure validation.
5. Choose **Release Approver** to complete deployment approval.
6. Choose **Engineering** to simulate deployment and complete verification.
7. Inspect **Audit Trail**, **Executive Overview**, and **IaC & Policy Evidence**.
8. Repeat using encryption disabled or public access enabled: the policy stage is rejected.

Every stage records a relational audit event. `DELETE` is a soft cancellation, retaining evidence.

## GitHub Actions

`.github/workflows/ci.yml` runs on PRs and pushes to `main`:

- Java 21 Maven verification, Spring Boot integration tests, Docker image build, Trivy security scan.
- Terraform `init`, `validate`, `plan`, `show -json` using **terraform_data** (built-in provider, no AWS credentials).
- Conftest / Rego verification of the secure plan, plus a negative test proving an insecure plan is rejected.
- After checks, a job using the `demo-approval` GitHub environment starts the stack in an ephemeral GitHub runner, verifies endpoints, uploads evidence, and tears the environment down.

To demonstrate a *human* approval gate, configure a `demo-approval` environment with required reviewers in repository settings **if your GitHub plan/repository visibility supports it**. Without that protection, the job is not actually human gated. Do not claim otherwise.

The GitHub-hosted runner shuts down after CI, so the workflow does **not** create a permanently hosted website. Use local Docker for the live demo.

## Run Terraform + OPA locally (optional)

Install Terraform CLI and Conftest CLI, then:

```bash
bash scripts/check-policy.sh
```

The script checks a secure Terraform plan and confirms an intentionally insecure plan fails.

## API examples

```bash
curl -s http://localhost:8080/api/dashboard
curl -s http://localhost:8080/api/requests
curl -s -H 'Content-Type: application/json' -H 'X-Demo-Role: BUSINESS_USER' \
  -d '{"title":"Provision analytics DB","requester":"Alex","businessUnit":"Data","service":"RDS","environment":"dev","justification":"Analytics","encryptionEnabled":true,"publicAccess":false}' \
  http://localhost:8080/api/requests
```

### What is real vs simulated?

| Feature | Status |
|---|---|
| Browser CRUD and Spring Boot REST API | Real |
| PostgreSQL data persistence | Real |
| Backend role-based workflow checks | Demo role header only |
| Approvals and audit history | Real demo database events |
| Maven build / tests / container scan | Real GitHub Actions steps |
| Provider-free Terraform plan | Real Terraform execution |
| Conftest / Rego policy evaluation | Real |
| AWS provisioning | **Simulated (never creates AWS resources)** |
| Portal actions triggering GitHub CI | **Not implemented; CI is triggered by Git events** |
| GitHub reviewer gate | Depends on explicitly configured environment protection |
| Deployment | Real ephemeral Docker deployment on CI runner, not cloud |

## Production hardening needed

**This is for laptop demo only, not production.** Replace the user-selectable `X-Demo-Role` header with OIDC login and server-validated RBAC, CSRF controls where relevant, strong record ownership and tenancy authorization. Add immutable/externally anchored audit logging, Flyway migrations, controlled secrets and Terraform state backends, workflow-run evidence integration, separate app and infrastructure change review, supply-chain provenance, artifact signatures, least-privilege GitHub credentials, pinned action SHAs, checksum-verified tool downloads, rate limits, backups, and observability.

Spring Boot is version 3.5.7. Java 21, PostgreSQL 16 and Terraform 1.13.3 are used for the demonstration. Review and update pinned versions as needed.
