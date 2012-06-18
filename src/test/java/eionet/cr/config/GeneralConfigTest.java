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
 *		kaido
 */

package eionet.cr.config;

import junit.framework.TestCase;

/**
 * Test for GeneralConfig methods.
 */

public class GeneralConfigTest extends TestCase {

    public static void testGetIntPropertyCorrect() {
        int propValue = GeneralConfig.getIntProperty("existing.intproperty", 2000);
        assertTrue(propValue == 1000);
    }

    public static void testGetIntPropertyNotExisting() {
        int propValue = GeneralConfig.getIntProperty("not.existing.intproperty", 2000);
        assertTrue(propValue == 2000);
    }

    public static void testGetIntPropertyWrong() {
        int propValue = GeneralConfig.getIntProperty("wrong.intproperty", 55);
        assertTrue(propValue == 55);
    }
}
