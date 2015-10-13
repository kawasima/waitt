package net.unit8.waitt.mojo;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.unit8.waitt.EmbeddedServer;
import net.unit8.waitt.mojo.configuration.Server;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author kawasima
 */

public class ServerProvider {
    private static final Logger LOG = Logger.getGlobal();

    private RepositorySystem repositorySystem;
    private ClassRealm rootRealm;
    private List<ArtifactRepository> remoteRepositories;
    private MavenSession session;

    public ServerProvider(MavenSession session, RepositorySystem repositorySystem, ClassRealm rootRealm, List<ArtifactRepository> remoteRepositories) {
        this.session = session;
        this.repositorySystem = repositorySystem;
        this.rootRealm = rootRealm;
        this.remoteRepositories = remoteRepositories;
    }


    public ServerSpec getServer(Server server) {
        Artifact artifact = repositorySystem.createArtifact(
                server.getGroupId(),
                server.getArtifactId(),
                server.getVersion(),
                "jar");
        ArtifactResolutionRequest warArtifactRequest = new ArtifactResolutionRequest()
                .setRemoteRepositories(remoteRepositories)
                .setLocalRepository(session.getLocalRepository())
                .setResolveRoot(true)
                .setResolveTransitively(true)
                .setArtifact(artifact);
        ArtifactResolutionResult warArtifactResult = repositorySystem.resolve(warArtifactRequest);
        if (warArtifactResult.hasExceptions()) {
            for (Exception e : warArtifactResult.getExceptions()) {
                LOG.log(Level.SEVERE, "resolve error.", e);
            }
        }

        ClassRealm serverRealm;
        try {
            serverRealm = rootRealm.createChildRealm(server.getGroupId() + ":" + server.getArtifactId() + ":" + server.getVersion());
                    
            for (Artifact resolvedArtifact : warArtifactResult.getArtifacts()) {
                if (!Artifact.SCOPE_PROVIDED.equals(resolvedArtifact.getScope())) {
                    serverRealm.addURL(resolvedArtifact.getFile().toURI().toURL());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class, serverRealm);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Embedded server is not found.");
        }
        return new ServerSpec(iter.next(), serverRealm);
    }
}
