package eionet.cr.web.action.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.BadUserHomeUrlException;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.UserHomeUrlExtractor;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}")
public class HomeActionBean extends WorkspaceActionBean{
}
