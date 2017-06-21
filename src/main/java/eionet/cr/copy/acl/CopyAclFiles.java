package eionet.cr.copy.acl;

import eionet.cr.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 *
 * @author Thanos Tourikas
 *
 */
public class CopyAclFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyAclFiles.class.getName());
    private ConfigurationPropertyResolver propertyResolver = (ConfigurationPropertyResolver) SpringApplicationContext.getBean("configurationPropertyResolver");

    /**
     * Copy acl files at startup.
     * @throws IOException - If the file copy fails.
     * @throws URISyntaxException - If the file URL is wrong.
     */
    public CopyAclFiles() throws IOException, URISyntaxException {
        String appHome = null;
        try {
            appHome = propertyResolver.resolveValue("app.home");
            copyFiles(appHome);
        } catch (UnresolvedPropertyException e) {
            e.printStackTrace();
        } catch (CircularReferenceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy files to target location.
     * @param target - Target location
     * @throws URISyntaxException - If the file URL is wrong.
     * @throws IOException - If the file copy fails.
     */
    private void copyFiles(String target) throws URISyntaxException, IOException {
        target = target + "/acl/";
        URL sourceURL = this.getClass().getClassLoader().getResource("acl/");
        
        File sourceFolder = new File(sourceURL.toURI());
        
        File[] files = sourceFolder.listFiles();
            for (File file:files){
                if (file.getName().contains(".prms") || file.getName().contains(".permissions")  || !((new File(target + (file.getName())).exists()))) {
                    copyFile(file.toURI().toURL(), target + (file.getName()));
                }
            }
    }

    /**
     * Copy file to target location
     * @param source source url
     * @param target target location
     * @throws IOException - If the file copy fails.
     * @throws URISyntaxException - If the file URL is wrong.
     */
    private void copyFile(URL source, String target) throws IOException, URISyntaxException {
        File sourceFile = new File(source.toURI());
        File targetDirectory = new File(target);
        org.apache.commons.io.FileUtils.copyFile(sourceFile, targetDirectory);
        LOGGER.info("Successfully copied file...{0}", target);
    }
}
 

    


