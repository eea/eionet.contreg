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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for GeneralConfig methods.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-context-test.xml")
public class GeneralConfigTest extends TestCase {

    @Test
    public void testGetIntPropertyCorrect() {
        int propValue = GeneralConfig.getIntProperty("existing.intproperty", 2000);
        assertTrue(propValue == 1000);
    }

    @Test
    public void testGetIntPropertyNotExisting() {
        int propValue = GeneralConfig.getIntProperty("not.existing.intproperty", 2000);
        assertTrue(propValue == 2000);
    }

    @Test
    public void testGetIntPropertyWrong() {
        int propValue = GeneralConfig.getIntProperty("wrong.intproperty", 55);
        assertTrue(propValue == 55);
    }

    @Test
    public void testTimePropertiesMilliseconds(){

        int ms = 0;

        // should return 60 minutes in milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.minute.ordinary", -1);
        assertEquals(60*60*1000, ms);

        // should return 60 minutes in milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.minute.gaps", -1);
        assertEquals(60*60*1000, ms);

        // should return 60 minutes in milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.minute.capital", -1);
        assertEquals(60*60*1000, ms);

        // should return default value
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.minute.faulty1", -1);
        assertEquals(-1, ms);

        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.minute.faulty2", -1);
        assertEquals(-1, ms);

        // should return 60 sec in milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.second.ordinary", -1);
        assertEquals(60*1000, ms);

        // should return default value
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.second.faulty", -1);
        assertEquals(-1, ms);

        // should return 2h in milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.hour.ordinary", -1);
        assertEquals(2 * 60 * 60 * 1000, ms);

        // should return 2 milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.millisecond.ordinary", -1);
        assertEquals(2, ms);

        // should return 123 milliseconds
        ms = GeneralConfig.getTimePropertyMilliseconds("timeproperty.millisecond.nosuffix", -1);
        assertEquals(123, ms);


    }

    @Test
    public void testTimePropertiesMinutes(){

        int minutes = 0;

        // should return 60 minutes in minutes
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.minute.ordinary", -1);
        assertEquals(60, minutes);

        // should return default value
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.minute.faulty1", -1);
        assertEquals(-1, minutes);

        // should return 1 minute
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.underminute", -1);
        assertEquals(1, minutes);

        // should return 1 minute
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.overminute", -1);
        assertEquals(1, minutes);

        // should return 10 minutes
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.over10minute", -1);
        assertEquals(10, minutes);

        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.notfound", 100);
        assertEquals(100, minutes);

        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.empty", 100);
        assertEquals(100, minutes);

        // Introducing a construction used to initialize a new property instead of old one, but return the old if first not found or default constant.
        minutes = GeneralConfig.getTimePropertyMinutes("timeproperty.millisecond.empty", GeneralConfig.getTimePropertyMinutes("timeproperty.minute.ordinary", -1));
        assertEquals(60, minutes);

    }

}
