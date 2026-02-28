package net.unit8.waitt.mojo;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 *
 * @author kawasima
 */
@Mojo(name = "run-headless", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class RunHeadlessMojo extends AbstractRunMojo {
    @Override
    protected void afterStart() {
        // no-op
    }
}
