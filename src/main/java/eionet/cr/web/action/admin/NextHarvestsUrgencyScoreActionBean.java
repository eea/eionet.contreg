package eionet.cr.web.action.admin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.AccessException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
import eionet.cr.util.Pair;
import eionet.cr.web.action.AbstractSearchActionBean;
import eionet.cr.web.util.columns.NextHarvestsUrgencyScoreColumn;
import eionet.cr.web.util.columns.NextHarvestsUrgencyScoreColumn.COLUMN;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 * Urgency score for the next X (default 20) harvests.
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 */

@UrlBinding("/admin/nhus")
public class NextHarvestsUrgencyScoreActionBean extends AbstractSearchActionBean<HarvestUrgencyScoreDTO> {

    private int urgencyScoreLimit = 20;
    private boolean adminLoggedIn = false;
    private int resultsFound = 0;
    private int nrOfUrgentSources;

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator()) {
                setAdminLoggedIn(true);
                Pair<Integer, List<HarvestUrgencyScoreDTO>> result =
                        DAOFactory.get().getDao(HarvestSourceDAO.class).getUrgencyOfComingHarvests(urgencyScoreLimit);
                resultList = result.getRight();
                matchCount = 0;
                resultsFound = result.getLeft();
                nrOfUrgentSources = getNumberOfUrgentSources(resultList, 1d);
            } else {
                setAdminLoggedIn(false);
            }
        } else {
            setAdminLoggedIn(false);
        }
        return new ForwardResolution("/pages/admin/nextHarvestsUrgencyScore.jsp");
    }

    /**
     * Returns the number of elements in the resultList, that have higher urgency parameter than the provided urgencyScore.
     *
     * @param resultList
     * @param urgancyScore
     * @return
     */
    private int getNumberOfUrgentSources(Collection<HarvestUrgencyScoreDTO> resultList, double urgancyScore) {

        int result = 0;

        for (HarvestUrgencyScoreDTO dto : resultList) {
            if (dto.getUrgency() > urgancyScore) {
                result++;
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() {

        LinkedList<SearchResultColumn> columnList = new LinkedList<SearchResultColumn>();

        NextHarvestsUrgencyScoreColumn urlColumn = new NextHarvestsUrgencyScoreColumn();
        urlColumn.setColumnType(COLUMN.URL);
        urlColumn.setSortable(false);
        urlColumn.setTitle("URL");
        urlColumn.setEscapeXml(false);
        columnList.add(urlColumn);

        NextHarvestsUrgencyScoreColumn dateColumn = new NextHarvestsUrgencyScoreColumn();
        dateColumn.setColumnType(COLUMN.LASTHARVEST);
        dateColumn.setSortable(false);
        dateColumn.setTitle("Last harvest");
        columnList.add(dateColumn);

        NextHarvestsUrgencyScoreColumn intervalColumn = new NextHarvestsUrgencyScoreColumn();
        intervalColumn.setColumnType(COLUMN.INTERVAL);
        intervalColumn.setSortable(false);
        intervalColumn.setTitle("Interval (min)");
        columnList.add(intervalColumn);

        NextHarvestsUrgencyScoreColumn urgencyColumn = new NextHarvestsUrgencyScoreColumn();
        urgencyColumn.setColumnType(COLUMN.URGENCY);
        urgencyColumn.setSortable(false);
        urgencyColumn.setTitle("Urgency");
        columnList.add(urgencyColumn);

        return columnList;
    }

    public Resolution search() throws DAOException {
        return view();
    }

    public Resolution filter() throws DAOException, AccessException {
        return view();
    }

    public int getUrgencyScoreLimit() {
        return urgencyScoreLimit;
    }

    public void setUrgencyScoreLimit(int urgencyScoreLimit) {
        this.urgencyScoreLimit = urgencyScoreLimit;
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public void setAdminLoggedIn(boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
    }

    public int getResultsFound() {
        return resultsFound;
    }

    public void setResultsFound(int resultsFound) {
        this.resultsFound = resultsFound;
    }

    /**
     * @return the nrOfUrgentSources
     */
    public int getNrOfUrgentSources() {
        return nrOfUrgentSources;
    }

}
