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
package eionet.cr.harvest.util.arp;

import com.hp.hpl.jena.rdf.arp.AResource;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AResourceImpl implements AResource {

    /** */
    private String uri;

    /**
     *
     * @param uri
     */
    public AResourceImpl(String uri) {
        this.uri = uri;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hp.hpl.jena.rdf.arp.AResource#getAnonymousID()
     */
    public String getAnonymousID() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hp.hpl.jena.rdf.arp.AResource#getURI()
     */
    public String getURI() {
        return uri;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hp.hpl.jena.rdf.arp.AResource#getUserData()
     */
    public Object getUserData() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hp.hpl.jena.rdf.arp.AResource#isAnonymous()
     */
    public boolean isAnonymous() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hp.hpl.jena.rdf.arp.AResource#setUserData(java.lang.Object)
     */
    public void setUserData(Object obj) {
    }
}
