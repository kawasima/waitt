package net.unit8.waitt.api.configuration;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kawasima
 */
@Data
public class FilterConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String className;
    private List<String> urlPatterns;
}
