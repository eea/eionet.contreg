package eionet.cr.util.sql;

import java.util.StringTokenizer;

/**
 * 
 * @author heinljab
 *
 */
public class ParameterizedSQL {

	/** */
	private String sqlString;
	private String[] paramNames;

	/**
	 * 
	 * @param sqlString
	 * @param paramNames
	 */
	public ParameterizedSQL(String sqlString, String paramNames){
		this.sqlString = sqlString;
		this.paramNames = paramNames.split(",");
		for (int i=0; i<this.paramNames.length; i++)
			this.paramNames[i] = this.paramNames[i].trim();
	}

	/**
	 * @return the sqlString
	 */
	public String getSqlString() {
		return sqlString;
	}

	/**
	 * @return the paramNames
	 */
	public String[] getParamNames() {
		return paramNames;
	}
}
