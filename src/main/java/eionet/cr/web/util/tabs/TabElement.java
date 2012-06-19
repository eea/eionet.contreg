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
 *        Juhan Voolaid
 */

package eionet.cr.web.util.tabs;

import java.util.HashMap;
import java.util.Map;

/**
 * Tab formbean in tab menu.
 *
 * @author Juhan Voolaid
 */
public class TabElement {

    /** Title. */
    private String title;

    /** Link's url whithout context root. */
    private String href;

    /** Stripes event. */
    private String event;

    /** Request parameters. */
    private Map<String, Object> params = new HashMap<String, Object>();

    /** True, if current tab element is selected. */
    private boolean selected;

    /**
     * Class constructor.
     *
     * @param title
     * @param href
     * @param selectedTitle
     */
    public TabElement(String title, String href, String selectedTitle) {
        this.title = title;
        this.href = href;
        selected = selectedTitle == null ? false : title.equals(selectedTitle);
    }

    /**
     * Adds request parameter.
     *
     * @param key
     * @param value
     */
    public void addParam(String key, Object value) {
        params.put(key, value);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the params
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the event
     */
    public String getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(String event) {
        this.event = event;
    }
}
