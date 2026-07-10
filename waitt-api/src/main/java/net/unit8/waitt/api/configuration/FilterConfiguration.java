package net.unit8.waitt.api.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * @author kawasima
 */
public class FilterConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String className;
    private List<String> urlPatterns;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<String> getUrlPatterns() { return urlPatterns; }
    public void setUrlPatterns(List<String> urlPatterns) { this.urlPatterns = urlPatterns; }
}
