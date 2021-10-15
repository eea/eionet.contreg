package eionet.cr.ldap.dao.impl;

import eionet.cr.config.GeneralConfig;
import eionet.cr.ldap.dao.BaseLdapDao;
import eionet.cr.ldap.dao.LdapRoleDao;
import eionet.cr.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.io.IOException;
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

    @Override
    public List<String> findRoleUsers(String roleName) throws LdapDaoException {
        List<String> result = new ArrayList<>();
        DirContext ctx = null;
        try {
            String myFilter = "(&(objectClass=groupOfUniqueNames)(" + roleName + "))";
            NamingEnumeration results = getResults(myFilter);
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();
                Attribute usersAttr = attrs.get("uniquemember");
                if (usersAttr != null && usersAttr.get() != null){
                    result = parseUsersAttr(usersAttr);
                }
            }
        } catch (NamingException e) {
            throw new LdapDaoException(e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    @Override
    public List<LdapRole> findAllRoles() throws LdapDaoException {
        List<LdapRole> result = new ArrayList(1);
        LdapContext ctx = null;
        try {
            ctx = getPagedLdapContext();
            String myFilter = "objectclass=groupOfUniqueNames";
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setCountLimit(0);
            sc.setTimeLimit(0);
            sc.setReturningObjFlag(true);

            byte[] cookie = null;
            int total;
            do {
                NamingEnumeration results = ctx.search(rolesDn, myFilter, sc);
                while (results != null && results.hasMore()) {
                    SearchResult sr = (SearchResult) results.next();
                    String dn = sr.getName();
                    if (dn != null && dn.length() > 0) {
                        String cn = (String)sr.getAttributes().get(GeneralConfig.getProperty(GeneralConfig.LDAP_ROLE_NAME)).get();
                        LdapRole r = new LdapRole();
                        r.setFullDn(dn + "," + baseDn);
                        r.setName("cn=" + cn);
                        result.add(r);
                    }
                }
                Control[] controls = ctx.getResponseControls();
                if (controls != null) {
                    for (int i = 0; i < controls.length; i++) {
                        if (controls[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc =
                                    (PagedResultsResponseControl) controls[i];
                            total = prrc.getResultSize();
                            cookie = prrc.getCookie();
                        }
                    }
                }
                // Re-activate paged results
                ctx.setRequestControls(new Control[]{
                        new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);
        } catch (IOException | NamingException e) {
            throw new LdapDaoException("Error: " + e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    protected List<String> parseUsersAttr(Attribute usersAttr) throws NamingException {
        List<String> result = new ArrayList<>();
        for (int i=0; i<usersAttr.size(); i++) {
            String user = (String) usersAttr.get(i);
            int pos = user.indexOf("ou=");
            String userName = user.substring(4, pos-1);
            result.add(userName);
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
