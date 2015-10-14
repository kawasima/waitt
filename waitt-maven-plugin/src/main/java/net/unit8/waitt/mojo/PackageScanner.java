package net.unit8.waitt.mojo;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class PackageScanner {
    public static Set<String> scan(File sourceDirectory) {
        Set<String> packages = new HashSet<String>();
        File[] directories = sourceDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f != null && f.isDirectory();
            }
        });
        if (directories != null) {
            for (File dir : directories) {
                scanPackageInner(dir, null, packages);
            }
        }
        return packages;
    }

    private static void scanPackageInner(File dir, String pkg, Set<String> packages) {
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null)
            return;

        Boolean isAllDirectory = true;
        for (File f : files) {
            isAllDirectory = (f != null && f.isDirectory());
        }
        if (isAllDirectory) {
            for (File f : files) {
                String prefix = (pkg == null) ? "" : pkg + ".";
                scanPackageInner(f, prefix + dir.getName(), packages);
            }
        } else {

            packages.add(pkg == null ? dir.getName() : pkg);
        }
    }
}
