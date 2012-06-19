package eionet.cr.web.sparqlClient.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public final class SPARQLEndpoints extends ArrayList<String> {

    /** */
    private static final String FILENAME = "endpoints.xml";

    /** */
    private static Log logger = LogFactory.getLog(SPARQLEndpoints.class);

    /** */
    private static SPARQLEndpoints instance;
    private static Object lock = new Object();

    /**
     *
     */
    private SPARQLEndpoints() {

        super();
        loadFromProperties();
        Collections.sort(this);
    }

    /**
     *
     */
    private void loadFromProperties() {

        InputStream inputStream = null;
        try {
            inputStream = SPARQLEndpoints.class.getClassLoader().getResourceAsStream(FILENAME);
            Properties properties = new Properties();
            properties.loadFromXML(inputStream);

            for (Object key : properties.keySet()) {
                this.add(key.toString());
            }
        } catch (InvalidPropertiesFormatException e) {
            logger.error("Failed to load endpoints from " + FILENAME, e);
        } catch (IOException e) {
            logger.error("Failed to load endpoints from " + FILENAME, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * @return
     */
    public static SPARQLEndpoints getInstance() {

        if (instance == null) {

            synchronized (lock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (instance == null) {
                    instance = new SPARQLEndpoints();
                }
            }
        }

        return instance;
    }
}
