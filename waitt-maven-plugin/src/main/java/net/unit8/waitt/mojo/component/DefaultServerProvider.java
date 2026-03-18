package net.unit8.waitt.mojo.component;

import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.configuration.Server;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

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

    private ServerSpec selectByProperty(List<ServerSpec> serverSpecs, String serverProp) throws MojoFailureException {
        // Try as index
        try {
            int index = Integer.parseInt(serverProp);
            if (index >= 0 && index < serverSpecs.size()) {
                LOG.info("Selected server by index: " + serverSpecs.get(index).getEmbeddedServer().getName());
                return serverSpecs.get(index);
            }
            throw new MojoFailureException("Server index out of range: " + index
                    + " (available: 0-" + (serverSpecs.size() - 1) + ")");
        } catch (NumberFormatException e) {
            // Not a number, try as name match
        }

        // Try as name (partial match on server name or artifactId)
        for (ServerSpec spec : serverSpecs) {
            if (spec.getEmbeddedServer().getName().contains(serverProp)) {
                LOG.info("Selected server by name: " + spec.getEmbeddedServer().getName());
                return spec;
            }
        }

        List<String> names = new ArrayList<String>();
        for (ServerSpec spec : serverSpecs) {
            names.add(spec.getEmbeddedServer().getName());
        }
        throw new MojoFailureException("No server matching '" + serverProp + "'. Available: " + names);
    }

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

    @Override
    public ServerSpec selectServer(List<Server> servers, ClassRealm parentRealm, boolean interactive)
            throws MojoFailureException {
        if (servers == null || servers.isEmpty()) {
            throw new MojoFailureException(servers, "No settings for server.",
                    "server not found");
        }

        List<ServerSpec> serverSpecs = new ArrayList<ServerSpec>();
        for (Server server : servers) {
            ServerSpec serverSpec = getServer(server, parentRealm);
            serverSpecs.add(serverSpec);
        }

        String serverProp = System.getProperty("waitt.server");
        if (serverProp != null && !serverProp.isEmpty()) {
            return selectByProperty(serverSpecs, serverProp);
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
                throw new MojoFailureException("Prompt error.", e);
            }
        } else {
            return serverSpecs.get(0);
        }
    }

}
