package net.unit8.waitt.feature.jacoco;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;

/**
 *
 * @author kawasima
 */
public class BundleCreator {
    public IBundleCoverage createBundle(
            final ExecutionDataStore executionDataStore) throws IOException {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionDataStore, builder);
        
        final File classesDir = new File("target/classes");
        Collection<File> filesToAnalyze = FileUtils.listFiles(classesDir, new String[]{"class"}, true);
        for (final File file : filesToAnalyze) {
            analyzer.analyzeAll(file);
        }
        
        return builder.getBundle("project");
    }
}
