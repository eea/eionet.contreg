package eionet.cr.util;

import eionet.cr.config.GeneralConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * Utility to synchronise files generated on the Jenkins worker into the Virtuoso container
 * before the RDF loader accesses them.
 */
public final class DockerFileSync {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerFileSync.class);

    private DockerFileSync() {
        // utility class
    }

    /**
     * Ensure that the given host file is available inside the Docker container.
     *
     * @param hostFilePath the path written by the JVM on the Jenkins worker
     * @return the path that should be used by Virtuoso (container path or original if sync disabled)
     */
    public static String ensureFileAccessible(String hostFilePath) {

        String enabled = GeneralConfig.getProperty("config.docker.copy.enabled", "false");
        if (!"true".equalsIgnoreCase(enabled)) {
            return hostFilePath;
        }

        File hostFile = new File(hostFilePath);
        if (!hostFile.exists()) {
            LOGGER.warn("Host file does not exist for Docker sync: {}", hostFilePath);
            return hostFilePath;
        }

        String hostBase = GeneralConfig.getProperty("config.docker.sharedVolume");
        String containerBase = GeneralConfig.getProperty("config.docker.container.path", "/tmp/tmp_cr");
        if (StringUtils.isBlank(hostBase)) {
            hostBase = hostFile.getParentFile().getParent();
        }

        String containerPath = hostFilePath;
        if (StringUtils.isNotBlank(hostBase) && hostFilePath.startsWith(hostBase)) {
            containerPath = containerBase + hostFilePath.substring(hostBase.length());
        }

        File containerFile = new File(containerPath);

        String containerMatch = GeneralConfig.getProperty("config.docker.container.match", "virtuoso");
        String containerId = findContainer(containerMatch);
        if (containerId == null) {
            LOGGER.warn("Could not find Docker container matching '{}'; falling back to host path", containerMatch);
            return hostFilePath;
        }

        try {
            execute("docker", "exec", containerId, "mkdir", "-p", containerFile.getParent());
            execute("docker", "cp", hostFilePath, containerId + ":" + containerPath);
            int attempts = 10;
            while (attempts-- > 0) {
                if (execute("docker", "exec", containerId, "test", "-f", containerPath) == 0) {
                    return containerPath;
                }
                Thread.sleep(100);
            }
            LOGGER.warn("Timed out waiting for file to appear inside Docker container: {}", containerPath);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        return hostFilePath;
    }

    private static String findContainer(String match) {
        try {
            Process process = new ProcessBuilder("docker", "ps", "--format", "{{.ID}} {{.Image}} {{.Names}}").start();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (InputStream in = process.getInputStream()) {
                IOUtils.copy(in, buffer);
            }
            process.waitFor();
            String[] lines = buffer.toString(StandardCharsets.UTF_8.name()).split("\n");
            for (String line : lines) {
                if (line.contains(match)) {
                    return line.split(" ")[0];
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Failed to inspect Docker containers: {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.warn("Failed to inspect Docker containers: {}", e.getMessage());
        }
        return null;
    }

    private static int execute(String... command) {
        try {
            Process process = new ProcessBuilder(command).inheritIO().start();
            return process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Command {} interrupted: {}", String.join(" ", command), e.getMessage());
            return -1;
        } catch (IOException e) {
            LOGGER.warn("Command {} failed: {}", String.join(" ", command), e.getMessage());
            return -1;
        }
    }
}
