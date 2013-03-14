package net.unit8.waitt;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;

/**
 * Cobertura maven plugin
 *
 * @author kawasima
 */
@Mojo(name = "run",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        configurator = "include-project-dependencies")
public class RunMojo extends AbstractMojo {
    @Parameter
    private int port;

    @Parameter(defaultValue = "8080")
    private int startPort;

    @Parameter(defaultValue = "9000")
    private int endPort;

    @Component
    protected MavenProject project;

    private static final String CONTEXT_PATH = "";
    private static final File COVERAGE_REPORT_DIR = new File("target/coverage");
    private static final int REPORT_INTERVAL_SECONDS = 30;

    public void execute() throws MojoExecutionException, MojoFailureException {
        CoberturaClassLoader.instrumentedPackageNames = scanPackage(
                new File(project.getBuild().getSourceDirectory()));

        String appBase = new File("src/main/webapp").getAbsolutePath();
        Tomcat tomcat = new Tomcat();
        if (port == 0)
            scanPort();
        tomcat.setPort(port);

        tomcat.setBaseDir(".");
        tomcat.getHost().setAppBase(appBase);

        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        try {
            Context context = tomcat.addWebapp(CONTEXT_PATH, appBase);
            WebappLoader webappLoader = new WebappLoader(Thread.currentThread().getContextClassLoader());
            webappLoader.setLoaderClass(CoberturaClassLoader.class.getName());
            webappLoader.setDelegate(((StandardContext) context).getDelegate());
            context.setLoader(webappLoader);
            new CoverageMonitor(webappLoader).start();
            tomcat.start();
            Desktop.getDesktop().browse(URI.create("http://localhost:" + port + "/"));
            tomcat.getServer().await();
        } catch (Exception e) {
            throw new MojoExecutionException("Tomcat start failure", e);
        }
    }
    protected void scanPort() {
        for (int p = startPort; p <= endPort; p++) {
            try {
                Socket sock = new Socket("localhost", p);
                sock.close();
            } catch (IOException e) {
                port = p;
                return;
            }
        }
        throw new RuntimeException("Can't find available port from " + startPort + " to " + endPort);
    }

    public Set<String> scanPackage(File sourceDirectory) {
        Set<String> packages = Sets.newHashSet();
        File[] directories = sourceDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f != null && f.isDirectory();
            }
        });
        for (File dir : directories) {
            scanPackageInner(dir, null, packages);
        }
        return packages;
    }

    private void scanPackageInner(File dir, String pkg, Set<String> packages) {
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (Iterables.all(Arrays.asList(files), new Predicate<File>() {
            @Override
            public boolean apply(File f) {
                return f != null && f.isDirectory();
            }
        })) {
            for (File f : files) {
                String prefix = Optional
                        .fromNullable(pkg)
                        .transform(new Function<String, String>() {
                            @Override
                            public String apply(String s) {
                                return s + ".";
                            }
                        })
                        .or("");
                scanPackageInner(f, prefix + dir.getName(), packages);
            }
        } else {
            packages.add(pkg);
        }
    }
    public static class CoverageMonitor extends Thread {
        private WebappLoader webappLoader;
        ComplexityCalculator complexity;
        FileFinder finder;
        public CoverageMonitor(WebappLoader webappLoader) {
            this.webappLoader = webappLoader;
            finder = new FileFinder();
            finder.addSourceDirectory("src/main/java");
            complexity = new ComplexityCalculator(finder);
        }
        @Override
        public void run() {
            while(true) {
                CoberturaClassLoader cl = (CoberturaClassLoader)webappLoader.getClassLoader();
                if (cl != null) {
                    ProjectData data = cl.getProjectData();
                    TouchCollector.applyTouchesOnProjectData(data);
                    try {
                        new HTMLReport(data, COVERAGE_REPORT_DIR, finder, complexity, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(REPORT_INTERVAL_SECONDS * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}