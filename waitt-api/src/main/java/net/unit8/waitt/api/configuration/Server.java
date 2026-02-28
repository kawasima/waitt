package net.unit8.waitt.api.configuration;

import java.io.Serializable;
import lombok.Data;

/**
 * @author kawasima
 */
@Data
public class Server implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String artifactId;
    private String version;
}
