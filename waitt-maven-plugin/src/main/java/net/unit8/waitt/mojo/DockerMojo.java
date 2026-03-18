package net.unit8.waitt.mojo;

import com.google.cloud.tools.jib.api.*;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import com.google.cloud.tools.jib.api.buildplan.Port;
import net.unit8.waitt.mojo.configuration.DockerConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Build a Docker/OCI image from a standalone JAR using Jib (no Docker daemon required).
 *
 * @author kawasima
 */
@Mojo(name = "docker", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DockerMojo extends JarMojo {

    @Parameter
    private DockerConfiguration docker;

    @Parameter(property = "docker.to")
    private String dockerTo;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File standaloneJar = buildStandaloneJar();

        if (docker == null) {
            docker = new DockerConfiguration();
        }
        if (dockerTo != null && !dockerTo.isEmpty()) {
            docker.setTo(dockerTo);
        }
        String imageName = docker.getImageName() != null ? docker.getImageName() : project.getArtifactId();
        String imageTag = docker.getImageTag() != null ? docker.getImageTag() : project.getVersion();
        String imageRef = imageName + ":" + imageTag;

        List<Integer> ports = docker.getPorts() != null ? docker.getPorts() : Collections.singletonList(8080);
        int appPort = ports.isEmpty() ? 8080 : ports.get(0);

        try {
            JibContainerBuilder builder = Jib.from(docker.getBaseImage())
                    .addLayer(
                            Collections.singletonList(standaloneJar.toPath()),
                            AbsoluteUnixPath.get("/app")
                    )
                    .setWorkingDirectory(AbsoluteUnixPath.get("/app"));

            for (int port : ports) {
                builder.addExposedPort(Port.tcp(port));
            }

            if (docker.getJavaOpts() != null && !docker.getJavaOpts().isEmpty()) {
                builder.addEnvironmentVariable("JAVA_TOOL_OPTIONS", docker.getJavaOpts());
            }

            builder.setEntrypoint(Arrays.asList(
                    "java", "-jar", "/app/" + standaloneJar.getName(),
                    "--port", String.valueOf(appPort)
            ));

            Containerizer containerizer = createContainerizer(imageRef);
            builder.containerize(containerizer);

            String to = docker.getTo() != null ? docker.getTo() : "daemon";
            getLog().info("Successfully built image: " + imageRef + " (to=" + to + ")");
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to build Docker image: " + e.getMessage(), e);
        }
    }

    private Containerizer createContainerizer(String imageRef) throws MojoFailureException, InvalidImageReferenceException {
        String to = docker.getTo();
        if (to == null || to.isEmpty()) {
            to = "daemon";
        }
        switch (to) {
            case "daemon":
                return Containerizer.to(DockerDaemonImage.named(imageRef));
            case "tar":
                Path tarPath = new File(project.getBuild().getDirectory(),
                        project.getArtifactId() + ".tar").toPath();
                return Containerizer.to(TarImage.at(tarPath).named(imageRef));
            case "registry":
                return Containerizer.to(RegistryImage.named(imageRef));
            default:
                throw new MojoFailureException("Invalid docker.to value: " + to
                        + ". Must be one of: daemon, tar, registry");
        }
    }
}
