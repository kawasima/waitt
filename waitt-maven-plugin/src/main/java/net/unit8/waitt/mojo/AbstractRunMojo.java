package net.unit8.waitt.mojo;

import net.unit8.waitt.api.*;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.Server;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.mojo.component.ArtifactResolver;
import net.unit8.waitt.mojo.component.ServerProvider;
import net.unit8.waitt.mojo.configuration.ExtraWebapp;
import net.unit8.waitt.mojo.configuration.ServerSpec;
import net.unit8.waitt.mojo.log.WaittLogHandler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.DirectoryScanner;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * @author kawasima
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRunMojo extends AbstractMojo {
    private static final String[] WELLKNOWN_DOCROOT = {"src/main/webapp", "WebContent"};

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

    @Parameter
    private List<Server> servers;

    @Parameter
    private List<Feature> features;

    @Parameter
    protected File docBase;

    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    @Component
    protected RepositorySystem repositorySystem;

    @Component
    protected ArtifactResolver artifactResolver;

    @Component
    protected ServerProvider serverProvider;


    private final List<ServerMonitor> serverMonitors = new ArrayList<ServerMonitor>();
    private final List<LogListener> logListeners = new ArrayList<LogListener>();
    private final List<ExtraWebapp> extraWebapps = new ArrayList<ExtraWebapp>();
    private final List<WebappDecorator> webappDecorators = new ArrayList<WebappDecorator>();


    /**
     * Start embedded server.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AnsiConsole.systemInstall();
        artifactResolver.setProject(project);
        artifactResolver.setSession(session);
        initLogger();
        if (docBase == null)
            docBase = scanDocBase(new File("."));
        WebappConfiguration webappConfig = new WebappConfiguration();
        webappConfig.setApplicationName(project.getName());
        webappConfig.setBaseDirectory(docBase);
        webappConfig.setPackages(PackageScanner.scan(new File(project.getBuild().getSourceDirectory())));
        webappConfig.setSourceDirectory(new File(project.getBuild().getSourceDirectory()));

        ClassRealm waittRealm = (ClassRealm) Thread.currentThread().getContextClassLoader();
        ServerSpec serverSpec = serverProvider.selectServer(servers, waittRealm, session.getSettings().getInteractiveMode());
        EmbeddedServer embeddedServer = serverSpec.getEmbeddedServer();

        if (port == 0)
            scanPort();
        embeddedServer.setPort(port);

        if (contextPath == null || contextPath.equals("/"))
            contextPath = "";
        embeddedServer.setBaseDir(".");

        loadFeature(waittRealm, webappConfig);

        try {
            embeddedServer.start();
            List<URL> classpathUrls = resolveClasspaths();
            ClassLoader webappRealm = new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]),
                    serverSpec.getClassLoader());

            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.init(embeddedServer);
            }
            embeddedServer.setWebappDecorators(webappDecorators);
            embeddedServer.setMainContext(contextPath, docBase.getAbsolutePath(), webappRealm);
            for (ExtraWebapp extraWebapp : extraWebapps) {
                Set<URL> urls = extraWebapp.getUrls();
                ClassLoader extraWebappLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), serverSpec.getClassLoader());
                embeddedServer.addContext("/_" + extraWebapp.getName(), extraWebapp.getWarPath(), extraWebappLoader);
            }

            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.start(embeddedServer);
            }
            path = path == null ? "" : path;

            afterStart();
            embeddedServer.await();
        } catch (Exception e) {
            throw new MojoExecutionException("Fail to start server", e);
        } finally {
            embeddedServer.stop();
        }
    }

    abstract protected void afterStart() throws IOException;

    /**
     * Read artifacts.
     *
     * @param subDirectory a sub directory
     * @param artifacts artifacts
     * @param classpathFiles classpaths
     * @throws MojoExecutionException
     */
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
                docBase = (subDirectory == null || subDirectory.isEmpty()) ?
                        scanDocBase(new File(".")) :
                        scanDocBase(new File(subDirectory));
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
     * Load features.
     *
     * @param waittRealm The realm of this plugin
     * @param config     The configuration of web application
     */
    private void loadFeature(ClassRealm waittRealm, WebappConfiguration config) {
        if (features == null) return;
        for (Feature feature : features) {
            String type = feature.getType();
            if (type == null) {
                type = "jar";
            }
            Artifact artifact = repositorySystem.createArtifact(feature.getGroupId(), feature.getArtifactId(), feature.getVersion(), type);
            Set<URL> urls = artifactResolver.resolve(artifact, waittRealm);
            config.getFeatures().add(feature);

            if ("war".equals(artifact.getType())) {
                String name = artifact.getArtifactId();
                if (name.startsWith("waitt-")) {
                    name = name.substring("waitt-".length());
                }
                extraWebapps.add(new ExtraWebapp(name, artifact.getFile().getAbsolutePath(), urls));
            } else {
                ClassLoader featureLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), waittRealm);
                ServiceLoader<ServerMonitor> serverMonitorLoader = ServiceLoader.load(ServerMonitor.class, featureLoader);
                for (ServerMonitor serverMonitor : serverMonitorLoader) {
                    if (serverMonitor instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) serverMonitor).config(config);
                    }
                    serverMonitors.add(serverMonitor);
                }

                ServiceLoader<LogListener> logListenerLoader = ServiceLoader.load(LogListener.class, featureLoader);
                for (LogListener logListener : logListenerLoader) {
                    if (logListener instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) logListener).config(config);
                    }
                    logListeners.add(logListener);
                }
                ServiceLoader<WebappDecorator> webappDecoratorLoader = ServiceLoader.load(WebappDecorator.class, featureLoader);
                for (WebappDecorator webappDecorator : webappDecoratorLoader) {
                    if (webappDecorator instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) webappDecorator).config(config);
                    }
                    webappDecorators.add(webappDecorator);
                }
            }
        }
    }

    /**
     * Initialize logger.
     */
    private void initLogger() {
        Logger logger = Logger.getLogger("");
        logger.setLevel(ALL);
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        logger.addHandler(new WaittLogHandler(getLog()));
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLoggerName() != null && record.getLoggerName().startsWith("sun.awt."))
                    return;
                Level lv = record.getLevel();
                for (LogListener logListener : logListeners) {
                    if (Arrays.asList(ALL, CONFIG, FINE, FINER, FINEST).contains(lv)) {
                        logListener.debug(record.getMessage(), record.getThrown());
                    } else if (lv.equals(INFO)) {
                        logListener.info(record.getMessage(), record.getThrown());
                    } else if (lv.equals(WARNING)) {
                        logListener.warn(record.getMessage(), record.getThrown());
                    } else if (lv.equals(SEVERE)) {
                        logListener.error(record.getMessage(), record.getThrown());
                    }
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

    /**
     * Resolve dependencies as classpath.
     *
     * @return find classpath urls
     * @throws MojoExecutionException
     */
    private List<URL> resolveClasspaths() throws MojoExecutionException {
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

    /**
     * Scan a base directory.
     *
     * @param baseDir
     * @return document
     * @throws MojoExecutionException
     */
    protected File scanDocBase(File baseDir) throws MojoExecutionException {
        for (String dirStr : WELLKNOWN_DOCROOT) {
            File docBase = new File(baseDir, dirStr);
            if (docBase.isDirectory()) {
                return docBase;
            }
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDir);
        scanner.setIncludes(new String[]{"web.xml"});
        scanner.addDefaultExcludes();
        scanner.scan();
        for (String path : scanner.getIncludedFiles()) {
            File webxml = new File(baseDir, path);
            if ("WEB-INF".equals(webxml.getParentFile().getName())) {
                return webxml.getParentFile().getParentFile();
            }
        }

        File dummy = new File(baseDir, "target/dummy_webapp");
        if (!dummy.exists() && !dummy.mkdirs())
            throw new MojoExecutionException("Can't create webapp directory");
        return dummy;
    }

    /**
     * Scan an available port.
     */
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

    public int getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getPath() {
        return path;
    }
}
