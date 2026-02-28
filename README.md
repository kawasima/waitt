# WAITT

WAITT is the Web Application Integration Test Tool.

Using WAITT, you can deploy your application to any server without building war or jar file.

## Requirements

- Java 8 or later
- Maven 3.2.5 or later

## Usage

Add plugin to your pom.xml

```xml
<plugin>
  <groupId>net.unit8.waitt</groupId>
  <artifactId>waitt-maven-plugin</artifactId>
  <version>1.4.0-SNAPSHOT</version>
  <configuration>
    <servers>
      <server>
        <groupId>net.unit8.waitt.server</groupId>
        <artifactId>waitt-tomcat9</artifactId>
        <version>1.4.0-SNAPSHOT</version>
      </server>
    </servers>
  </configuration>
</plugin>
```

And execute following command to start the server.

```shell
% mvn waitt:run
```

Then web browser will be started automatically.

If you do not want to start a browser, you can use a `run-headless` goal as follows.

```shell
% mvn waitt:run-headless
```

### Build an executable jar

Waitt plugin can build an executable jar.

```shell
% mvn waitt:jar
```

It doesn't include non-resource files automatically. So, if your app refers asset files outside jar file, you can use `-d` option in runtime.

```shell
% java -jar xxx-standalone.jar -d src/main/webapp
```

### Forked JVM execution

For applications that require specific JVM arguments or a different JDK version, you can run in a forked JVM.

```xml
<configuration>
  <forkJvm>true</forkJvm>
  <forkJvmArgs>--add-opens=java.base/java.lang=ALL-UNNAMED</forkJvmArgs>
  <forkJdkVersion>21</forkJdkVersion>
  ...
</configuration>
```

Or via command line properties:

```shell
% mvn waitt:run -Dwaitt.fork=true -Dwaitt.fork.jvmArgs="--add-opens=java.base/java.lang=ALL-UNNAMED"
```

This is useful when running legacy frameworks on modern JDKs that require `--add-opens` flags, or when you need to use a specific JDK version via Maven Toolchains.

## Supported server products

Choose a server based on the Servlet API version your application uses.

| Server | Servlet API | Namespace |
|--------|-------------|-----------|
| waitt-tomcat9 | Servlet 4.0 | `javax.servlet` |
| waitt-tomcat10 | Servlet 6.0 (Jakarta EE 10) | `jakarta.servlet` |
| waitt-tomcat11 | Servlet 6.1 (Jakarta EE 11) | `jakarta.servlet` |
| waitt-jetty12 | Servlet 6.0 (Jakarta EE 10) | `jakarta.servlet` |

- Tomcat 9 (javax.servlet)

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat9</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </server>
```

- Tomcat 10 (jakarta.servlet)

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat10</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </server>
```

- Tomcat 11 (jakarta.servlet)

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat11</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </server>
```

- Jetty 12 (jakarta.servlet)

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-jetty12</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </server>
```

If you set multiple servers and maven is executed in the interactive mode, you can select a server at runtime.

## Features

### Coverage

You can use JaCoCo or Cobertura.
When you access to `/_coverage/`, you can see the coverages of your code.

#### Cobertura

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-coverage</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </feature>
```

#### JaCoCo

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-jacoco</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </feature>
```

### Dashboard

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-dashboard</artifactId>
    <version>1.4.0-SNAPSHOT</version>
    <type>war</type>
  </feature>
```

When you access to `/_dashboard/`, you can see the information of your application.

In dashboard, you can monitor the memory usage / cpu load of a server and redeploy your application.
Add `waitt-admin` feature to your configuration.

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-admin</artifactId>
    <version>1.4.0-SNAPSHOT</version>
  </feature>
```

### Tracer

You can show and search HTTP request/response logs at development in Kibana.
Note: waitt-tracer uses the Jakarta Servlet API, so it is only compatible with Tomcat 10/11 and Jetty 12.

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-tracer</artifactId>
    <version>1.4.0-SNAPSHOT</version>
    <configuration>
      <elasticsearch.url>http://[es host]:9200</elasticsearch.url>
    </configuration>
  </feature>
```

## Examples

The `examples/` directory contains sample applications:

| Example | Framework | Server |
|---------|-----------|--------|
| spring-boot | Spring Boot | waitt-tomcat11 |
| struts2 | Struts2 6.x | waitt-tomcat9 |
| nablarch | Nablarch 5u26 | waitt-tomcat9 |
| sa-struts | SAStruts | waitt-tomcat9 |

To run an example:

```shell
% cd examples/spring-boot
% mvn waitt:run
```

## for Developer

Refresh README and examples version placeholders from `pom.xml`:

```shell
% ./scripts/update-version.sh
```

ClassLoader hierarchy

```text
WaittRealm (Maven plugin)
   |
ServerRealm (Tomcat or Jetty)
   |
ApplicationRealm (Each Webapplication)
```
