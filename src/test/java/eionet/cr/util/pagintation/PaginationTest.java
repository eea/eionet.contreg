package eionet.cr.util.pagintation;

import eionet.cr.util.pagination.Pagination;
import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PaginationTest extends TestCase{

	/**
	 * 
	 */
	public void testPagination(){
		
		Pagination pagination = Pagination.getPagination(Pagination.pageLength(), 2, "");
		assertNull(pagination);
		
		pagination = Pagination.getPagination(Pagination.pageLength()+1, 2, "");
		
		assertNotNull(pagination.getPrev());
		assertEquals(1, pagination.getPrev().getNumber());
		
		assertNull(pagination.getNext());
		assertNull(pagination.getLast());
		
		assertNotNull(pagination.getGroup());
		assertEquals(2,pagination.getGroup().size());
		
		assertEquals(1,pagination.getGroup().get(0).getNumber());
		assertEquals("&" + Pagination.PAGE_NUM_PARAM + "=" + 1,pagination.getGroup().get(0).getHref());
		assertEquals(2,pagination.getGroup().get(1).getNumber());
		assertEquals("&" + Pagination.PAGE_NUM_PARAM + "=" + 2,pagination.getGroup().get(1).getHref());
		
		assertTrue(pagination.getGroup().get(1).isSelected());
		assertFalse(pagination.getGroup().get(0).isSelected());
		
		assertEquals(pagination.getRowsFrom(), pagination.getRowsTo());
	}
}
