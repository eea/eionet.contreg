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
import org.slf4j.LoggerFactory;

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
public class UsefulNamespaces extends OrderedProperties {

    /** Auto-generated serial version. */
    private static final long serialVersionUID = 3867281673204174828L;

    /** The name of the properties-file to load from. */
    public static final String PROPERTIES_FILE = "useful-namespaces.properties";

    /** A static Log4j logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UsefulNamespaces.class);

    /**
     * Private constructor that loads the values.
     */
    private UsefulNamespaces() {
        super(PROPERTIES_FILE, LOGGER, true);
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
