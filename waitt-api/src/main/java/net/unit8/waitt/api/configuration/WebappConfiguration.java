package net.unit8.waitt.api.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 *
 * @author kawasima
 */
@Data
public class WebappConfiguration {
    private String applicationName;
    private File baseDirectory;
    private File sourceDirectory;
    private Set<String> packages;
    
    private List<Feature> features = new ArrayList<Feature>();
}
