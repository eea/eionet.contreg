package eionet.cr.util.pagination;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.QueryString;

/**
 * 
 * @author <a urlPath="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Pagination {
	
	/** */
	public static final String PAGE_NUM_PARAM = "pageN";

	/** */
	private int matchCount;
	private int numOfPages;
	private int curPageNum;	
	private String urlPath;
	private QueryString queryString;
	
	/** */
	private Page first;
	private Page prev;
	private Page last;
	private Page next;
	private List<Page> group;
	
	/**
	 * 
	 * @param matchCount
	 * @param curPageNum
	 * @param urlPath
	 */
	private Pagination(int matchCount, int numOfPages, int curPageNum, String urlPath, QueryString queryString){
		
		this.matchCount = matchCount;
		this.numOfPages = numOfPages;
		this.curPageNum = curPageNum;
		this.urlPath = urlPath;
		this.queryString = queryString==null ? QueryString.getInstance() :  queryString;
		
		constructPages();
	}
	
	/**
	 * 
	 * @param matchCount
	 * @param curPageNum
	 * @param urlPath
	 * @return
	 */
	public static Pagination getPagination(int matchCount, int curPageNum, String urlPath, QueryString queryString){

		int numOfPages = matchCount / pageLength();
		if (matchCount % pageLength() != 0)
			numOfPages = numOfPages + 1;

		if (numOfPages>1)
			return new Pagination(matchCount, numOfPages, Math.min(numOfPages, Math.max(1, curPageNum)), urlPath, queryString);
		else
			return null;
	}
	
	/**
	 * 
	 */
	private void constructPages(){
		
		if (curPageNum>1){
			first = createPage(1);
			prev = createPage(curPageNum-1);
		}
		
		if (curPageNum<numOfPages){
			last = createPage(numOfPages);
			next = createPage(curPageNum+1);
		}
		
        int startPage = Math.max(Math.min(curPageNum - groupSize()/2, numOfPages-(groupSize()-1)), 1);
        int endPage = Math.min(startPage + groupSize() - 1, numOfPages);

        group = new ArrayList<Page>();
        for (int i=startPage; i<=endPage; i++){
        	group.add(createPage(i));
        }
	}
	
	/**
	 * 
	 * @param newPageNum
	 * @return
	 */
	private String getPageHref(int pageNum){
		
		StringBuffer buf = new StringBuffer(urlPath);
		return buf.append("?").append(queryString.setParameterValue(PAGE_NUM_PARAM, String.valueOf(pageNum))).toString();
	}
	
	/**
	 * 
	 * @param pageNum
	 * @return
	 */
	private Page createPage(int pageNum){
		
		Page page = new Page();
		page.setNumber(pageNum);
		page.setSelected(curPageNum==pageNum);
		page.setHref(getPageHref(pageNum));
		
		return page;
	}

	/**
	 * @return the matchCount
	 */
	public int getMatchCount() {
		return matchCount;
	}

	/**
	 * @return the curPageNum
	 */
	public int getCurPageNum() {
		return curPageNum;
	}

	/**
	 * @return the numOfPages
	 */
	public int getNumOfPages() {
		return numOfPages;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int pageLength(){
		return 15; // TODO should probably not be hard-coded
	}
	
	/**
	 * 
	 * @return
	 */
	public static int groupSize(){
		return 9; // TODO should probably not be hard-coded
	}

	/**
	 * @return the rowsFrom
	 */
	public int getRowsFrom() {
		return (curPageNum-1)*pageLength()+1;
	}

	/**
	 * @return the rowsTo
	 */
	public int getRowsTo() {
		return Math.min(curPageNum*pageLength(), matchCount);
	}
	
	/**
	 * @return the first
	 */
	public Page getFirst() {
		return first;
	}

	/**
	 * @return the prev
	 */
	public Page getPrev() {
		return prev;
	}

	/**
	 * @return the last
	 */
	public Page getLast() {
		return last;
	}

	/**
	 * @return the next
	 */
	public Page getNext() {
		return next;
	}

	/**
	 * @return the group
	 */
	public List<Page> getGroup() {
		return group;
	}
}
