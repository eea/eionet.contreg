/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.web.util;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class Colors {

    /**
     * Hide utility class constructor.
     */
    private Colors() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param hash
     * @return
     */
    public static String colorByModulus(long hash) {

        int index = Math.abs((int) (hash % COLORS.length));
        index = Math.max(0, index);
        index = Math.min(COLORS.length - 1, index);
        return COLORS[index];
    }

    /**
     *
     * @param rgbColor
     * @return
     */
    public static String toKML(String rgbColor, boolean transparent) {
        return new StringBuffer(transparent ? "00" : "ff").append(rgbColor.substring(4, 6)).append(rgbColor.substring(2, 4))
        .append(rgbColor.substring(0, 2)).toString();
    }

    /** */
    public static final String[] COLORS = {"eee8cd", "d8bfd8", "40e0d0", "cdb38b", "cae1ff", "cdb5cd", "f5f5f5", "ee9a49",
        "1e90ff", "ff00ff", "556b2f", "cdc9c9", "e0eeee", "8b8989", "cd5c5c", "ff83fa", "76ee00", "8a2be2", "8deeee",
        "c1ffc1", "8b3a3a", "eee5de", "3cb371", "8b5742", "6b8e23", "cd7054", "e6e6fa", "7ccd7c", "cd0000", "cd69c9",
        "8ee5ee", "cd3333", "eeee00", "fffafa", "ff6eb4", "cd6839", "c1cdc1", "ee0000", "3a5fcd", "98f5ff", "adff2f",
        "458b74", "ffd39b", "eedc82", "ff8c69", "bcd2ee", "ffaeb9", "9400d3", "8b7e66", "9acd32", "eee9e9", "838b8b",
        "c1cdcd", "ffdead", "f4a460", "8b6914", "ff7f00", "eeb4b4", "8b4513", "8b4726", "fff5ee", "cd3278", "ffb6c1",
        "fff68f", "cd5555", "eed8ae", "20b2aa", "bfefff", "ffe1ff", "ff6a6a", "e066ff", "8b6969", "00e5ee", "cdc9a5",
        "eeeed1", "9ac0cd", "98fb98", "cdaf95", "1c86ee", "cdaa7d", "eed2ee", "fafad2", "ffe4b5", "eead0e", "7d26cd",
        "8b8386", "79cdcd", "7cfc00", "cd8162", "eecfa1", "008b45", "ff6347", "ab82ff", "b9d3ee", "90ee90", "b4cdcd",
        "548b54", "00ee76", "7fff00", "5f9ea0", "8b5a00", "8b8378", "fffacd", "b3ee3a", "f0fff0", "ff8247", "9b30ff",
        "ff0000", "ee6aa7", "f0f8ff", "8b8682", "ee4000", "db7093", "8b5a2b", "8b8b83", "6495ed", "cd9b1d", "ffa500",
        "cd2990", "cd6600", "ee7942", "00cd66", "ff7256", "ee7ae9", "97ffff", "c71585", "4f94cd", "8b864e", "fdf5e6",
        "698b22", "8b8970", "8b4500", "8b3a62", "ee00ee", "b23aee", "cdad00", "ffe4e1", "8b4789", "2e8b57", "d2b48c",
        "68838b", "66cdaa", "ee9a00", "008b8b", "eed5d2", "cdba96", "cdc5bf", "836fff", "d15fee", "4876ff", "ffefdb",
        "698b69", "8b475d", "fffaf0", "b0e2ff", "e0ffff", "ffebcd", "a2cd5a", "aeeeee", "00eeee", "ff34b3", "76eec6",
        "48d1cc", "cd5b45", "668b8b", "8b636c", "ee3a8c", "ffa54f", "cd6090", "9370db", "9aff9a", "bc8f8f", "b03060",
        "9a32cd", "cd9b9b", "7fffd4", "7a8b8b", "cd919e", "eedfcc", "ffd700", "ff69b4", "eedd82", "63b8ff", "fff8dc",
        "cdc8b1", "f0e68c", "8fbc8f", "eea2ad", "7a67ee", "a9a9a9", "ff8c00", "eec591", "32cd32", "cdbe70", "8b795e",
        "8b8b00", "00b2ee", "8b668b", "8b4c39", "8b7500", "8b8b7a", "009acd", "cd8c95", "a4d3ee", "696969", "b8860b",
        "5cacee", "ffff00", "eeaeee", "ee1289", "cd6889", "cdc673", "87ceeb", "f08080", "ff7f24", "ffc0cb", "d1eeee",
        "faf0e6", "607b8b", "00c5cd", "f5fffa", "00fa9a", "8b6508", "cd3700", "cdcdb4", "8b7765", "daa520", "a0522d",
        "ffc125", "912cee", "53868b", "b2dfee", "ffec8b", "00ced1", "ee6363", "f8f8ff", "cd853f", "8b7355", "ee3b3b",
        "cd950c", "00ee00", "ee9572", "00bfff", "ff82ab", "b22222", "8b3626", "228b22", "afeeee", "7a378b", "cd4f39",
        "ff7f50", "cdcd00", "a020f0", "528b8b", "a52a2a", "eee9bf", "ee5c42", "ff4040", "00cdcd", "add8e6", "ff1493",
        "778899", "f0ffff", "8b7b8b", "d3d3d3", "1874cd", "ffe7ba", "fa8072", "ee8262", "cdc1c5", "66cd00", "00ff7f",
        "8b2323", "cd8500", "d2691e", "9932cc", "eeb422", "8470ff", "8b814c", "e9967a", "8b5f65", "ee30a7", "6a5acd",
        "c6e2ff", "708090", "5d478b", "54ff9f", "bdb76b", "9bcd9b", "838b83", "4eee94", "96cdcd", "ffa07a", "eeeee0",
        "cdc0b0", "eee685", "ee799f", "deb887", "00ff00", "b4eeb4", "ee7600", "ffb5c5", "cdb7b5", "ffbbff", "ffc1c1",
        "b452cd", "a2b5cd", "473c8b", "00ffff", "eee8aa", "ffe4c4", "d02090", "4a708b", "8b8878", "6ca6cd", "ba55d3",
        "bbffff", "7b68ee", "4682b4", "ee7621", "bf3eff", "9fb6cd", "b0e0e6", "6e8b3d", "00cd00", "cd00cd", "eee0e5",
        "cd1076", "cd2626", "cd96cd", "b0c4de", "fff0f5", "ee82ee", "7ec0ee", "9f79ee", "8968cd", "ff4500", "eecbad",
        "eea9b8", "eec900", "8db6cd", "87cefa", "ff3030", "ff3e96", "ffffe0", "ffdab9", "cdcdc1", "ee2c2c", "dcdcdc",
        "eed5b7", "dda0dd", "458b00", "f5f5dc", "c0ff3e", "f5deb3", "faebd7", "e0eee0", "6e7b8b", "43cd80", "bcee68",
        "caff70", "8b7d7b", "cd661d", "6959cd", "8b3e2f", "00f5ff", "7ac5cd", "cdb79e", "ffb90f", "fffff0", "8b7d6b",
        "da70d6", "6c7b8b", "ee6a50", "4169e1"};

}
