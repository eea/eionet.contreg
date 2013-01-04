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
package eionet.cr.harvest.scheduled;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
class HourSpan {

    /** */
    private int from;
    private int to;

    /**
     *
     * @param from
     * @param to
     */
    HourSpan(int from, int to) {
        this.from = from;
        this.to = to;
    }

    /**
     * @return the from
     */
    public int getFrom() {
        return from;
    }

    /**
     * @return the to
     */
    public int getTo() {
        return to;
    }

    /**
     *
     * @return
     */
    public int length() {
        // Creator responsible for ensuring that to >= from
        return to - from;
    }

    /**
     *
     * @param hour
     * @return
     */
    public boolean includes(int hour) {
        return hour >= from && hour < to;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuffer().append(from).append("-").append(to).toString();
    }
}
