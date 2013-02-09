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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.staging.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

/**
 * Utility methods for import/export logs of staging databases.
 *
 * @author jaanus
 */
public class ImportExportLogUtil {

    /**
     * Hide utility class constructor.
     */
    private ImportExportLogUtil() {
        // Hide utility class constructor
    }

    /**
     * Formats the given import log for display in the browser.
     * @param log The log.
     * @return The log formatted.
     */
    public static String formatLogForDisplay(String log) {

        StringBuilder sb = new StringBuilder("<div>");
        String[] lines = log.split("\\n");
        if (lines != null) {
            for (int i = 0; i < lines.length; i++) {
                sb.append(formatLogLineForDisplay(lines[i])).append("<br/>");
            }
        }
        return sb.append("</div>").toString();
    }

    /**
     * Formats the given import log line for display in the browser.
     *
     * @param line The log line.
     * @return The log line formatted.
     */
    public static String formatLogLineForDisplay(String line) {

        String start = Level.WARN + ":";
        if (line.startsWith(start)) {
            StringUtils.replaceOnce(line, start, "<span style=\"color:#F5B800\">" + start + "</span>");
        }
        start = Level.ERROR + ":";
        if (line.startsWith(start)) {
            StringUtils.replaceOnce(line, start, "<span style=\"color:#FF0000\">" + start + "</span>");
        }

        return StringUtils.replace(line, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    }
}
