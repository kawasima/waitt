package net.unit8.waitt.mojo;

import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author kawasima
 */
@Mojo(name = "run-headless")
public class RunHeadlessMojo extends AbstractRunMojo {
    @Override
    protected void afterStart() {
        // no-op
    }
}
