package net.unit8.waitt.mojo;


import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Web Application Integration Test Tool maven plugin.
 *
 * @author kawasima
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class RunMojo extends AbstractRunMojo {
    protected void afterStart() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create("http://localhost:"
                    + getPort() + getContextPath() + getPath()));
        }
    }
}
