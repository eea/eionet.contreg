package eionet.cr.ldap.dao.impl;

import eionet.cr.config.GeneralConfig;
import eionet.cr.ldap.dao.BaseLdapDao;
import eionet.cr.ldap.dao.LdapRoleDao;
import eionet.cr.ldap.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LdapRoleDaoImpl extends BaseLdapDao implements LdapRoleDao {

    private String usersDn;
    private String rolesDn;

    public LdapRoleDaoImpl() {
        usersDn = GeneralConfig.getProperty(GeneralConfig.LDAP_USER_DIR) + "," + baseDn;
        rolesDn = GeneralConfig.getProperty(GeneralConfig.LDAP_ROLE_DIR) + "," + baseDn;
    }

    @Override
    public List<LdapRole> findUserRoles(String user) throws LdapDaoException {
        List<LdapRole> result = new ArrayList<>();
        DirContext ctx = null;
        try {
            String myFilter = "(&(objectClass=groupOfUniqueNames)(uniqueMember=uid=" + user + "," + usersDn + "))";
            NamingEnumeration results = getResults(myFilter);
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                String dn = sr.getName();
                if (dn != null && dn.length() > 0){
                    String cn = (String)sr.getAttributes().get(GeneralConfig.getProperty(GeneralConfig.LDAP_ROLE_NAME)).get();
                    LdapRole r = new LdapRole();
                    r.setFullDn(dn + "," + rolesDn);
                    r.setName("cn=" + cn);
                    result.add(r);
                }
            }
        } catch (NamingException e) {
            throw new LdapDaoException(e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    protected NamingEnumeration getResults(String myFilter) throws NamingException {
        DirContext ctx = getDirContext();
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return ctx.search(rolesDn, myFilter, sc);
    }
}
