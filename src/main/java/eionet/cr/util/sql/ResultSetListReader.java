package eionet.cr.util.sql;

import java.util.List;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public abstract class ResultSetListReader<T> extends ResultSetBaseReader {
	
	public abstract List<T> getResultList();
}
