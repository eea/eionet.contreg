package eionet.cr.web.sparqlClient.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a LinkedHashMap for a simple caching system
 * @author George Sofianos
 */
public class QueryCachedMap<K,V> extends LinkedHashMap<K,V> {

  /* Maximum map size */
  private final int MAX_CACHE_SIZE = 10;

  /* returns true if size larger than maximum map size */
  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    if (super.size() > MAX_CACHE_SIZE) {
      return true;
    }
    return false;
  }
  /* default constructor */
  public QueryCachedMap() {
    super(10, 0.75f, false);
  }
}