package net.unit8.waitt.feature.jacoco;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.jacoco.report.ISourceFileLocator;

/**
 *
 * @author kawasima
 */
public class SourceFileCollection implements ISourceFileLocator {
    private final List<File> sourceRoots;
    private final String encoding;
    
    public SourceFileCollection(final List<File> sourceRoots, final String encoding) {
        this.sourceRoots = sourceRoots;
        this.encoding = encoding;
    }
    
    @Override
    public Reader getSourceFile(final String packageName, final String fileName) throws IOException {
        final String r;
        if (packageName.length() > 0) {
            r = packageName + '/' + fileName;
        } else {
            r = fileName;
        }
        for (final File sourceRoot : sourceRoots) {
            final File file = new File(sourceRoot, r);
            if (file.exists() && file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    return new InputStreamReader(fis, encoding);
                } catch (Exception e) {
                    fis.close();
                    throw e;
                }
            }
        }
        return null;
    }
    
    @Override
    public int getTabWidth() {
        return 4;
    }
}
