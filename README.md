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
    <version>0.2.0</version>
</plugin>
```

And execute following command to start tomcat.

```shell
% mvn waitt:run
```

Then web browser will be started automatically.