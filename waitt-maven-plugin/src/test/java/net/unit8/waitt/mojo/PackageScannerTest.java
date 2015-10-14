package net.unit8.waitt.mojo;

import org.junit.Test;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class PackageScannerTest {
    @Test
    public void test() {
        Set<String> packages = PackageScanner.scan(new File("examples/struts2/src/main/java"));
        String[] pkgArray = packages.toArray(new String[1]);
        assertEquals(1, pkgArray.length);
    }
}
