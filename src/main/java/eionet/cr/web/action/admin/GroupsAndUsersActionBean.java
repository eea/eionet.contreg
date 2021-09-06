package eionet.cr.web.action.admin;

import eionet.cr.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.errors.AclPropertiesInitializationException;
import eionet.cr.ldap.services.LdapService;
import eionet.cr.util.UserUtil;
import eionet.cr.web.HandleGroupService;
import eionet.cr.web.PageErrorConstants;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.viewmodel.GroupDetails;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

@UrlBinding("/admin/admintools")
public class GroupsAndUsersActionBean extends AbstractActionBean {

    private String groupsAndUsersPage = "/pages/admin/groupsAndUsers.jsp";
    private String messagePage = "/pages/admin/message.jsp";;
    private String msgOne;
    private Set<String> crGroups;
    private Hashtable<String, Vector<String>> crGroupsAndUsers;
    private GroupDetails groupDetails;

    @SpringBean
    HandleGroupService handleGroupService;

    @SpringBean
    LdapService ldapService;

    @DefaultHandler
    public Resolution view() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if(!isUserLoggedIn()) {
            msgOne = PageErrorConstants.NOT_AUTHENTICATED + " Admin tools";
            return new ForwardResolution(messagePage);
        }
        if (!UserUtil.hasAuthPermission(getContext().getRequest(), "/admintools", "v")) {
            msgOne = PageErrorConstants.FORBIDDEN_ACCESS + " Admin tools";
            return new ForwardResolution(messagePage);
        }
        crGroupsAndUsers = new Hashtable<>();
        if (UserUtil.getCrGroupsAndUsers()!=null) {
            crGroupsAndUsers = UserUtil.getCrGroupsAndUsers();
        } else {
            crGroupsAndUsers = UserUtil.fetchGroupsAndUsers(false);
            UserUtil.setCrGroupsAndUsers(crGroupsAndUsers);
        }
        crGroups = crGroupsAndUsers.keySet();
        groupDetails = new GroupDetails();
        return new ForwardResolution(groupsAndUsersPage);
    }

    public String getMsgOne() {
        return msgOne;
    }

    public void setMsgOne(String msgOne) {
        this.msgOne = msgOne;
    }

    public Set<String> getCrGroups() {
        return crGroups;
    }

    public void setCrGroups(Set<String> crGroups) {
        this.crGroups = crGroups;
    }

    public Hashtable<String, Vector<String>> getCrGroupsAndUsers() {
        return crGroupsAndUsers;
    }

    public void setCrGroupsAndUsers(Hashtable<String, Vector<String>> crGroupsAndUsers) {
        this.crGroupsAndUsers = crGroupsAndUsers;
    }

    public GroupDetails getGroupDetails() {
        return groupDetails;
    }

    public void setGroupDetails(GroupDetails groupDetails) {
        this.groupDetails = groupDetails;
    }
}













