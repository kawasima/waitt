package net.unit8.waitt.mojo.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.net.URL;
import java.util.Set;

/**
 *
 * @author kawasima
 */
@Data
@AllArgsConstructor
public class ExtraWebapp {
    private String name;
    private String warPath;
    private ClassRealm realm;
}
