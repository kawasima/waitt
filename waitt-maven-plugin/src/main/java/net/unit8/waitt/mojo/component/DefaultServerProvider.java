package net.unit8.waitt.mojo.component;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.configuration.Server;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 *
 * @author kawasima
 */
public class DefaultServerProvider implements ServerProvider {
    private static final Logger LOG = Logger.getGlobal();
    
    @Component
    protected ArtifactResolver artifactResolver;

    @Component
    protected RepositorySystem repositorySystem;
    
    
    @Override
    public ServerSpec getServer(Server server, ClassRealm parentRealm) {
        Artifact artifact = repositorySystem.createArtifact(server.getGroupId(), server.getArtifactId(), server.getVersion(), "jar");
        ClassRealm serverRealm = artifactResolver.resolve(artifact, parentRealm);
        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class, serverRealm);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Embedded server is not found.");
        }
        return new ServerSpec(iter.next(), serverRealm);
    }
   
}
