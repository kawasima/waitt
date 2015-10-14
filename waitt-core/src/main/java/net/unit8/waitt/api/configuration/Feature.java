package net.unit8.waitt.api.configuration;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 *
 * @author kawasima
 */
@Data
public class Feature implements Serializable {
    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    
    private Map<String, String> configuration;
}
