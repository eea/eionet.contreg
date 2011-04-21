package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;


/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
@UrlBinding("/objectsInSource.action")
public class ObjectsInSourceActionBean extends AbstractSearchActionBean<SubjectDTO> {

    /** */
    private String uri;
    private long uriHash;
    private long anonHash;
    private boolean noCriteria;


    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution init() {
        return new ForwardResolution("/pages/objectsInSource.jsp");
    }


    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    public Resolution search() throws DAOException{

        if (resultList == null || resultList.size() == 0) {
            Pair<Integer, List<SubjectDTO>> result =
                DAOFactory.get().getDao(SearchDAO.class)
                .searchBySource(
                        uri,
                        PagingRequest.create(getPageN()),
                        new SortingRequest(getSortP(), SortOrder.parse(getSortO())));

            resultList = result.getRight();
            matchCount = result.getLeft();
        }

        return new ForwardResolution("/pages/objectsInSource.jsp");
    }


    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() {

        ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

        SubjectPredicateColumn col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDF_TYPE);
        col.setTitle("Type");
        col.setSortable(true);
        list.add(col);

        col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDFS_LABEL);
        col.setTitle("Label");
        col.setSortable(true);
        list.add(col);

        return list;
    }


    public String getUri() {
        return uri;
    }


    public void setUri(String uri) {
        this.uri = uri;
    }


    public long getUriHash() {
        return uriHash;
    }


    public void setUriHash(long uriHash) {
        this.uriHash = uriHash;
    }

    public long getAnonHash() {
        return anonHash;
    }


    public void setAnonHash(long anonHash) {
        this.anonHash = anonHash;
    }


    public boolean isNoCriteria() {
        return noCriteria;
    }


    public void setNoCriteria(boolean noCriteria) {
        this.noCriteria = noCriteria;
    }

}
