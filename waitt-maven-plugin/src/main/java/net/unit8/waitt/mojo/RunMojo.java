package net.unit8.waitt.mojo;


import org.apache.maven.plugins.annotations.Mojo;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Web Application Integration Test Tool maven plugin.
 *
 * @author kawasima
 */
@Mojo(name = "run")
public class RunMojo extends AbstractRunMojo {
    protected void afterStart() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create("http://localhost:"
                    + getPort() + getContextPath() + getPath()));
        }
    }
}
