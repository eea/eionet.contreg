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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest;

/**
 *
 * @author Jaanus Heinlaid
 */
public final class ResponseCodeUtil {

    /**
     * Hide utility class constructor.
     */
    private ResponseCodeUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param code
     * @return
     */
    public static boolean isRedirect(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307;
    }

    /**
     *
     * @param code
     * @return
     */
    public static boolean isPermanentError(int code) {
        return code == 400 || (code >= 402 && code <= 407) || (code >= 409 && code <= 417) || code == 501 || code == 505;
    }

    /**
     *
     * @param code
     * @return
     */
    public static boolean isError(int code) {
        return code >= 400 && code <= 599;
    }

    /**
     *
     * @param code
     * @return
     */
    public static boolean isNotModified(int code) {
        return code == 304;
    }

}
