package net.unit8.waitt.mojo.configuration;

import net.unit8.waitt.api.EmbeddedServer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author kawasima
 */
public class ServerSpec {
    private final EmbeddedServer embeddedServer;
    private final ClassRealm classRealm;

    public ServerSpec(EmbeddedServer embeddedServer, ClassRealm classRealm) {
        this.embeddedServer = embeddedServer;
        this.classRealm = classRealm;
    }

    public EmbeddedServer getEmbeddedServer() {
        return embeddedServer;
    }
    public ClassRealm getClassRealm() {
        return classRealm;
    }

}
