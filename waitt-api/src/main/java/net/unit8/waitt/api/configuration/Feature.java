package net.unit8.waitt.api.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kawasima
 */
public class Feature implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String artifactId;
    private String version;
    private String type;

    private Map<String, String> configuration = new HashMap<>();

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, String> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, String> configuration) { this.configuration = configuration; }
}
