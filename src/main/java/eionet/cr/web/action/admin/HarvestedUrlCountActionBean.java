package eionet.cr.web.action.admin;

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
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Pair;
import eionet.cr.web.action.AbstractSearchActionBean;
import eionet.cr.web.util.columns.HarvestedUrlCountColumn;
import eionet.cr.web.util.columns.HarvestedUrlCountColumn.COLUMN;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 * Harvested URL count per day for the last X (default 42) days.
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 */

@UrlBinding("/admin/harvestedurl")
public class HarvestedUrlCountActionBean extends AbstractSearchActionBean<HarvestedUrlCountDTO> {

    private int harvestedUrlDays = 42; // matches the default number of days between harvests.
    private boolean adminLoggedIn = false;
    private int resultsFound = 0;

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator()) {
                setAdminLoggedIn(true);
                Pair<Integer, List<HarvestedUrlCountDTO>> result =
                        DAOFactory.get().getDao(HarvestSourceDAO.class).getLatestHarvestedURLs(harvestedUrlDays);
                resultList = result.getRight();
                matchCount = 0;
                resultsFound = result.getLeft();
            } else {
                setAdminLoggedIn(false);
            }
        } else {
            setAdminLoggedIn(false);
        }
        return new ForwardResolution("/pages/admin/harvestedUrlCount.jsp");
    }

    public Resolution search() throws DAOException {
        return view();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() {

        LinkedList<SearchResultColumn> columnList = new LinkedList<SearchResultColumn>();

        HarvestedUrlCountColumn dateColumn = new HarvestedUrlCountColumn();
        dateColumn.setColumnType(COLUMN.HARVESTDAYSTRING);
        dateColumn.setSortable(false);
        dateColumn.setTitle("Harvest day");
        columnList.add(dateColumn);

        HarvestedUrlCountColumn intervalColumn = new HarvestedUrlCountColumn();
        intervalColumn.setColumnType(COLUMN.HARVESTCOUNT);
        intervalColumn.setSortable(false);
        intervalColumn.setTitle("Harvest count");
        columnList.add(intervalColumn);

        return columnList;
    }

    public Resolution filter() throws DAOException, AccessException {
        return view();
    }

    public int getHarvestedUrlDays() {
        return harvestedUrlDays;
    }

    public void setHarvestedUrlDays(int harvestedUrlDays) {
        this.harvestedUrlDays = harvestedUrlDays;
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

}
