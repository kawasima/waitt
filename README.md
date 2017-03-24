WAITT
==================

WAITT is the Web Application Integration Test Tool.

Using WAITT, your can deploy your application to any server without building a war or jar file.

## Usage

Add plugin to your pom.xml

```xml
<plugin>
  <groupId>net.unit8.waitt</groupId>
  <artifactId>waitt-maven-plugin</artifactId>
  <version>1.1.0</version>
  <configuration>
    <servers>
      <server>
        <groupId>net.unit8.waitt.server</groupId>
        <artifactId>waitt-tomcat8</artifactId>
        <version>0.1.0</version>
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

If you do not want to start a browser, you can use a `run-headless` goal as follows.

```shell
% mvn waitt:run-headless
```

## Supported server products

- Tomcat8

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat8</artifactId>
    <version>0.1.0</version>
  </server>
```

- Tomcat7

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-tomcat7</artifactId>
    <version>0.1.0</version>
  </server>
```

- Jetty9

```xml
  <server>
    <groupId>net.unit8.waitt.server</groupId>
    <artifactId>waitt-jetty9</artifactId>
    <version>0.1.0</version>
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
    <version>0.1.0</version>
  </feature>
```

#### JaCoCo

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-jacoco</artifactId>
    <version>0.1.0</version>
  </feature>
```

### Dashboard

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-dashboard</artifactId>
    <version>0.1.0</version>
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
    <version>0.1.0</version>
  </feature>
```


### Tracer

You can show and search logs at development in Kibana.

```xml
  <feature>
    <groupId>net.unit8.waitt.feature</groupId>
    <artifactId>waitt-tracer</artifactId>
    <version>0.1.0</version>
    <configuration>
      <elasticsearch.url>http://[es host]:9200</elasticsearch.url>
    </configuration>
  </feature>
```
