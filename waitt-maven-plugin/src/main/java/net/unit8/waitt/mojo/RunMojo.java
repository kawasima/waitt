package net.unit8.waitt.mojo;

import net.unit8.waitt.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * Web Application Integration Test Tool maven plugin.
 *
 * @author kawasima
 */
@SuppressWarnings("unchecked")
@Mojo(name = "run")
public class RunMojo extends AbstractMojo {
    @Parameter
    private int port;

    @Parameter(defaultValue = "8080")
    private int startPort;

    @Parameter(defaultValue = "9000")
    private int endPort;

    @Parameter(defaultValue = "")
    private String contextPath;

    @Parameter(defaultValue = "")
    private String path;

    @Parameter(defaultValue = "true")
    private boolean delegate;

    @Parameter(defaultValue = "true")
    private boolean interactive;

    /*
    @Parameter
    private List<Webapp> webapps;
    */

    @Component
    protected MavenProject project;

    @Component
    protected ProjectBuilder projectBuilder;

    @Component
    protected RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "target/coverage")
    protected File coverageReportDirectory;

    protected String appBase;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private void readArtifacts(String subDirectory, List<Artifact> artifacts, List<File> classpathFiles)
            throws MojoExecutionException {
        File modulePom = (subDirectory == null || subDirectory.isEmpty()) ? new File("pom.xml") : new File(subDirectory, "pom.xml");

        try {
            ProjectBuildingRequest request = session.getProjectBuildingRequest()
                    .setProcessPlugins(false)
                    .setResolveDependencies(true);

            ProjectBuildingResult result = projectBuilder.build(modulePom, request);
            MavenProject subProject = result.getProject();

            if ("war".equals(subProject.getPackaging())) {
                appBase = (subDirectory == null || subDirectory.isEmpty()) ?
                        new File("src/main/webapp").getAbsolutePath() :
                        new File(subDirectory, "src/main/webapp").getAbsolutePath();
            }
            for (Artifact dependency : subProject.getArtifacts()) {
                String scope = dependency.getScope();
                if (Artifact.SCOPE_COMPILE.equals(scope)
                        || Artifact.SCOPE_RUNTIME.equals(scope)
                        || Artifact.SCOPE_SYSTEM.equals(scope)) {
                    artifacts.add(dependency);
                }
            }
            classpathFiles.add(new File(subProject.getBuild().getOutputDirectory()));
        } catch (Exception e) {
            throw new MojoExecutionException("module(" + subDirectory + ") build failure", e);
        }
    }

    /**
     * Start tomcat.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        initLogger();
        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            throw new MojoExecutionException("Embedded server is not found.");
        }
        EmbeddedServer embeddedServer = iter.next();
        /*
        if (interactive) {
            for (EmbeddedServer server : serviceLoaders) {

            }
        }
        */
        if (port == 0)
            scanPort();
        embeddedServer.setPort(port);

        if (contextPath == null || contextPath.equals("/"))
            contextPath = "";
        embeddedServer.setBaseDir(".");

        try {
            List<URL> classpathUrls = resolveClasspaths();
            ClassLoader parentClassLoader = new ParentLastClassLoader(
                    classpathUrls.toArray(new URL[classpathUrls.size()]),
                    Thread.currentThread().getContextClassLoader());
            if (appBase == null)
                appBase = new File("src/main/webapp").getAbsolutePath();
            getLog().info("App base: " + appBase);
            getLog().info("Classpath:" + Arrays.asList(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()));
            embeddedServer.setClassLoader(parentClassLoader);

            embeddedServer.addContext(contextPath, appBase);
            /*
            if (webapps != null) {
                for (Webapp webapp : webapps) {
                    initExtraWebapp(tomcat, webapp);
                }
            }
            */

            /*
            WaittServlet waittServlet = new WaittServlet(server, executorService);
            Context adminContext = tomcat.addContext("/waitt", "");
            adminContext.setParentClassLoader(Thread.currentThread().getContextClassLoader());
            tomcat.addServlet(adminContext, "waittServlet", waittServlet);
            adminContext.addServletMapping("/*", "waittServlet");


            tomcat.start();
            */

            initCoverageContext(embeddedServer);

            initCoverageMonitor();
            embeddedServer.start();
            path = path == null ? "" : path;
            Desktop.getDesktop().browse(URI.create("http://localhost:" + port + contextPath + path));
            /*
            */
        } catch (Exception e) {
            throw new MojoExecutionException("Tomcat start failure", e);
        }
    }

    /*
    private void initExtraWebapp(Tomcat tomcat, Webapp webapp) throws MalformedURLException, ServletException{
        Artifact artifact = repositorySystem.createArtifact(
                webapp.getGroupId(),
                webapp.getArtifactId(),
                webapp.getVersion(),
                "war");
        ArtifactResolutionRequest warArtifactRequest = new ArtifactResolutionRequest()
                .setRemoteRepositories(project.getRemoteArtifactRepositories())
                .setArtifact(artifact);
        ArtifactResolutionResult warArtifactResult = repositorySystem.resolve(warArtifactRequest);
        if (warArtifactResult.hasExceptions()) {
            for (Exception e : warArtifactResult.getExceptions()) {
                getLog().error("resolve error.", e);
            }
        }

        Dependency d = new Dependency();
        d.setGroupId(webapp.getGroupId());
        d.setArtifactId(webapp.getArtifactId());
        d.setVersion(webapp.getVersion());
        d.setType("jar");
        ArtifactResolutionRequest artifactRequest = new ArtifactResolutionRequest();
        artifactRequest
                .setArtifact(repositorySystem.createDependencyArtifact(d))
                .setResolveTransitively(true)
                .setResolveRoot(false)
                .setLocalRepository(session.getLocalRepository())
                .setRemoteRepositories(project.getRemoteArtifactRepositories());
        ArtifactResolutionResult artifactResult = repositorySystem.resolve(artifactRequest);

        List<URL> classpathUrls = new ArrayList<URL>();
        for (Artifact dependency : artifactResult.getArtifacts()) {
            if (Artifact.SCOPE_PROVIDED.equals(artifact.getScope()))
                continue;
            classpathUrls.add(dependency.getFile().toURI().toURL());
        }

        if (webapp.getDependencies() != null) {
            for (Dependency dependency : webapp.getDependencies()) {
                ArtifactResolutionRequest depRequest = new ArtifactResolutionRequest();
                depRequest
                        .setArtifact(repositorySystem.createDependencyArtifact(dependency))
                        .setResolveRoot(true)
                        .setResolveTransitively(true)
                        .setLocalRepository(session.getLocalRepository())
                        .setRemoteRepositories(project.getRemoteArtifactRepositories());
                ArtifactResolutionResult depResult = repositorySystem.resolve(depRequest);
                for (Artifact depArtifact : depResult.getArtifacts()) {
                    if (Artifact.SCOPE_PROVIDED.equals(artifact.getScope()))
                        continue;
                    classpathUrls.add(depArtifact.getFile().toURI().toURL());
                }
            }
        }

        Context extContext = tomcat.addWebapp(webapp.getPath(), artifact.getFile().getAbsolutePath());
        extContext.addParameter("antiJARLocking", "false");
        extContext.addParameter("antiResourceLocking", "false");
        extContext.addParameter("unpackWARs", "false");
        extContext.setLoader(new WebappLoader(
                new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]),
                        Thread.currentThread().getContextClassLoader())));
        for (Map.Entry<String,String> entry : webapp.getConfiguration().entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }
    */

    private void initLogger() {
        Logger logger = Logger.getLogger("");
        logger.setLevel(ALL);
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        final Log mavenLogger = getLog();

        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (logRecord.getLoggerName().startsWith("sun.awt."))
                    return;
                Level lv = logRecord.getLevel();
                if (Arrays.asList(ALL, CONFIG, FINE, FINER, FINEST).contains(lv)) {
                    mavenLogger.debug(logRecord.getMessage());
                } else if (lv.equals(INFO)) {
                    mavenLogger.info(logRecord.getMessage());
                } else if (lv.equals(WARNING)) {
                    Throwable t = logRecord.getThrown();
                    if (t == null)
                        mavenLogger.warn(logRecord.getMessage());
                    else
                        mavenLogger.warn(logRecord.getMessage(), t);
                } else if (lv.equals(SEVERE)) {
                    Throwable t = logRecord.getThrown();
                    if (t == null)
                        mavenLogger.error(logRecord.getMessage());
                    else
                        mavenLogger.error(logRecord.getMessage(), t);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    private List<URL> resolveClasspaths() throws MojoExecutionException {
        TargetPackages.getInstance().set(
                PackageScanner.scan(new File(project.getBuild().getSourceDirectory())));
        List<Artifact> artifacts = new ArrayList<Artifact>();
        List<File> classpathFiles = new ArrayList<File>();

        if (project.getModel().getModules().isEmpty()) {
            readArtifacts("", artifacts, classpathFiles);
        } else {
            for (String module : project.getModel().getModules()) {
                readArtifacts(module, artifacts, classpathFiles);
            }
        }
        List<URL> classpathUrls = new ArrayList<URL>();
        Set<String> uniqueArtifacts = new HashSet<String>();

        try {
            for (File classpathFile : classpathFiles) {
                URL url = classpathFile.toURI().toURL();
                classpathUrls.add(url);
            }
            for (URL url : ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()) {
                if (url.toString().contains("/org/ow2/asm/")
                        || url.toString().contains("/waitt-maven-plugin/")
                        || url.toString().contains("/net/sourceforge/cobertura/")) {
                    classpathUrls.add(url);
                }
            }
            for (Artifact artifact : artifacts) {
                if ("provided".equals(artifact.getScope()))
                    continue;

                String versionlessKey = ArtifactUtils.versionlessKey(artifact);
                if (!uniqueArtifacts.contains(versionlessKey)) {
                    classpathUrls.add(artifact.getFile().toURI().toURL());
                    uniqueArtifacts.add(versionlessKey);
                }
            }
            return classpathUrls;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error during setting up classpath", e);
        }
    }

    private void initCoverageContext(EmbeddedServer embeddedServer) throws ServletException {
        // Cobertura coverage report
        if (!coverageReportDirectory.exists()) {
            // ignore error when create coverage directory.
            assert (coverageReportDirectory.mkdirs());
        }

        embeddedServer.addContext("/coverage", coverageReportDirectory.getAbsolutePath());
        //coverageContext.setParentClassLoader(Thread.currentThread().getContextClassLoader());
    }

    private void initCoverageMonitor() {
        final CoverageMonitorConfiguration config = new CoverageMonitorConfiguration();
        config.setCoverageReportDirectory(coverageReportDirectory);
        config.setSourceDirectory(new File(project.getBuild().getSourceDirectory()));
        executorService.execute(
            new Runnable() {
                @Override
                public void run() {
                    Class loaderClass;
                    try {
                        loaderClass = Class.forName("net.unit8.waitt.CoberturaClassLoader");
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                    while (true) {
                        try {
                            ClassLoader cl = (ClassLoader) loaderClass.getMethod("getInstance").invoke(null);
                            Class<?> monitorClass = cl.loadClass("net.unit8.waitt.CoverageMonitor");
                            Constructor<?> constructor = monitorClass.getConstructor(
                                    ClassLoader.class,
                                    CoverageMonitorConfiguration.class);
                            executorService.execute((Runnable) constructor.newInstance(cl, config));
                            break;
                        } catch (Exception e) {
                            getLog().warn(e);
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            assert false;
                        }
                    }
                }
            }
        );
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
}
