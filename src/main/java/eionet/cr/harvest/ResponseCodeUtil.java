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
public class ResponseCodeUtil {

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
    public static boolean isTemporaryError(int code) {
        return code == 401 || code == 408 || code == 500 || (code >= 502 && code <= 504);
    }

    /**
     * 
     * @param code
     * @return
     */
    public static boolean isError(int code) {
        return isTemporaryError(code) || isPermanentError(code);
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
