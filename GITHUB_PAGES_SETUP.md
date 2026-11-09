# CloudGate: GitHub Pages team demo

## What team members can access

- The **GitHub Pages** site hosts a browser-only self-service provisioning portal.
- Data is stored with `localStorage` on the current browser/device. **Not shared** across teammates, not PostgreSQL, not secured by a login.
- All AWS resources and browser approval/deployment steps are **simulated**. The role selector is for training only.
- Separately, the existing GitHub Actions `ci.yml` runs Java tests, real Terraform plan and policy validation, plus an ephemeral Docker deployment on a runner (no AWS). CI results are not wired back into the browser UI.
- For actual shared relational CRUD, retain the Spring Boot/PostgreSQL stack in this repository, but deploy it to a company-approved server. GitHub Pages alone cannot host a Java API or database.

## Set up GitHub Pages

1. Create a GitHub repository and push **the contents of this folder** to its default `main` branch. Ensure the repo root contains `docs/index.html`, `.github/workflows/pages.yml`, and `pom.xml`.
2. In **Settings → Pages → Build and deployment → Source**, select **GitHub Actions**.
3. Go to **Actions → Publish CloudGate GitHub Pages Demo → Run workflow** (or push a change under `docs/`).
4. After a successful workflow, open the URL printed under the `github-pages` environment. For a project repository it is usually `https://USERNAME.github.io/REPOSITORY/`.
5. Test accessibility from the corporate network; `github.io` may be filtered even if `github.com` is allowed.

If your organization disallows GitHub Pages or Actions, a GitHub repo can show source code and pipeline configuration but will not serve the interactive site.

## Run local Java/PostgreSQL application

```bash
docker compose up --build -d
```

Open `http://localhost:8080` for the **original** server-backed application. It is separate from the `docs/` browser demo. On corporate laptops, check Docker licensing, access, and policy constraints.

## Demonstration walkthrough

1. Open the Pages URL in a browser.
2. In **Business User** role, submit an intake.
3. Select **Product Owner**, open the intake, and approve the first stage.
4. Select **Tech Owner** and approve the second stage.
5. Use **Engineering** to demonstrate code push, build, security, and IaC stages (these are UI-only state transitions).
6. Try a new intake with encryption disabled or public access enabled to demonstrate that the UI's infrastructure-validation gate blocks it.
7. Switch to **Release Approver** for the approval gate, then **Engineering** to simulate deploy/verify.
8. Open **Audit Trail** to inspect locally recorded actions.
9. Open the GitHub **Actions** tab and show a completed `Portal CI + IaC Policy Gates` run for real pipeline evidence. Do not describe it as triggered by the UI.

## Corporate/security caveats

- Keep the repository and website free of internal data, secrets, real employee names, and confidential designs.
- On standard public GitHub Pages, the site should be considered public. A private repository does not automatically mean a private Pages site. Verify your plan and organization access settings.
- Browser localStorage is editable by the user, not audit-grade. No real authorization is provided by the role selector.
- There are no AWS resources, credentials, or provisioning costs in Demo Mode.
- The `render.yaml` and `HOSTED_DEPLOYMENT.md` files are legacy optional Render hosting configuration; they are **not used** by GitHub Pages.
