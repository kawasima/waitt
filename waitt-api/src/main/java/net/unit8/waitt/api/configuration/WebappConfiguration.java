package net.unit8.waitt.api.configuration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author kawasima
 */
public class WebappConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationName;
    private File baseDirectory;
    private File sourceDirectory;
    private File outputDirectory;
    private Set<String> packages = new HashSet<>();
    private List<Feature> features = new ArrayList<>();

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public File getBaseDirectory() { return baseDirectory; }
    public void setBaseDirectory(File baseDirectory) { this.baseDirectory = baseDirectory; }

    public File getSourceDirectory() { return sourceDirectory; }
    public void setSourceDirectory(File sourceDirectory) { this.sourceDirectory = sourceDirectory; }

    public File getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(File outputDirectory) { this.outputDirectory = outputDirectory; }

    public Set<String> getPackages() { return packages; }
    public void setPackages(Set<String> packages) { this.packages = packages; }

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }
}
