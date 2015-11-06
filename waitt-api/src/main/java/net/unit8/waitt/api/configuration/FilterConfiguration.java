package net.unit8.waitt.api.configuration;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kawasima
 */
@Data
public class FilterConfiguration implements Serializable {
    private String name;
    private String className;
    private String[] urlPattern;
}
