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
public class ATriple {

    /** */
    private AResource subject;
    private AResource predicate;
    private String object;
    private String objectLang;
    private boolean litObject;
    private boolean anonObject;

    /**
     *
     */
    private ATriple() {
    }

    /**
     * 
     * @param subject
     * @param predicate
     * @param object
     * @param litObject
     * @return
     */
    public static ATriple create(AResource subject, AResource predicate, String object, boolean litObject) {

        ATriple aTriple = new ATriple();
        aTriple.setSubject(subject);
        aTriple.setPredicate(predicate);
        aTriple.setObject(object);
        aTriple.setLitObject(litObject);
        aTriple.setAnonObject(false);

        return aTriple;
    }

    /**
     * @return the subject
     */
    public AResource getSubject() {
        return subject;
    }

    /**
     * @return the predicate
     */
    public AResource getPredicate() {
        return predicate;
    }

    /**
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * @return the objectLang
     */
    public String getObjectLang() {
        return objectLang;
    }

    /**
     * @return the litObject
     */
    public boolean isLitObject() {
        return litObject;
    }

    /**
     * @return the anonObject
     */
    public boolean isAnonObject() {
        return anonObject;
    }

    /**
     * @param subject the subject to set
     */
    private void setSubject(AResource subject) {
        this.subject = subject;
    }

    /**
     * @param predicate the predicate to set
     */
    private void setPredicate(AResource predicate) {
        this.predicate = predicate;
    }

    /**
     * @param object the object to set
     */
    private void setObject(String object) {
        this.object = object;
    }

    /**
     * @param objectLang the objectLang to set
     */
    private void setObjectLang(String objectLang) {
        this.objectLang = objectLang;
    }

    /**
     * @param litObject the litObject to set
     */
    private void setLitObject(boolean litObject) {
        this.litObject = litObject;
    }

    /**
     * @param anonObject the anonObject to set
     */
    private void setAnonObject(boolean anonObject) {
        this.anonObject = anonObject;
    }
}
