# AGENTS.md

Spring Boot 4 (parent 4.1.0) / Spring Framework 6 backend on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package `ch.dboeckli.spring.datarest`. It exposes a
Spring Data REST API (base path `/api/v4`) for Beers and Customers, plus a Thymeleaf web UI
(`/web/beers`). OAuth2 JWT resource server, H2 in-memory database, OpenAPI 3 via springdoc.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BeerControllerTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop).
- Start locally: `./mvnw spring-boot:run` (app on `:8084`).

> The Selenium IT `BeerListPageIT` requires a Chrome/WebDriver browser. It runs in CI and on a
> developer machine, but not in the headless sandbox.

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Versioning (Maven → Docker → Helm)

- ONE version drives everything: `helm.chart.version` (derived from `project.version` in phase
  `initialize`, see antrun `generate-helm-version`). Docker image main tags and the packaged Helm
  chart use it. SemVer-compliant snapshots: `0.0.1-SNAPSHOT` → `0.0.1-snapshot.<abbrev-hash>`.
- The chart is named `spring-6-data-rest-chart` (renamed from the bare artifactId). Chart
  `appVersion` and the deployed container name are `spring-6-data-rest`.
- Helm is invoked directly via `exec-maven-plugin` (Helm v4-compatible), not the kokuwa plugin.

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`.
- Spotless flexmark also formats markdown, so `README.md` edits must stay flexmark-clean; run
  `./mvnw spotless:apply` after editing markdown. `AGENTS.md` and `CLAUDE.md` are excluded.
- Shell scripts are formatted by Spotless/shfmt `3.13.1`.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- A custom `TestClassOrderer` sorts test classes (unit → IT). Do not change that ordering.
- `*IT` classes use `@SpringBootTest` (random port / H2) and run the packaged context; the Selenium
  UI IT (`BeerListPageIT`) is order-sensitive (`@TestMethodOrder`) and marks the context dirty.

## Architecture

- Spring Data REST exposes entities (`Beer`, `Customer`) via `repository/` interfaces at base path
  `/api/v4`; `@RepositoryRestResource` is used for customization.
- `BeerWebController` renders the Thymeleaf web UI (`/web/beers`); no custom REST controllers.
- Security (`SpringSecurityConfigRest`): swagger, h2-console and actuator are `permitAll`; the REST
  API requires a JWT (configured issuer). Server port is `8084`, Kubernetes NodePort `30084`.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/`, packaged to `target/helm/repo/`, release name =
  artifactId, namespace `spring-6-data-rest`. The `k8s/` directory is a legacy raw-manifest source;
  prefer Helm.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Docker/Helm registry is Docker Hub (`domboeckli`). Update tooling: `.github/renovate.json` +
  `.github/dependabot.yml`; validate Renovate config with `renovate-config-validator`.