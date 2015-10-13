package net.unit8.waitt.mojo.configuration;

import net.unit8.waitt.EmbeddedServer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author kawasima
 */
public class ServerSpec {
    private EmbeddedServer embeddedServer;
    private ClassRealm classRealm;

    public ServerSpec(EmbeddedServer embeddedServer, ClassRealm classRealm) {
        this.embeddedServer = embeddedServer;
        this.classRealm = classRealm;
    }
    public EmbeddedServer getEmbeddedServer() {
        return embeddedServer;
    }

    public void setEmbeddedServer(EmbeddedServer embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

    public ClassRealm getClassRealm() {
        return classRealm;
    }

    public void setClassRealm(ClassRealm classRealm) {
        this.classRealm = classRealm;
    }
}
