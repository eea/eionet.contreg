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
package eionet.cr.util;

import junit.framework.TestCase;
import eionet.cr.config.GeneralConfig;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URIUtilTest extends TestCase {

    /**
     *
     */
    public void testExtractURILabel() {

        assertEquals(null, URIUtil.extractURILabel("http://sws.geonames.org/6255148/"));
        assertEquals("6255148", URIUtil.extractURILabel("http://sws.geonames.org/6255148"));
        assertEquals("Name", URIUtil.extractURILabel("http://sws.geonames.org/6255148#Name"));
        assertEquals(null, URIUtil.extractURILabel(null));
        assertEquals(null, URIUtil.extractURILabel(""));
        assertEquals(null, URIUtil.extractURILabel(" \t "));
        assertEquals(null, URIUtil.extractURILabel("Name"));

        assertEquals("dflt", URIUtil.extractURILabel("http://sws.geonames.org/6255148/", "dflt"));
        assertEquals(null, URIUtil.extractURILabel("http://sws.geonames.org/6255148/", null));
    }

    /**
     *
     */
    public void testFixRelativeUrl() {
        assertEquals("http://sws.geonames.org", URLUtil.extractUrlHost("http://sws.geonames.org/6255148/"));
    }

    /**
     *
     */
    public void testIsUserHomeUri() {

        String appHomeUrl = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        assertTrue(URIUtil.isUserHomeUri(appHomeUrl + "/home/heinlja"));
        assertTrue(URIUtil.isUserHomeUri(appHomeUrl + "/home/heinlja/"));
        assertTrue(URIUtil.isUserHomeUri(appHomeUrl + "/home/heinlja/folder"));
        assertFalse(URIUtil.isUserHomeUri(appHomeUrl + "/home/"));
        assertFalse(URIUtil.isUserHomeUri(appHomeUrl + "/home"));
        assertFalse(URIUtil.isUserHomeUri(appHomeUrl));
        assertFalse(URIUtil.isUserHomeUri(""));
        assertFalse(URIUtil.isUserHomeUri(" "));
        try{
            assertFalse(URIUtil.isUserHomeUri(null));
        }
        catch (NullPointerException e){
            fail("Wasn't expecting this exception: " + e.toString());
        }
    }

    /**
     *
     */
    public void testExtarctUserName(){

        try{
            assertEquals(null, URIUtil.extractUserName(null));
        }
        catch (NullPointerException e){
            fail("Wasn't expecting this exception: " + e.toString());
        }

        assertEquals(null, URIUtil.extractUserName(""));
        assertEquals(null, URIUtil.extractUserName(" "));

        String appHomeUrl = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        assertEquals(null, URIUtil.extractUserName(appHomeUrl));
        assertEquals(null, URIUtil.extractUserName(appHomeUrl + "/home"));
        assertEquals(null, URIUtil.extractUserName(appHomeUrl + "/home/"));
        assertEquals(null, URIUtil.extractUserName(appHomeUrl + "/heinlja"));
        assertEquals(null, URIUtil.extractUserName(appHomeUrl + "/heinlja/"));
        assertEquals("heinlja", URIUtil.extractUserName(appHomeUrl + "/home/heinlja"));
        assertEquals("heinlja", URIUtil.extractUserName(appHomeUrl + "/home/heinlja/"));
        assertEquals("heinlja", URIUtil.extractUserName(appHomeUrl + "/home/heinlja/aaa"));
    }

    /**
     *
     */
    public void testIsUserReservedUri(){

        try{
            assertFalse(URIUtil.isUserReservedUri(null));
            assertFalse(URIUtil.isUserReservedUri(""));
            assertFalse(URIUtil.isUserReservedUri(" "));

            String appHomeUrl = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
            assertTrue(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/reviews"));
            assertTrue(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/bookmarks"));
            assertTrue(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/history"));
            assertTrue(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/registrations"));
            assertFalse(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/some"));
            assertFalse(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja/"));
            assertFalse(URIUtil.isUserReservedUri(appHomeUrl + "/home/heinlja"));
            assertFalse(URIUtil.isUserReservedUri(appHomeUrl + "/home/"));
        }
        catch (NullPointerException e){
            fail("Wasn't expecting this exception: " + e.toString());
        }
    }
}
