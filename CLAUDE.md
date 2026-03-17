# WAITT - Web Application Integration Test Tool

## Project Overview

Maven plugin for running Java web applications with embedded servers during development.
Provides features like code coverage (JaCoCo), response tracing, and an admin dashboard.

## Module Structure

```text
waitt-parent (pom.xml)          -- Parent POM, version management
├── waitt-api                   -- SPI interfaces (EmbeddedServer, WebappDecorator, etc.)
├── waitt-maven-plugin          -- Maven plugin (waitt:run, waitt:run-headless, waitt:jar goals)
├── waitt-embed-runner          -- Embedded runner utilities
├── waitt-tomcat9               -- Tomcat 9.0 server (javax.servlet / Servlet 4.0)
├── waitt-tomcat10              -- Tomcat 10.1 server (jakarta.servlet / Servlet 6.0)
├── waitt-tomcat11              -- Tomcat 11.0 server (jakarta.servlet / Servlet 6.1)
├── waitt-jetty12               -- Jetty 12 server (jakarta.servlet / Servlet 6.0)
├── waitt-jacoco                -- JaCoCo coverage
├── waitt-dashboard             -- Dashboard UI (war)
├── waitt-admin                 -- Admin feature (memory/CPU monitoring, heap dump, reload)
└── waitt-tracer                -- OpenTelemetry tracing (jakarta.servlet, Tomcat 10/11, Jetty 12)
```

`examples/` directory contains sample apps that are **not** part of the reactor build:

- `examples/spring-boot` -- Spring Boot (waitt-tomcat11)
- `examples/struts2` -- Struts2 6.x (waitt-tomcat9, javax.servlet)
- `examples/nablarch` -- Nablarch 5u26 (waitt-tomcat9, javax.servlet)
- `examples/sa-struts` -- SAStruts (waitt-tomcat9, javax.servlet)

## Build

```bash
mvn clean install
```

Java 8 target (`maven.compiler.source=1.8`). Build requires JDK 8+ but runs on modern JDKs.

## Key Design Decisions

- Server implementations are loaded via `ServiceLoader` (`META-INF/services/net.unit8.waitt.api.EmbeddedServer`)
- Features (jacoco, tracer, admin) are loaded via `ServiceLoader` as `WebappDecorator`
- ClassLoader isolation uses Plexus `ClassRealm`
- `waitt-tracer` uses **Jakarta Servlet API** (compatible with Tomcat 10/11 and Jetty 12, NOT with Tomcat 9)
- `waitt-maven-plugin` supports **forked JVM execution** (`<forkJvm>true</forkJvm>`) with Maven Toolchains for legacy apps requiring older JDKs
- Forked JVM args can be passed via `<forkJvmArgs>` (e.g., `--add-opens` flags for frameworks using deep reflection on modern JDKs)

## Servlet API Compatibility

When choosing a server module, match the Servlet API namespace your framework uses:

- **`javax.servlet`**: Use `waitt-tomcat9` (Struts2 6.x, Nablarch, SAStruts, etc.)
- **`jakarta.servlet`**: Use `waitt-tomcat10`, `waitt-tomcat11`, or `waitt-jetty12` (Spring Boot 3.x, Struts2 7.x, Jakarta EE 10+ apps)

## Version Management

All submodule versions are managed from the parent POM. Submodule-to-submodule dependencies use `${project.parent.version}`.

### Version UP (development)

Use the update script to set a new version across all modules, README.md, and examples at once:

```bash
./scripts/update-version.sh 1.4.0-SNAPSHOT
```

This runs `mvn versions:set`, updates `<version>` in `README.md`, and updates `<waitt.version>` in all `examples/*/pom.xml`.

### Release to Maven Central

```bash
# 1. Set release version
./scripts/update-version.sh 1.4.0

# 2. Commit and tag
git add -A && git commit -m "Release v1.4.0"
git tag v1.4.0

# 3. Deploy with release profile (source, javadoc, GPG signing)
mvn clean deploy -Prelease

# 4. Set next development version
./scripts/update-version.sh 1.4.1-SNAPSHOT
git add -A && git commit -m "Prepare next development version"

# 5. Push
git push origin master --tags
```

The `release` profile activates: maven-source-plugin, maven-javadoc-plugin, maven-gpg-plugin.
Artifacts are published to Maven Central via central-publishing-maven-plugin (central.sonatype.com).

`~/.m2/settings.xml` requires a `<server id="central">` entry with a token from <https://central.sonatype.com>.
