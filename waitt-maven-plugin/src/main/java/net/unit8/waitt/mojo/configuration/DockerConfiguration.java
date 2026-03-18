package net.unit8.waitt.mojo.configuration;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for the waitt:docker goal.
 *
 * @author kawasima
 */
@Data
public class DockerConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String baseImage = "eclipse-temurin:21-jre";
    private String imageName;
    private String imageTag;
    private List<Integer> ports = Collections.singletonList(8080);
    private String javaOpts = "";
    private String to = "daemon";
}
