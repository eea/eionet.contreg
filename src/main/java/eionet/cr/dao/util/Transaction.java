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
package eionet.cr.dao.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.util.sql.DbConnectionProvider;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Transaction {

    /** */
    protected Logger logger = Logger.getLogger(Transaction.class);

    /** */
    private Connection conn;

    /**
     *
     */
    private Transaction(Connection conn) {

        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null!");
        }
        this.conn = conn;
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public static Transaction begin() throws DAOException{

        try{
            return new Transaction(DbConnectionProvider.getConnection());
        }
        catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        }
    }

    /**
     *
     */
    public void commit() {

        try{
            conn.commit();
        }
        catch (SQLException e) {
            logger.error("Transaction commit failed: " + e.toString());
        }
    }

    /**
     *
     */
    public void rollback() {

        try{
            conn.rollback();
        }
        catch (SQLException e) {
            logger.error("Transaction rollback failed: " + e.toString());
        }
    }

    /**
     *
     */
    public static void rollback(Transaction transaction) {

        if (transaction!=null) {
            transaction.rollback();
        }
    }

    /**
     *
     * @param <T>
     * @param implementedInterface
     * @return
     */
    public <T extends DAO> T getDao(Class<T> implementedInterface) {

        T dao = DAOFactory.get().getDao(implementedInterface);
//      dao.setConnection(conn);
        return dao;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        Transaction transaction = null;;
        try{
            transaction = Transaction.begin();
            transaction.getDao(HelperDAO.class).addTriples(null);
            // do some DAO operations here
        }
        catch (DAOException e) {
            Transaction.rollback(transaction);
        }
    }
}
