package eionet.cr.web;

import eionet.cr.acl.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.acl.errors.AclPropertiesInitializationException;
import eionet.cr.ldap.services.LdapService;
import eionet.cr.util.UserUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.viewmodel.GroupDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

@Controller
@RequestMapping("/admintools")
public class GroupsController extends AbstractActionBean {

//    private AclService aclService;
    private LdapService ldapService;
    /** */
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";

    public static final String LDAP_GROUP_NOT_EXIST = "The LDAP group name you entered doesn't exist";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsController.class);

    @Autowired
    public GroupsController(LdapService ldapService) {
//        this.aclService = aclService;
        this.ldapService = ldapService;
    }

    @GetMapping("/list")
    public String getGroupsAndUsers(Model model, HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if(!UserUtil.isUserLoggedIn(request)) {
            model.addAttribute("msgOne", PageErrorConstants.NOT_AUTHENTICATED + " Admin tools");
            return "message";
        }
        if (!UserUtil.hasAuthPermission(UserUtil.getUser(request))) {
            model.addAttribute("msgOne", PageErrorConstants.FORBIDDEN_ACCESS + " Admin tools");
            return "message";
        }
        Hashtable<String, Vector<String>> ddGroupsAndUsers = new Hashtable<>();
        if (UserUtil.getCrGroupsAndUsers()!=null) {
            ddGroupsAndUsers = UserUtil.getCrGroupsAndUsers();
        } else {
            ddGroupsAndUsers = UserUtil.fetchGroupsAndUsers(false);
            UserUtil.setCrGroupsAndUsers(ddGroupsAndUsers);
        }
        Set<String> ddGroups = ddGroupsAndUsers.keySet();
        model.addAttribute("ddGroups", ddGroups);
        model.addAttribute("ddGroupsAndUsers", ddGroupsAndUsers);
        GroupDetails groupDetails = new GroupDetails();
        model.addAttribute("groupDetails", groupDetails);
        return "groupsAndUsers";
    }
}
