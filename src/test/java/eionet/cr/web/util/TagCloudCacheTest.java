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
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.dto.TagDTO;
import eionet.cr.util.Pair;

/**
 * 
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */

public class TagCloudCacheTest extends TestCase {
	
	@Test
	public void testCacheLimit(){
		new ApplicationCache().contextInitialized(null);
		
		ApplicationCache.updateTagCloudCache(getTestData(11));
		
		assertEquals(10, ApplicationCache.getTagCloud(10).size());		
		assertEquals(3, ApplicationCache.getTagCloud(3).size());
		assertEquals("tag0", ApplicationCache.getTagCloud(3).get(0).getTag());		
		assertEquals(99, ApplicationCache.getTagCloud(3).get(1).getCount());		
		assertEquals(9, ApplicationCache.getTagCloud(3).get(1).getScale());		
	}

	/**
	 * @return
	 */
	private List<TagDTO> getTestData(int size) {
		List<TagDTO> result = new ArrayList<TagDTO>();
		for(int i=0; i< size; i++) {
			result.add(new TagDTO("tag" + i, 100 - i, 100));
		}
		return result;
	}

}
