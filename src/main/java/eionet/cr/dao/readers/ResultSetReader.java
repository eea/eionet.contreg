package eionet.cr.dao.readers;

import java.util.List;

/**
 * 
 * @author jaanus
 * 
 * @param <T>
 */
public interface ResultSetReader<T> {

    /**
     * 
     * @return
     */
    List<T> getResultList();

    /**
     *
     */
    void endResultSet();
}
