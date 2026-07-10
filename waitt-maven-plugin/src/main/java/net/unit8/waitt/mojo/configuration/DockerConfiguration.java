package net.unit8.waitt.mojo.configuration;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for the waitt:docker goal.
 *
 * @author kawasima
 */
public class DockerConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String baseImage = "eclipse-temurin:21-jre";
    private String imageName;
    private String imageTag;
    private List<Integer> ports = Collections.singletonList(8080);
    private String javaOpts = "";
    private String to = "daemon";

    public String getBaseImage() { return baseImage; }
    public void setBaseImage(String baseImage) { this.baseImage = baseImage; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImageTag() { return imageTag; }
    public void setImageTag(String imageTag) { this.imageTag = imageTag; }

    public List<Integer> getPorts() { return ports; }
    public void setPorts(List<Integer> ports) { this.ports = ports; }

    public String getJavaOpts() { return javaOpts; }
    public void setJavaOpts(String javaOpts) { this.javaOpts = javaOpts; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}
