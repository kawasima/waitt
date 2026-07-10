package net.unit8.waitt.api.configuration;

import java.io.Serializable;

/**
 * @author kawasima
 */
public class Server implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String artifactId;
    private String version;

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
