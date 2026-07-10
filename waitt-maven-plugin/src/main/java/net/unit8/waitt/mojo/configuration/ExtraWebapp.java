package net.unit8.waitt.mojo.configuration;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 *
 * @author kawasima
 */
public class ExtraWebapp {
    private final String name;
    private final String warPath;
    private final ClassRealm realm;

    public ExtraWebapp(String name, String warPath, ClassRealm realm) {
        this.name = name;
        this.warPath = warPath;
        this.realm = realm;
    }

    public String getName() { return name; }
    public String getWarPath() { return warPath; }
    public ClassRealm getRealm() { return realm; }
}
