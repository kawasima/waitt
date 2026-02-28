package net.unit8.waitt.api.configuration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 *
 * @author kawasima
 */
@Data
public class WebappConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationName;
    private File baseDirectory;
    private File sourceDirectory;
    private Set<String> packages = new HashSet<>();

    private List<Feature> features = new ArrayList<>();
}
