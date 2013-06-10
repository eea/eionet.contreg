package eionet.cr.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.URIUtil;

/**
 * A linked hash-map of prefix-to-namespace pairs that could be useful at SPARQL query entrance in CR web forms.
 * Loaded from {@value #PROPERTIES_FILE}.
 * Using {@link LinkedHashMap} and line-by-line reading to preserve the order of the pairs in the file.
 * 
 * It is a singleton implemented by the Bill Pugh pattern:
 * http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
 * 
 * @author jaanus
 */
public class UsefulNamespaces extends LinkedHashMap<String, String> {

    /** Auto-generated serial version. */
    private static final long serialVersionUID = 3867281673204174828L;

    /** The name of the properties-file to load from. */
    public static final String PROPERTIES_FILE = "useful-namespaces.properties";

    /** A static Log4j logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(UsefulNamespaces.class);

    /**
     * Private constructor that loads the values.
     */
    private UsefulNamespaces() {
        super();
        load();
    }

    /**
     * Loads the useful namespaces from {@link #PROPERTIES_FILE}, in exactly the same order as they appear in the file.
     */
    private void load() {

        InputStream inputStream = null;
        try {
            inputStream = GeneralConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            List<String> lines = IOUtils.readLines(inputStream);
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (StringUtils.isNotBlank(trimmedLine) && trimmedLine.charAt(0) != '#') {
                    int index = trimmedLine.indexOf('=');
                    if (index > 0 && index < trimmedLine.length() - 1) {
                        String prefix = trimmedLine.substring(0, index).trim();
                        if (StringUtils.isNotBlank(prefix)) {
                            String uri = trimmedLine.substring(index + 1).trim();
                            if (URIUtil.isURI(uri)) {
                                put(prefix, uri);
                            } else {
                                LOGGER.warn("Invalid URI for the \"" + prefix + "\" namespace in " + PROPERTIES_FILE);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new CRRuntimeException("Failed to load " + PROPERTIES_FILE, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Public accessor for the singleton instance.
     * 
     * @return The singleton instance.
     */
    public static UsefulNamespaces getInstance() {
        return UsefulNamespacesHolder.INSTANCE;
    }

    /**
     * {@link UsefulNamespacesHolder} is loaded on the first execution of {@link UsefulNamespaces#getInstance()}.
     * or the first access to {@link #INSTANCE}, not before.
     * 
     * See singleton pattern by Bill Hugh at http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh.
     */
    private static class UsefulNamespacesHolder {
        public static final UsefulNamespaces INSTANCE = new UsefulNamespaces();
    }
}
