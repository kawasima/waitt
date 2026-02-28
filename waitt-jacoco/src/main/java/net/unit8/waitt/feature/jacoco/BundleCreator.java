package net.unit8.waitt.feature.jacoco;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

        final Path classesDir = new File("target/classes").toPath();
        try (Stream<Path> paths = Files.walk(classesDir)) {
            List<File> filesToAnalyze = paths
                    .filter(p -> p.toString().endsWith(".class"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (final File file : filesToAnalyze) {
                analyzer.analyzeAll(file);
            }
        }

        return builder.getBundle("project");
    }
}
