package net.unit8.waitt.mojo;

import net.unit8.waitt.api.configuration.Server;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.shade.DefaultShader;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.filter.Filter;
import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ManifestResourceTransformer;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.apache.maven.plugins.shade.resource.ServicesResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author kawasima
 */
@Mojo(name = "jar", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JarMojo extends AbstractMojo {
    private static final String[] WELLKNOWN_DOCROOT = {"src/main/webapp", "WebContent"};

    @Component( role = Archiver.class, hint = "jar" )
    private JarArchiver jarArchiver;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    protected RepositorySystem repositorySystem;

    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    @Parameter( defaultValue = "${project.build.directory}", required = true )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Parameter
    private List<Server> servers;

    protected void createArchive(File jarFile) throws MojoExecutionException {
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);
        archiver.getArchiver().addDirectory(new File(project.getBuild().getOutputDirectory()));
        try {
            archiver.createArchive(session, project, archive);
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling JAR", e);
        }
    }

    protected File getJarFile( File basedir, String resultFinalName) {
        if ( basedir == null ) {
            throw new IllegalArgumentException( "basedir is not allowed to be null" );
        }
        if ( resultFinalName == null ) {
            throw new IllegalArgumentException( "finalName is not allowed to be null" );
        }

        return new File( basedir, resultFinalName + ".jar");
    }

    protected Set<File> getDependencies() {
        Set<File> artifacts = new HashSet<File>();
        getLog().info(project.getArtifacts().toString());
        for (Artifact artifact : project.getArtifacts()) {
            artifacts.add(artifact.getFile());
        }
        return artifacts;
    }

    protected ManifestResourceTransformer createManifestTransformer(String mainClass)
            throws MojoExecutionException {
        try {
            ManifestResourceTransformer transformer = new ManifestResourceTransformer();
            Field mainClassField = ManifestResourceTransformer.class.getDeclaredField("mainClass");
            mainClassField.setAccessible(true);
            mainClassField.set(transformer, mainClass);
            return transformer;
        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }
    }

    private void resolveArtifact(String groupId, String artifactId, String version, Set<File> jars) {
        Artifact artifact = repositorySystem.createArtifact(groupId, artifactId, version, "jar");
        ArtifactResolutionRequest artifactRequest = new ArtifactResolutionRequest()
                .setRemoteRepositories(project.getRemoteArtifactRepositories())
                .setLocalRepository(session.getLocalRepository())
                .setResolveRoot(true)
                .setResolveTransitively(true)
                .setArtifact(artifact);
        ArtifactResolutionResult artifactResult = repositorySystem.resolve(artifactRequest);
        for (Artifact resolvedArtifact : artifactResult.getArtifacts()) {
            if (!Artifact.SCOPE_PROVIDED.equals(resolvedArtifact.getScope()) && !Artifact.SCOPE_TEST.equals(resolvedArtifact.getScope())) {
                jars.add(resolvedArtifact.getFile());
            }
        }
    }
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (servers.isEmpty()) {
            throw new MojoFailureException("Server is not found.");
        }
        DefaultShader shader = new DefaultShader();
        shader.enableLogging(new ConsoleLogger(Logger.LEVEL_DEBUG, "shader"));

        ShadeRequest request = new ShadeRequest();

        File jarFile = getJarFile(outputDirectory, finalName);
        createArchive(jarFile);

        request.setUberJar(new File(outputDirectory, project.getBuild().getFinalName() + "-standalone.jar"));

        Set<File> jars = getDependencies();
        Server server = servers.get(0);
        resolveArtifact(server.getGroupId(), server.getArtifactId(), server.getVersion(), jars);
        resolveArtifact("net.unit8.waitt", "waitt-embed-runner", "1.2.0-SNAPSHOT", jars);

        jars.add(new File(outputDirectory, project.getBuild().getFinalName() + ".jar"));

        request.setJars(jars);
        request.setFilters(Collections.<Filter>emptyList());
        request.setRelocators(Collections.<Relocator>emptyList());

        List<ResourceTransformer> transformers = new ArrayList<ResourceTransformer>();
        transformers.add(createManifestTransformer("net.unit8.waitt.embed.Runner"));
        transformers.add(new ServicesResourceTransformer());
        request.setResourceTransformers(transformers);
        try {
            shader.shade(request);
        } catch (IOException ex) {
            throw new MojoExecutionException("", ex);
        }
    }

}
