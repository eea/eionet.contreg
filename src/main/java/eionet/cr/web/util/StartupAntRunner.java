package eionet.cr.web.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.listener.Log4jListener;

import eionet.cr.common.CRRuntimeException;

/**
 * A servlet context startup listener that executes a bundled Ant build file that prepares various runtime resourecs that
 * the webapp needs.
 *
 * @author Jaanus
 */
public class StartupAntRunner implements ServletContextListener {

    /** Private static logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(StartupAntRunner.class);

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {

        // Locate Ant build file.

        URL fileURL = getClass().getClassLoader().getResource("build.xml");
        File buildFile = null;
        try {
            buildFile = new File(fileURL.toURI());
        } catch (URISyntaxException e) {
            LOGGER.error(e.getClass().getSimpleName() + " when converting URI from this URL: " + fileURL);
        }

        if (buildFile == null || !buildFile.exists() || !buildFile.isFile()) {
            CRRuntimeException e = new CRRuntimeException("Uanble to find such a build file: " + buildFile);
            throw e;
        }

        String buildFileAbsolutePath = buildFile.getAbsolutePath();
        LOGGER.debug("Absolute path of Ant build file: " + buildFileAbsolutePath);

        // Set up Ant project with above-found build file.

        Project project = new Project();
        project.setBaseDir(buildFile.getParentFile());
        project.addBuildListener(new Log4jListener());
        project.setUserProperty("ant.file", buildFileAbsolutePath);
        project.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        helper.parse(project, buildFile);

        // Execute the project's default target.

        String defaultTarget = project.getDefaultTarget();
        LOGGER.debug("Executing Ant target \"" + defaultTarget + "\" in " + buildFileAbsolutePath);
        project.executeTarget(defaultTarget);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        // Nothing to do here.
    }
}
