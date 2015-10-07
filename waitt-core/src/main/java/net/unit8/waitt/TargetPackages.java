package net.unit8.waitt;

import java.util.Set;

/**
 * @author kawasima
 */
@SuppressWarnings("CanBeFinal")
public class TargetPackages {
    private static final TargetPackages targetPackages = new TargetPackages();
    private Set<String> packages;

    public static TargetPackages getInstance() {
        return targetPackages;
    }

    public void set(Set<String> packages) {
        this.packages = packages;
    }

    public Set<String> get() {
        return packages;
    }
}
