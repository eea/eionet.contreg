package eionet.cr.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.URIUtil;
import org.slf4j.Logger;

/**
 *
 * A customized properties file loader for ordered resultset.
 *
 * @author Jaak
 * @author Jaanus
 */
public abstract class OrderedProperties extends LinkedHashMap<String, String> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ordered properties.
     *
     * @param propertiesFile the properties file
     * @param logger the logger
     */
    protected OrderedProperties(String propertiesFile, Logger logger) {
        super();
        load(propertiesFile, logger, false);
    }

    /**
     * Instantiates a new ordered properties.
     *
     * @param propertiesFile the properties file
     * @param logger the logger
     * @param testIfvalueIsUri the test ifvalue is uri
     */
    protected OrderedProperties(String propertiesFile, Logger logger, boolean testIfvalueIsUri) {
        super();
        load(propertiesFile, logger, testIfvalueIsUri);
    }

    /**
     * Loads the useful namespaces from {@link #PROPERTIES_FILE}, in exactly the same order as they appear in the file.
     *
     * @param propertiesFile the properties file
     * @param logger the logger
     * @param testIfvalueIsUri the test ifvalue is uri
     */
    private void load(String propertiesFile, Logger logger, boolean testIfvalueIsUri) {

        InputStream inputStream = null;
        try {
            inputStream = GeneralConfig.class.getClassLoader().getResourceAsStream(propertiesFile);
            List<String> lines = IOUtils.readLines(inputStream);
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (StringUtils.isNotBlank(trimmedLine) && trimmedLine.charAt(0) != '#') {
                    int index = trimmedLine.indexOf('=');
                    if (index > 0 && index < trimmedLine.length() - 1) {
                        String key = trimmedLine.substring(0, index).trim();
                        if (StringUtils.isNotBlank(key)) {
                            String value = trimmedLine.substring(index + 1).trim();
                            if (testIfvalueIsUri) {
                                if (URIUtil.isURI(value)) {
                                    put(key, value);
                                } else {
                                    logger.warn("Invalid URI for the \"" + key + "\" namespace in " + propertiesFile);
                                }
                            } else {
                                put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new CRRuntimeException("Failed to load " + propertiesFile, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
