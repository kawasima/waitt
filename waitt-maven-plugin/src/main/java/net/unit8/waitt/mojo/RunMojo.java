package net.unit8.waitt.mojo;

import net.unit8.waitt.api.*;
import net.unit8.waitt.mojo.component.ServerProvider;
import net.unit8.waitt.mojo.component.ArtifactResolver;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.Server;
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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.fusesource.jansi.AnsiConsole;

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

    @Parameter
    private List<Server> servers;
    
    @Parameter
    private List<Feature> features;

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
    
    protected String appBase;

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
        if (appBase == null)
            appBase = new File("src/main/webapp").getAbsolutePath();
        WebappConfiguration webappConfig = new WebappConfiguration();
        webappConfig.setApplicationName(project.getName());
        webappConfig.setBaseDirectory(new File(appBase));
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
            ClassRealm webappRealm = serverSpec.getClassRealm().createChildRealm("Application");
            List<URL> classpathUrls = resolveClasspaths();
            for (URL url : classpathUrls) {
                webappRealm.addURL(url);
            }

            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.init(embeddedServer);
            }
            embeddedServer.setWebappDecorators(webappDecorators);
            embeddedServer.setMainContext(contextPath, appBase, webappRealm);
            for (ExtraWebapp extraWebapp : extraWebapps) {
                extraWebapp.getRealm().setParentRealm(serverSpec.getClassRealm());
                embeddedServer.addContext("/_" + extraWebapp.getName(), extraWebapp.getWarPath(), extraWebapp.getRealm());
            }

            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.start(embeddedServer);
            }
            path = path == null ? "" : path;
            Desktop.getDesktop().browse(URI.create("http://localhost:" + port + contextPath + path));
            embeddedServer.await();
        } catch (Exception e) {
            throw new MojoExecutionException("Tomcat start failure", e);
        } finally {
            embeddedServer.stop();
        }
    }

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

    private void loadFeature(ClassRealm waittRealm, WebappConfiguration config) {
        if (features == null) return;
        for (Feature feature : features) {
            String type = feature.getType();
            if (type == null) {
                type = "jar";
            }
            Artifact artifact = repositorySystem.createArtifact(feature.getGroupId(), feature.getArtifactId(), feature.getVersion(), type);
            ClassRealm realm = artifactResolver.resolve(artifact, waittRealm);
            config.getFeatures().add(feature);

            if ("war".equals(artifact.getType())) {
                String name = artifact.getArtifactId();
                if (name.startsWith("waitt-")) {
                    name = name.substring("waitt-".length());
                }
                extraWebapps.add(new ExtraWebapp(name, artifact.getFile().getAbsolutePath(), realm));
            } else {
                ServiceLoader<ServerMonitor> serverMonitorLoader = ServiceLoader.load(ServerMonitor.class, realm);
                for (ServerMonitor serverMonitor : serverMonitorLoader) {
                    if (serverMonitor instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) serverMonitor).config(config);
                    }
                    serverMonitors.add(serverMonitor);
                }

                ServiceLoader<LogListener> logListenerLoader = ServiceLoader.load(LogListener.class, realm);
                for (LogListener logListener : logListenerLoader) {
                    if (logListener instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) logListener).config(config);
                    }
                    logListeners.add(logListener);
                }
                ServiceLoader<WebappDecorator> webappDecoratorLoader = ServiceLoader.load(WebappDecorator.class, realm);
                for (WebappDecorator webappDecorator : webappDecoratorLoader) {
                    if (webappDecorator instanceof ConfigurableFeature) {
                        ((ConfigurableFeature) webappDecorator).config(config);
                    }
                    webappDecorators.add(webappDecorator);
                }
            }
        }
    }

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
