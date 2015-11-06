WAITT
==================

WAITT is the Web Application Integration Test Tool.

## Usage

Add plugin to your pom.xml

```xml
<plugin>
  <groupId>net.unit8.waitt</groupId>
  <artifactId>waitt-maven-plugin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <configuration>
    <servers>
      <server>
        <groupId>net.unit8.waitt.server</groupId>
        <artifactId>waitt-tomcat8</artifactId>
        <version>0.1.0-SNAPSHOT</version>
      </server>
    </servers>
  </configuration>
</plugin>
```

And execute following command to start tomcat8.

```shell
% mvn waitt:run
```

Then web browser will be started automatically.

## Supported server products

- Tomcat8

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat8</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </server>
```

- Tomcat7

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat7</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </server>
```

- Jetty9

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-jetty9</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </server>
```


If you set multiple servers and maven is executed in the interactive mode, you can select a server at runtime.


## Features

### Coverage

You can use JaCoCo or Cobertura.

#### Cobertura

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-coverage</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </feature>
```

#### JaCoCo

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-jacoco</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </feature>
```

### Dashboard

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-dashboard</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </feature>
```

In dashboard, you can monitor the memory usage / cpu load of a server and redeploy your application.
Add `waitt-admin` feature to your configuration.

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-admin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </feature>
```


### Tracer

You can show and search logs at development in Kibana.

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-tracer</artifactId>
    <configuration>
      <elasticsearch.url>http://[es host]:9200</elasticsearch.url>
    </configuration>
  </feature>
```
