package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import eionet.cr.util.sql.ParameterizedSQL;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author heinljab
 *
 */
public abstract class MySQLBaseDAO {

	/**
	 * 
	 * @return
	 */
	protected Connection getConnection(){
		return MySQLDAOFactory.getConnection();
	}
}
