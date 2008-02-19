package eionet.cr.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * 
 * @author altnyris
 *
 */
public class GeneralConfig {

	private ResourceBundle props;
	  
	private final String PROP_FILE = "cr";
	
	/**
	* DB connection URL for DB connection
	*/
	public static final String DB_URL = "db.url";


	/**
	* DB driver
	*/
	public static final String DB_DRV = "db.drv";

    /**
	* DB user id for the extractor
	*/
	public static final String DB_USER_ID = "db.usr";

	/**
	* User PWD  for DB connection
	*/
	public static final String DB_USER_PWD = "db.pwd";
	
	/**
	* DataSource name
	*/
	public static final String JDBC_NAME = "jdbc.name";

	/** Creates new GeneralConfig */
	  public GeneralConfig() throws Exception {
	     try {
	        props = ResourceBundle.getBundle(PROP_FILE);
	     } catch (MissingResourceException mre) {
	       throw new Exception("Properties file " + PROP_FILE + ".properties not found");
	     }

	  }
	  
	  /**
	  *
	  */
	   public String getStringProperty(String propName) throws Exception {
	     try {
	        return props.getString(propName);
	     } catch (MissingResourceException mre) {
	        throw new Exception("Property value for key " + propName + " not found");
	     }
	   }

	 /**
	  *
	  */
	   public boolean getBooleanProperty(String propName) throws Exception {
	     try {
	        String s = props.getString(propName);
	        return Boolean.valueOf(s).booleanValue();
	     } catch (MissingResourceException mre) {
	        throw new Exception("Property value for key " + propName + " not found");
	     }
	   }

	 /**
	  *
	  */
	   public int getIntProperty(String propName) throws Exception {
	     try {
	       String s = props.getString(propName);
	       return Integer.parseInt(s);
	     } catch (MissingResourceException mre) {
	        throw new Exception("Property value for key " + propName + " not found");
	     } catch (NumberFormatException nfe) {
	        throw new Exception("Invalid value for integer property " + propName);
	     }
	   }

	   /**
	   *
	   */
	    public String[] getStringArrayProperty(String propName, String separator) throws Exception {
	      try {
	     	 String[] str= null;
	     	 String s = props.getString(propName);

	     	 if (separator == null || separator.length()==0){
	     		 str = new String[1];
	     		 str[0] = s;
	     	 }
	     	 else{
	     		 char c = separator.charAt(0);
	     		 String sep = Character.isLetterOrDigit(c) ? Character.toString(c) : "\\" + c;
	         	 str =s.split(sep);
	     	 }
	         return str;
	      } catch (MissingResourceException mre) {
	         throw new Exception("Property value for key " + propName + " not found");
	      }
	    }
}
