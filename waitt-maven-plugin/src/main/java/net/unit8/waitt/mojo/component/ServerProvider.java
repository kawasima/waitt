package net.unit8.waitt.mojo.component;

import java.util.List;
import net.unit8.waitt.api.configuration.Server;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author kawasima
 */

public interface ServerProvider {
    ServerSpec getServer(Server server, ClassRealm parentRealm);
    ServerSpec selectServer(List<Server> server, ClassRealm parentRealm, boolean interactive);
}
