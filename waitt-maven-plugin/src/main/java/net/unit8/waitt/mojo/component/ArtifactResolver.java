package net.unit8.waitt.mojo.component;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.net.URL;
import java.util.Set;

/**
 *
 * @author kawasima
 */
public interface ArtifactResolver {
    Set<URL> resolve(Artifact artifact, ClassLoader parent);
    void setProject(MavenProject project);
    void setSession(MavenSession session);
}
