package net.unit8.waitt;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.PlexusContainer;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * Web Application Integration Test Tool maven plugin.
 *
 * @author kawasima
 */
@Mojo(name = "run")
public class RunMojo extends AbstractMojo {
    @Parameter
    private int port;

    @Parameter(defaultValue = "8080")
    private int startPort;

    @Parameter(defaultValue = "9000")
    private int endPort;

    @Component
    protected MavenProject project;

    @Component
    protected MavenProjectBuilder projectBuilder;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    @Component
    private ArtifactResolver artifactResolver;

    @Component
    private ArtifactFactory artifactFactory;

    @Component
    private ArtifactMetadataSource metadataSource;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    protected ProjectBuilderConfiguration projectBuilderConfiguration = new DefaultProjectBuilderConfiguration();

    private static final String CONTEXT_PATH = "";
    private static final File COVERAGE_REPORT_DIR = new File("target/coverage");
    private static final int REPORT_INTERVAL_SECONDS = 30;
    protected String appBase;

    public void execute() throws MojoExecutionException, MojoFailureException {
        CoberturaClassLoader.instrumentedPackageNames = scanPackage(
                new File(project.getBuild().getSourceDirectory()));
        List<Artifact> artifacts = new ArrayList<Artifact>();
        List<File> classpathFiles = new ArrayList<File>();
        projectBuilderConfiguration.setLocalRepository(localRepository);
        PlexusContainer container = session.getContainer();
        Properties execution = session.getExecutionProperties();
        ProfileManager profileManager = new DefaultProfileManager(container, execution);

        for (String module : project.getModel().getModules()) {
            File modulePom = new File(module, "pom.xml");
            try {
                MavenProject subProject = projectBuilder.buildWithDependencies(modulePom, localRepository, profileManager);
                subProject.setRemoteArtifactRepositories(remoteRepositories);
                if (StringUtils.equals(subProject.getPackaging(), "war")) {
                    appBase = new File(module, "src/main/webapp").getAbsolutePath();
                }
                artifacts.addAll(subProject.getCompileArtifacts());
                artifacts.addAll(subProject.getRuntimeArtifacts());
                classpathFiles.add(new File(subProject.getBuild().getOutputDirectory()));
            } catch (Exception e) {
                throw new MojoExecutionException("module(" + module + ") build failure", e);
            }
        }
        List<URL> classpathUrls = new ArrayList<URL>();
        Set<String> uniqueArtifacts = new HashSet<String>();

        try {
            for (File classpathFile : classpathFiles) {
                URL url = classpathFile.toURI().toURL();
                classpathUrls.add(url);
            }
            for (Artifact artifact : artifacts) {
                String versionlessKey = ArtifactUtils.versionlessKey(artifact);
                if (!uniqueArtifacts.contains(versionlessKey)) {
                    classpathUrls.add(artifact.getFile().toURI().toURL());
                    uniqueArtifacts.add(versionlessKey);
                }
            }
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error during setting up classpath", e);
        }

        ClassLoader parentClassLoader = new URLClassLoader(
                classpathUrls.toArray(new URL[ classpathUrls.size()]),
                Thread.currentThread().getContextClassLoader()
        );

        if (appBase == null)
            appBase = new File("src/main/webapp").getAbsolutePath();
        getLog().info("App base: " + appBase);
        Tomcat tomcat = new Tomcat();
        if (port == 0)
            scanPort();
        tomcat.setPort(port);

        System.setProperty("catalina.home", ".");
        tomcat.setBaseDir(".");
        tomcat.getHost().setAppBase(appBase);

        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        try {
            Context context = tomcat.addWebapp(CONTEXT_PATH, appBase);
            WebappLoader webappLoader = new WebappLoader(parentClassLoader);
            webappLoader.setLoaderClass(CoberturaClassLoader.class.getName());
            webappLoader.setDelegate(((StandardContext) context).getDelegate());
            context.setLoader(webappLoader);
            tomcat.addWebapp("/coverage", COVERAGE_REPORT_DIR.getAbsolutePath());
            WaittServlet waittServlet = new WaittServlet(server);
            Context adminContext = tomcat.addContext("/waitt", "");
            tomcat.addServlet(adminContext, "waittServlet", waittServlet);
            adminContext.addServletMapping("/*", "waittServlet");
            new CoverageMonitor(webappLoader).start();
            tomcat.start();
            Desktop.getDesktop().browse(URI.create("http://localhost:" + port + "/"));
            server.await();
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

    private Set resolveExecutableDependencies( Artifact executablePomArtifact )
            throws MojoExecutionException {
        Set executableDependencies;
        try {
            MavenProject project = projectBuilder.buildFromRepository(
                    executablePomArtifact,
                    remoteRepositories,
                    localRepository);
            List dependencies = project.getDependencies();
            Set dependencyArtifacts = MavenMetadataSource.createArtifacts(artifactFactory, dependencies, null, null, null);
            dependencyArtifacts.add(project.getArtifact());
            ArtifactResolutionResult result = artifactResolver.resolveTransitively(
                    dependencyArtifacts,
                    executablePomArtifact,
                    Collections.EMPTY_MAP,
                    localRepository,
                    remoteRepositories,
                    metadataSource, null,
                    Collections.EMPTY_LIST);
            executableDependencies = result.getArtifacts();
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Encountered problems resolving dependencies of the executable " + "in preparation for its execution.",
                    e);
        }
        return executableDependencies;
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
                    PrintStream sysout = System.out;
                    System.setOut(new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM));

                    //TouchCollector.applyTouchesOnProjectData(data);
                    ProjectData.saveGlobalProjectData();

                    System.setOut(sysout);
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