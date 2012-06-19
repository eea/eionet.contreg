/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import eionet.cr.common.CRRuntimeException;

/**
 * Utility class for generating various hashes used in CR.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class Hashes {

    /** */
    private static final long SEED = 0xcbf29ce484222325L;

    /**
     * Hide utility class constructor.
     */
    private Hashes() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param s
     * @return long
     */
    public static long spoHash(String s) {
        return Hashes.fnv64(s);
    }

    /**
     *
     * @param s
     * @param seed
     * @return long
     */
    public static long spoHash(String s, long seed) {
        return Hashes.fnv64(s, seed);
    }

    /**
     *
     * @param s
     * @return
     */
    private static long fnv64(String s) {
        return Hashes.fnv64(s, SEED);
    }

    /**
     *
     * @param s
     * @return
     */
    private static long fnv64(String s, long seed) {

        int sLen = s.length();
        for (int i = 0; i < sLen; i++) {
            seed ^= s.charAt(i);
            seed += (seed << 1) + (seed << 4) + (seed << 5) + (seed << 7) + (seed << 8) + (seed << 40);
        }

        return seed;
    }

    /**
     *
     * @param s
     * @return String
     */
    public static String md5(String s) {
        return Hashes.digest(s, "md5");
    }

    /**
     *
     * @param src
     * @param algorithm
     * @return String
     */
    public static String digest(String src, String algorithm) {

        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (GeneralSecurityException e) {
            throw new CRRuntimeException(e.toString(), e);
        }
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < dstBytes.length; i++) {
            Byte byteWrapper = new Byte(dstBytes[i]);
            String s = Integer.toHexString(byteWrapper.intValue());
            if (s.length() == 1)
                s = "0" + s;
            buf.append(s.substring(s.length() - 2));
        }

        return buf.toString();
    }

    /**
     * Just used for getting an spoHash() of a string and printing it in System.out.
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 0) {
            for (String s : args) {
                System.out.println(s + " = " + Hashes.spoHash(s));
            }
        } else {
            System.out.println(spoHash("http://www.gutenberg.org/feeds/catalog.rdf"));
        }
    }
}
