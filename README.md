WAITT maven plugin
==================
The Web Application Integration Test Tool.
This plugin starts tomcat and measures test coverages automatically.

## Usage

Add plugin to your pom.xml.

```xml
<plugin>
    <groupId>net.unit8.maven.plugins</groupId>
    <artifactId>waitt-maven-plugin</artifactId>
    <version>0.4.0-SNAPSHOT</version>
    <configuration>
        <path>/action/welcome.do</path>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>net.unit8.waitt</groupId>
            <artifactId>waitt-tomcat8</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

And execute following command to start tomcat8.

```shell
% mvn waitt:run
```

Then web browser will be started automatically.
