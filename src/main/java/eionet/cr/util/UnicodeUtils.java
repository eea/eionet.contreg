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

import java.util.HashMap;

public final class UnicodeUtils {

    /** */
    private static HashMap<String, String> entityReferenceMap;

    /**
     * Hide utility class constructor.
     */
    private UnicodeUtils() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param entityReference
     * @return
     */
    public static int getEntityReferenceDecimal(String entityReference) {

        String s = getEntityRefrenceMap().get(entityReference);
        return s == null ? -1 : Integer.parseInt(s);
    }

    /**
     * Parses the given string, replaces all occurrences of Unicode entity references with corresponding Java characters (primitive
     * type char) and returns the resulting string.
     *
     * @param literal
     * @return
     */
    public static String replaceEntityReferences(String str) {

        int strLen = str.length();
        StringBuilder budiler = new StringBuilder();
        for (int i = 0; i < strLen; i++) {

            char c = str.charAt(i);

            if (c == '&') {
                int j = str.indexOf(";", i);
                if (j > i) {
                    char cc = str.charAt(i + 1);
                    int decimal = -1;
                    if (cc == '#') {
                        // handle Unicode decimal escape
                        String sDecimal = str.substring(i + 2, j);

                        try {
                            decimal = Integer.parseInt(sDecimal);
                        } catch (Exception e) {
                            // No need to throw or log it.
                        }
                    } else {
                        // handle entity
                        String ent = str.substring(i + 1, j);
                        decimal = UnicodeUtils.getEntityReferenceDecimal(ent);
                    }

                    if (decimal >= 0) {
                        // if decimal was found, use the corresponding char. otherwise stick to c.
                        c = (char) decimal;
                        i = j;
                    }
                }
            }

            budiler.append(c);
        }

        return budiler.toString();
    }

    /**
     *
     * @return
     */
    private static HashMap<String, String> getEntityRefrenceMap() {

        if (entityReferenceMap == null) {

            entityReferenceMap = new HashMap<String, String>();
            entityReferenceMap.put("nbsp", "160");
            entityReferenceMap.put("iexcl", "161");
            entityReferenceMap.put("cent", "162");
            entityReferenceMap.put("pound", "163");
            entityReferenceMap.put("curren", "164");
            entityReferenceMap.put("yen", "165");
            entityReferenceMap.put("brvbar", "166");
            entityReferenceMap.put("sect", "167");
            entityReferenceMap.put("uml", "168");
            entityReferenceMap.put("copy", "169");
            entityReferenceMap.put("ordf", "170");
            entityReferenceMap.put("laquo", "171");
            entityReferenceMap.put("not", "172");
            entityReferenceMap.put("shy", "173");
            entityReferenceMap.put("reg", "174");
            entityReferenceMap.put("macr", "175");
            entityReferenceMap.put("deg", "176");
            entityReferenceMap.put("plusmn", "177");
            entityReferenceMap.put("sup2", "178");
            entityReferenceMap.put("sup3", "179");
            entityReferenceMap.put("acute", "180");
            entityReferenceMap.put("micro", "181");
            entityReferenceMap.put("para", "182");
            entityReferenceMap.put("middot", "183");
            entityReferenceMap.put("cedil", "184");
            entityReferenceMap.put("sup1", "185");
            entityReferenceMap.put("ordm", "186");
            entityReferenceMap.put("raquo", "187");
            entityReferenceMap.put("frac14", "188");
            entityReferenceMap.put("frac12", "189");
            entityReferenceMap.put("frac34", "190");
            entityReferenceMap.put("iquest", "191");
            entityReferenceMap.put("Agrave", "192");
            entityReferenceMap.put("Aacute", "193");
            entityReferenceMap.put("Acirc", "194");
            entityReferenceMap.put("Atilde", "195");
            entityReferenceMap.put("Auml", "196");
            entityReferenceMap.put("Aring", "197");
            entityReferenceMap.put("AElig", "198");
            entityReferenceMap.put("Ccedil", "199");
            entityReferenceMap.put("Egrave", "200");
            entityReferenceMap.put("Eacute", "201");
            entityReferenceMap.put("Ecirc", "202");
            entityReferenceMap.put("Euml", "203");
            entityReferenceMap.put("Igrave", "204");
            entityReferenceMap.put("Iacute", "205");
            entityReferenceMap.put("Icirc", "206");
            entityReferenceMap.put("Iuml", "207");
            entityReferenceMap.put("ETH", "208");
            entityReferenceMap.put("Ntilde", "209");
            entityReferenceMap.put("Ograve", "210");
            entityReferenceMap.put("Oacute", "211");
            entityReferenceMap.put("Ocirc", "212");
            entityReferenceMap.put("Otilde", "213");
            entityReferenceMap.put("Ouml", "214");
            entityReferenceMap.put("times", "215");
            entityReferenceMap.put("Oslash", "216");
            entityReferenceMap.put("Ugrave", "217");
            entityReferenceMap.put("Uacute", "218");
            entityReferenceMap.put("Ucirc", "219");
            entityReferenceMap.put("Uuml", "220");
            entityReferenceMap.put("Yacute", "221");
            entityReferenceMap.put("THORN", "222");
            entityReferenceMap.put("szlig", "223");
            entityReferenceMap.put("agrave", "224");
            entityReferenceMap.put("aacute", "225");
            entityReferenceMap.put("acirc", "226");
            entityReferenceMap.put("atilde", "227");
            entityReferenceMap.put("auml", "228");
            entityReferenceMap.put("aring", "229");
            entityReferenceMap.put("aelig", "230");
            entityReferenceMap.put("ccedil", "231");
            entityReferenceMap.put("egrave", "232");
            entityReferenceMap.put("eacute", "233");
            entityReferenceMap.put("ecirc", "234");
            entityReferenceMap.put("euml", "235");
            entityReferenceMap.put("igrave", "236");
            entityReferenceMap.put("iacute", "237");
            entityReferenceMap.put("icirc", "238");
            entityReferenceMap.put("iuml", "239");
            entityReferenceMap.put("eth", "240");
            entityReferenceMap.put("ntilde", "241");
            entityReferenceMap.put("ograve", "242");
            entityReferenceMap.put("oacute", "243");
            entityReferenceMap.put("ocirc", "244");
            entityReferenceMap.put("otilde", "245");
            entityReferenceMap.put("ouml", "246");
            entityReferenceMap.put("divide", "247");
            entityReferenceMap.put("oslash", "248");
            entityReferenceMap.put("ugrave", "249");
            entityReferenceMap.put("uacute", "250");
            entityReferenceMap.put("ucirc", "251");
            entityReferenceMap.put("uuml", "252");
            entityReferenceMap.put("yacute", "253");
            entityReferenceMap.put("thorn", "254");
            entityReferenceMap.put("yuml", "255");
            entityReferenceMap.put("fnof", "402");
            entityReferenceMap.put("Alpha", "913");
            entityReferenceMap.put("Beta", "914");
            entityReferenceMap.put("Gamma", "915");
            entityReferenceMap.put("Delta", "916");
            entityReferenceMap.put("Epsilon", "917");
            entityReferenceMap.put("Zeta", "918");
            entityReferenceMap.put("Eta", "919");
            entityReferenceMap.put("Theta", "920");
            entityReferenceMap.put("Iota", "921");
            entityReferenceMap.put("Kappa", "922");
            entityReferenceMap.put("Lambda", "923");
            entityReferenceMap.put("Mu", "924");
            entityReferenceMap.put("Nu", "925");
            entityReferenceMap.put("Xi", "926");
            entityReferenceMap.put("Omicron", "927");
            entityReferenceMap.put("Pi", "928");
            entityReferenceMap.put("Rho", "929");
            entityReferenceMap.put("Sigma", "931");
            entityReferenceMap.put("Tau", "932");
            entityReferenceMap.put("Upsilon", "933");
            entityReferenceMap.put("Phi", "934");
            entityReferenceMap.put("Chi", "935");
            entityReferenceMap.put("Psi", "936");
            entityReferenceMap.put("Omega", "937");
            entityReferenceMap.put("alpha", "945");
            entityReferenceMap.put("beta", "946");
            entityReferenceMap.put("gamma", "947");
            entityReferenceMap.put("delta", "948");
            entityReferenceMap.put("epsilon", "949");
            entityReferenceMap.put("zeta", "950");
            entityReferenceMap.put("eta", "951");
            entityReferenceMap.put("theta", "952");
            entityReferenceMap.put("iota", "953");
            entityReferenceMap.put("kappa", "954");
            entityReferenceMap.put("lambda", "955");
            entityReferenceMap.put("mu", "956");
            entityReferenceMap.put("nu", "957");
            entityReferenceMap.put("xi", "958");
            entityReferenceMap.put("omicron", "959");
            entityReferenceMap.put("pi", "960");
            entityReferenceMap.put("rho", "961");
            entityReferenceMap.put("sigmaf", "962");
            entityReferenceMap.put("sigma", "963");
            entityReferenceMap.put("tau", "964");
            entityReferenceMap.put("upsilon", "965");
            entityReferenceMap.put("phi", "966");
            entityReferenceMap.put("chi", "967");
            entityReferenceMap.put("psi", "968");
            entityReferenceMap.put("omega", "969");
            entityReferenceMap.put("thetasy", "977");
            entityReferenceMap.put("upsih", "978");
            entityReferenceMap.put("piv", "982");
            entityReferenceMap.put("bull", "8226");
            entityReferenceMap.put("hellip", "8230");
            entityReferenceMap.put("prime", "8242");
            entityReferenceMap.put("Prime", "8243");
            entityReferenceMap.put("oline", "8254");
            entityReferenceMap.put("frasl", "8260");
            entityReferenceMap.put("weierp", "8472");
            entityReferenceMap.put("image", "8465");
            entityReferenceMap.put("real", "8476");
            entityReferenceMap.put("trade", "8482");
            entityReferenceMap.put("alefsym", "8501");
            entityReferenceMap.put("larr", "8592");
            entityReferenceMap.put("uarr", "8593");
            entityReferenceMap.put("rarr", "8594");
            entityReferenceMap.put("darr", "8595");
            entityReferenceMap.put("harr", "8596");
            entityReferenceMap.put("crarr", "8629");
            entityReferenceMap.put("lArr", "8656");
            entityReferenceMap.put("uArr", "8657");
            entityReferenceMap.put("rArr", "8658");
            entityReferenceMap.put("dArr", "8659");
            entityReferenceMap.put("hArr", "8660");
            entityReferenceMap.put("forall", "8704");
            entityReferenceMap.put("part", "8706");
            entityReferenceMap.put("exist", "8707");
            entityReferenceMap.put("empty", "8709");
            entityReferenceMap.put("nabla", "8711");
            entityReferenceMap.put("isin", "8712");
            entityReferenceMap.put("notin", "8713");
            entityReferenceMap.put("ni", "8715");
            entityReferenceMap.put("prod", "8719");
            entityReferenceMap.put("sum", "8721");
            entityReferenceMap.put("minus", "8722");
            entityReferenceMap.put("lowast", "8727");
            entityReferenceMap.put("radic", "8730");
            entityReferenceMap.put("prop", "8733");
            entityReferenceMap.put("infin", "8734");
            entityReferenceMap.put("ang", "8736");
            entityReferenceMap.put("and", "8743");
            entityReferenceMap.put("or", "8744");
            entityReferenceMap.put("cap", "8745");
            entityReferenceMap.put("cup", "8746");
            entityReferenceMap.put("int", "8747");
            entityReferenceMap.put("there4", "8756");
            entityReferenceMap.put("sim", "8764");
            entityReferenceMap.put("cong", "8773");
            entityReferenceMap.put("asymp", "8776");
            entityReferenceMap.put("ne", "8800");
            entityReferenceMap.put("equiv", "8801");
            entityReferenceMap.put("le", "8804");
            entityReferenceMap.put("ge", "8805");
            entityReferenceMap.put("sub", "8834");
            entityReferenceMap.put("sup", "8835");
            entityReferenceMap.put("nsub", "8836");
            entityReferenceMap.put("sube", "8838");
            entityReferenceMap.put("supe", "8839");
            entityReferenceMap.put("oplus", "8853");
            entityReferenceMap.put("otimes", "8855");
            entityReferenceMap.put("perp", "8869");
            entityReferenceMap.put("sdot", "8901");
            entityReferenceMap.put("lceil", "8968");
            entityReferenceMap.put("rceil", "8969");
            entityReferenceMap.put("lfloor", "8970");
            entityReferenceMap.put("rfloor", "8971");
            entityReferenceMap.put("lang", "9001");
            entityReferenceMap.put("rang", "9002");
            entityReferenceMap.put("loz", "9674");
            entityReferenceMap.put("spades", "9824");
            entityReferenceMap.put("clubs", "9827");
            entityReferenceMap.put("hearts", "9829");
            entityReferenceMap.put("diams", "9830");
            entityReferenceMap.put("quot", "34");
            entityReferenceMap.put("amp", "38");
            entityReferenceMap.put("lt", "60");
            entityReferenceMap.put("gt", "62");
            entityReferenceMap.put("OElig", "338");
            entityReferenceMap.put("oelig", "339");
            entityReferenceMap.put("Scaron", "352");
            entityReferenceMap.put("scaron", "353");
            entityReferenceMap.put("Yuml", "376");
            entityReferenceMap.put("circ", "710");
            entityReferenceMap.put("tilde", "732");
            entityReferenceMap.put("ensp", "8194");
            entityReferenceMap.put("emsp", "8195");
            entityReferenceMap.put("thinsp", "8201");
            entityReferenceMap.put("zwnj", "8204");
            entityReferenceMap.put("zwj", "8205");
            entityReferenceMap.put("lrm", "8206");
            entityReferenceMap.put("rlm", "8207");
            entityReferenceMap.put("ndash", "8211");
            entityReferenceMap.put("mdash", "8212");
            entityReferenceMap.put("lsquo", "8216");
            entityReferenceMap.put("rsquo", "8217");
            entityReferenceMap.put("sbquo", "8218");
            entityReferenceMap.put("ldquo", "8220");
            entityReferenceMap.put("rdquo", "8221");
            entityReferenceMap.put("bdquo", "8222");
            entityReferenceMap.put("dagger", "8224");
            entityReferenceMap.put("Dagger", "8225");
            entityReferenceMap.put("permil", "8240");
            entityReferenceMap.put("lsaquo", "8249");
            entityReferenceMap.put("rsaquo", "8250");
            entityReferenceMap.put("euro", "8364");
        }

        return entityReferenceMap;
    }

}
