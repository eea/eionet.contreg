package eionet.cr.util;

import java.util.HashMap;

import eionet.cr.common.Namespace;

public final class NamespaceUtil {

    /**
     * Hide utility class constructor.
     */
    private NamespaceUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param url
     * @return
     */
    public static String extractNamespace(String url) {

        if (url == null)
            return null;

        int i = url.lastIndexOf("#");
        if (i < 0) {
            i = url.lastIndexOf("/");
        }

        return i < 0 ? null : url.substring(0, i + 1);
    }

    /**
     *
     * @param url
     * @return
     */
    public static String extractLocalName(String url) {
        if (url == null)
            return null;

        int i = url.lastIndexOf("#");
        if (i < 0) {
            i = url.lastIndexOf("/");
        }

        return i < 0 ? null : url.substring(i + 1, url.length());
    }

    /**
     *
     * @param url
     * @param prefix
     */
    public static void addNamespace(HashMap<Long, String> namespaces, Namespace namespace) {
        namespaces.put(Hashes.spoHash(namespace.getUri()), namespace.getPrefix());
    }

    /**
     *
     * @param namespace
     * @return
     */
    public static String getKnownNamespace(String namespace) {

        Namespace[] knownNamespaces = Namespace.values();

        for (Namespace singleNamespace : knownNamespaces) {
            if (singleNamespace.getUri().equals(namespace)) {
                return singleNamespace.getPrefix();
            }
        }

        return null;
    }
}
