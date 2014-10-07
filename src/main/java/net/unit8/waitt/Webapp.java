package net.unit8.waitt;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class Webapp {
    private String path;
    private String groupId;
    private String artifactId;
    private String version;

    private Map<String, String> configuration;
    private List<Dependency> dependencies;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfigurations(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
