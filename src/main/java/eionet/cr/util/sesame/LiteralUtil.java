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

package eionet.cr.util.sesame;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;

import eionet.cr.util.Util;

/**
 * Utility functions for operating with objects of {@link Literal} class.
 *
 * @author Jaanus Heinlaid
 */
public final class LiteralUtil {

    /**
     * Hide utility class constructor.
     */
    private LiteralUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Null-safe method that gets the given literal's {@link Date} value. Null-safe means that it will simply return null if the
     * given literal is null. Null is also returned if the literal could not be parsed into {@link Date}.
     *
     * The method first tries to get the date value by trying {@link Literal#calendarValue()}. If that fails, the method tries to
     * parse the literal's string value with {@link eionet.cr.harvest.BaseHarvest#DATE_FORMATTER}. If the fails too, null is
     * returned.
     *
     * @param literal The literal whose date value is returned.
     * @return The date value of the given literal.
     */
    public static Date getDateValue(Literal literal) {

        if (literal == null) {
            return null;
        }

        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            xmlGregorianCalendar = literal.calendarValue();
        } catch (Exception e) {
            xmlGregorianCalendar = null;
        }

        if (xmlGregorianCalendar != null) {
            return xmlGregorianCalendar.toGregorianCalendar().getTime();
        }

        return Util.virtuosoStringToDate(literal.stringValue());
    }
}
