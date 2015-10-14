WAITT
==================

WAITT is the Web Application Integration Test Tool.

## Usage

Add plugin to your pom.xml.

```xml
<plugin>
  <groupId>net.unit8.waitt</groupId>
  <artifactId>waitt-maven-plugin</artifactId>
  <version>0.4.0-SNAPSHOT</version>
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
- Tomcat7

## Features

### Coverage

```xml
<features>
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-coverage</artifactId>
  </feature>
</features>
```

### Dashboard

### Tracer

You can show and search logs at development in Kibana.

```xml
<features>
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-tracer</artifactId>
    <configuration>
      <elasticsearch.url>http://[es host]:9200</elasticsearch.url>
    </configuration>
  </feature>
</features>
