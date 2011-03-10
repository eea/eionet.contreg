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
package eionet.cr.dao.mysql;


import eionet.cr.dao.DAOException;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dto.SpoBinaryDTO;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class MySQLSpoBinaryDAO extends MySQLBaseDAO implements SpoBinaryDAO{

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#add(eionet.cr.dto.SpoBinaryDTO, long)
     */
    public void add(SpoBinaryDTO dto, long contentSize) throws DAOException{
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#get(java.lang.String)
     */
    public SpoBinaryDTO get(String subjectUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#exists(java.lang.String)
     */
    public boolean exists(String subjectUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
