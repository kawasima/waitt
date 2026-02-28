# WAITT - Web Application Integration Test Tool

## Project Overview

Maven plugin for running Java web applications with embedded servers during development.
Provides features like code coverage (JaCoCo), response tracing, and an admin dashboard.

## Module Structure

```text
waitt-parent (pom.xml)          -- Parent POM, version management
├── waitt-api                   -- SPI interfaces (EmbeddedServer, WebappDecorator, etc.)
├── waitt-maven-plugin          -- Maven plugin (waitt:run goal)
├── waitt-embed-runner          -- Embedded runner utilities
├── waitt-tomcat9               -- Tomcat 9 server (javax.servlet)
├── waitt-tomcat10              -- Tomcat 10 server (jakarta.servlet)
├── waitt-tomcat11              -- Tomcat 11 server (jakarta.servlet)
├── waitt-jetty9                -- Jetty 9 server (javax.servlet)
├── waitt-coverage              -- Coverage base
├── waitt-jacoco                -- JaCoCo integration
├── waitt-dashboard             -- Dashboard UI (war)
├── waitt-admin                 -- Admin feature
└── waitt-tracer                -- Response tracing feature (jakarta.servlet)
```

`examples/` directory contains sample apps (sa-struts, struts2, spring-boot, nablarch) that are **not** part of the reactor build.

## Build

```bash
mvn clean install
```

Java 8 target (`maven.compiler.source=1.8`). Build requires JDK 8+ but runs on modern JDKs.

## Key Design Decisions

- Server implementations are loaded via `ServiceLoader` (`META-INF/services/net.unit8.waitt.api.EmbeddedServer`)
- Features (jacoco, tracer, admin) are loaded via `ServiceLoader` as `WebappDecorator`
- ClassLoader isolation uses Plexus `ClassRealm`
- `waitt-tracer` uses **Jakarta Servlet API** (compatible with Tomcat 10/11, NOT with Tomcat 9)
- `waitt-maven-plugin` supports **forked JVM execution** (`<forkJvm>true</forkJvm>`) with Maven Toolchains for legacy apps requiring older JDKs

## Version Management

All submodule versions are managed from the parent POM. Submodule-to-submodule dependencies use `${project.parent.version}`.

### Version UP (development)

```bash
# Set new version across all modules at once
mvn versions:set -DnewVersion=1.4.0-SNAPSHOT -DgenerateBackupPoms=false
```

This updates the parent POM version and all submodule `<parent><version>` entries automatically.

**Note:** `examples/` are outside the reactor. After `versions:set`, update `<waitt.version>` property in each example's pom.xml manually:

- `examples/sa-struts/pom.xml`
- `examples/struts2/pom.xml`
- `examples/spring-boot/pom.xml`
- `examples/nablarch/pom.xml`

### Release to Maven Central

```bash
# 1. Set release version
mvn versions:set -DnewVersion=1.3.1 -DgenerateBackupPoms=false

# 2. Commit and tag
git add -A && git commit -m "Release v1.3.1"
git tag v1.3.1

# 3. Deploy with release profile (source, javadoc, GPG signing)
mvn clean deploy -Prelease

# 4. Set next development version
mvn versions:set -DnewVersion=1.3.2-SNAPSHOT -DgenerateBackupPoms=false
git add -A && git commit -m "Prepare next development version"

# 5. Push
git push origin master --tags
```

The `release` profile activates: maven-source-plugin, maven-javadoc-plugin, maven-gpg-plugin.
Artifacts are published to Maven Central via central-publishing-maven-plugin (central.sonatype.com).

`~/.m2/settings.xml` requires a `<server id="central">` entry with a token from <https://central.sonatype.com>.
