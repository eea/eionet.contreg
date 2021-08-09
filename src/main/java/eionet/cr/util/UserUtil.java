package eionet.cr.util;

import eionet.cr.acl.services.AclOperationsService;
import eionet.cr.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.errors.AclPropertiesInitializationException;
import eionet.cr.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;
import eionet.cr.ldap.services.LdapService;
import eionet.cr.spring.SpringApplicationContext;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Component
public class UserUtil {

    private static Hashtable<String, Vector<String>> crGroupsAndUsers;
    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUtil.class);

    public ArrayList<String> getUserOrGroup(String userName, boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        ArrayList<String> userGroupResults = new ArrayList<>();
        Hashtable<String, Vector<String>> results = fetchGroupsAndUsers(init);
        setCrGroupsAndUsers(results);
        Set<String> ddGroups = getCrGroupsAndUsers().keySet();
        for (String ddGroup : ddGroups) {
            Vector<String> ddGroupUsers = getCrGroupsAndUsers().get(ddGroup);
            if (ddGroupUsers.contains(userName)) {
                userGroupResults.add(userName);
            }
        }
        List<LdapRole> rolesList = this.getLdapService().getUserLdapRoles(userName);
        rolesList.forEach(role -> userGroupResults.add(role.getName()));
        return userGroupResults;
    }

    public static Hashtable<String, Vector<String>> fetchGroupsAndUsers(boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Hashtable<String, Vector<String>> results;
        if (init) {
            results = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(true);
        }
        results = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(false);
        return results;
    }

    public static AclOperationsService getAclOperationsService() {
        return SpringApplicationContext.getBean("aclOperationsService");
    }

    public LdapService getLdapService() {
        return SpringApplicationContext.getBean("ldapService");
    }

    public static Hashtable<String, Vector<String>> getCrGroupsAndUsers() {
        return crGroupsAndUsers;
    }

    public static void setCrGroupsAndUsers(Hashtable<String, Vector<String>> crGroupsAndUsers) {
        UserUtil.crGroupsAndUsers = crGroupsAndUsers;
    }

    public static void setUserGroupResults(CRUser user) {
        try {
            UserUtil userUtil = new UserUtil();
            ArrayList<String> results = userUtil.getUserOrGroup(user.getUserName(), false);
            user.setGroupResults(results);
        } catch (AclLibraryAccessControllerModifiedException | AclPropertiesInitializationException | LdapDaoException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * checks if user has specific permission to aclPath
     * @param session
     * @param aclPath
     * @param perm
     * @return
     */
    public static Boolean hasPermission(HttpSession session, String aclPath, String perm) {
        CRUser user = session == null ? null : (CRUser) session.getAttribute(WebConstants.USER_SESSION_ATTR);
        if (user!=null) {
            if (user.getGroupResults() != null) {
                for (String result : user.getGroupResults()) {
                    if (CRUser.hasPermission(result, aclPath, perm)) {
                        return true;
                    }
                }
            } else {
                return CRUser.hasPermission(user.getUserName(),aclPath,perm);
            }
        }
        return false;
    }

    /**
     * checks if user is logged in
     * @param request
     * @return
     */
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        CRUser user = getUser(request);
        return user != null;
    }

    /**
     * Returns current user, or null, if the current session does not have user attached to it.
     * @param request
     * @return
     */
    public static CRUser getUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session == null ? null : (CRUser) session.getAttribute(WebConstants.USER_SESSION_ATTR);
    }
}
