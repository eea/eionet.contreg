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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class Pair<L, R> implements Serializable {

    /** */
    private static final String[] EXCLUDE_FROM_EQUALS = new String[] {"right"};

    /**
     * serial.
     */
    private static final long serialVersionUID = 1L;

    /** */
    private L left;
    private R right;

    /**
     * @param left
     * @param right
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return the left
     */
    public L getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(L id) {
        this.left = id;
    }

    /**
     * @return the right
     */
    public R getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(R value) {
        this.right = value;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDE_FROM_EQUALS);
    }
}
