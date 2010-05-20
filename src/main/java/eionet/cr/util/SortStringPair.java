package eionet.cr.util;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class SortStringPair {

	public static List<Pair<String, String>> sortByLeftAsc(List<Pair<String, String>> sourcePairs){
		// Sorting the types by Pair left.
		for (int i=0; i<sourcePairs.size(); i++){
			for (int j = i; j<sourcePairs.size(); j++){
				if (sourcePairs.get(i).getLeft().compareTo(sourcePairs.get(j).getLeft())>0){
					Collections.swap(sourcePairs, i, j);
				}
			}
		}
		return sourcePairs;
	}
}
