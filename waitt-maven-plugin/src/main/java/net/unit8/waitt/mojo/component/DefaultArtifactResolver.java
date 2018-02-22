/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.unit8.waitt.mojo.component;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kawasima
 */
public class DefaultArtifactResolver implements ArtifactResolver {
    private static final Logger LOG = Logger.getLogger(DefaultArtifactResolver.class.getName());

    @Component
    protected RepositorySystem repositorySystem;

    protected MavenProject project;
    protected MavenSession session;

    @Override
    public ClassRealm resolve(Artifact artifact, ClassRealm parent) {
        ArtifactResolutionRequest artifactRequest = new ArtifactResolutionRequest()
                .setRemoteRepositories(project.getRemoteArtifactRepositories())
                .setLocalRepository(session.getLocalRepository())
                .setResolveRoot(true)
                .setResolveTransitively(true)
                .setArtifact(artifact);
        ArtifactResolutionResult artifactResult = repositorySystem.resolve(artifactRequest);
        if (artifactResult.hasExceptions()) {
            for (Exception e : artifactResult.getExceptions()) {
                LOG.log(Level.SEVERE, "resolve error.", e);
            }
        }
        try {
            ClassRealm realm = new ClassRealm(parent.getWorld(), artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion(), ClassLoader.getSystemClassLoader());
            realm.setParentRealm(parent);
            for (Artifact resolvedArtifact : artifactResult.getArtifacts()) {
                if (!Artifact.SCOPE_PROVIDED.equals(resolvedArtifact.getScope()) && !Artifact.SCOPE_TEST.equals(resolvedArtifact.getScope())) {
                    realm.addURL(resolvedArtifact.getFile().toURI().toURL());
                }
            }
            return realm;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }
}
