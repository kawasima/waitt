package net.unit8.waitt.mojo.component;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 *
 * @author kawasima
 */
public interface ArtifactResolver {
    ClassRealm resolve(Artifact artifact, ClassRealm parentRealm);
    void setProject(MavenProject project);
    void setSession(MavenSession session);
}
