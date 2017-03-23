package net.unit8.waitt.mojo.component;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.configuration.Server;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * A provider of a server.
 *
 * @author kawasima
 */
public class DefaultServerProvider implements ServerProvider {
    private static final Logger LOG = Logger.getLogger(DefaultServerProvider.class.getName());

    @Requirement
    private Prompter prompter;

    @Component
    protected ArtifactResolver artifactResolver;

    @Component
    protected RepositorySystem repositorySystem;

    @Override
    public ServerSpec getServer(Server server, ClassRealm parentRealm) {
        Artifact artifact = repositorySystem.createArtifact(server.getGroupId(), server.getArtifactId(), server.getVersion(), "jar");
        Set<URL> urls = artifactResolver.resolve(artifact, parentRealm);
        ClassLoader serverLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), parentRealm);
        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class, serverLoader);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Embedded server is not found.");
        }
        return new ServerSpec(iter.next(), serverLoader);
    }

    @Override
    public ServerSpec selectServer(List<Server> servers, ClassRealm parentRealm, boolean interactive) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalStateException("No settings for server.");
        }

        List<ServerSpec> serverSpecs = new ArrayList<ServerSpec>();
        for (Server server : servers) {
            ServerSpec serverSpec = getServer(server, parentRealm);
            serverSpecs.add(serverSpec);
        }

        if (interactive && serverSpecs.size() > 1) {
            try {
                prompter.showMessage("Detect multiple servers...\n");
                List<String> possibleValues = new ArrayList<String>();
                for (int i=0; i<serverSpecs.size(); i++) {
                    possibleValues.add(Integer.toString(i));
                    prompter.showMessage("  " + i + ". " + serverSpecs.get(i).getEmbeddedServer().getName() + "\n");
                }

                String res = prompter.prompt("What number will you use? (default: 0)", possibleValues, "0");
                int num = Integer.parseInt(res);
                return serverSpecs.get(num);
            } catch(PrompterException e) {
                throw new IllegalStateException("Prompt error.", e);
            }
        } else {
            return serverSpecs.get(0);
        }
    }

}
