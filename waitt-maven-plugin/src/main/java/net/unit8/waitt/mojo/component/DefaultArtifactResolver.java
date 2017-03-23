/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.unit8.waitt.mojo.component;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

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
    public Set<URL> resolve(Artifact artifact, ClassLoader loader) {
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
        Set<URL> urls = new HashSet<URL>();
        try {
            for (Artifact resolvedArtifact : artifactResult.getArtifacts()) {
                if (!Artifact.SCOPE_PROVIDED.equals(resolvedArtifact.getScope()) && !Artifact.SCOPE_TEST.equals(resolvedArtifact.getScope())) {
                    urls.add(resolvedArtifact.getFile().toURI().toURL());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return urls;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }
}
