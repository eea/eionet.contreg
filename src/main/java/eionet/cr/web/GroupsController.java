package eionet.cr.web;

import eionet.cr.errors.*;
import eionet.cr.ldap.model.LdapRole;
import eionet.cr.ldap.services.LdapService;
import eionet.cr.util.UserUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;
import eionet.cr.web.viewmodel.GroupDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/admintools")
public class GroupsController extends AbstractActionBean {

    private HandleGroupService handleGroupService;
    private LdapService ldapService;
    public static final String LDAP_GROUP_NOT_EXIST = "The LDAP group name you entered doesn't exist";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsController.class);

    @Autowired
    public GroupsController(HandleGroupService handleGroupService, LdapService ldapService) {
        this.handleGroupService = handleGroupService;
        this.ldapService = ldapService;
    }

    @GetMapping("/list")
    public String getGroupsAndUsers(Model model, HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if(!UserUtil.isUserLoggedIn(request)) {
            model.addAttribute("msgOne", PageErrorConstants.NOT_AUTHENTICATED + " Admin tools");
            return "message";
        }
        if (!UserUtil.hasAuthPermission(request, "/admintools", "v") || !UserUtil.hasCrPermission(request)) {
            model.addAttribute("msgOne", PageErrorConstants.FORBIDDEN_ACCESS + " Admin tools");
            return "message";
        }
        Hashtable<String, Vector<String>> crGroupsAndUsers = new Hashtable<>();
        if (UserUtil.getCrGroupsAndUsers()!=null) {
            crGroupsAndUsers = UserUtil.getCrGroupsAndUsers();
        } else {
            crGroupsAndUsers = UserUtil.fetchGroupsAndUsers(false);
            UserUtil.setCrGroupsAndUsers(crGroupsAndUsers);
        }
        Set<String> crGroups = crGroupsAndUsers.keySet();
        model.addAttribute("crGroups", crGroups);
        model.addAttribute("crGroupsAndUsers", crGroupsAndUsers);
        GroupDetails groupDetails = new GroupDetails();
        model.addAttribute("groupDetails", groupDetails);
        return "groupsAndUsers";
    }

    protected synchronized List<String> getAllLdapRoles() throws LdapDaoException {
        List<String> ldapRoleNames = new ArrayList<>();
        List<LdapRole> ldapRoles = ldapService.getAllLdapRoles();
        ldapRoles.forEach(role->ldapRoleNames.add(role.getName()));
        return ldapRoleNames;
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("groupDetails") GroupDetails groupDetails, Model model, HttpServletRequest request)
            throws UserExistsException, XmlMalformedException, LdapDaoException, AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if (!UserUtil.hasAuthPermission(request, "/admintools", "u") || !UserUtil.hasCrPermission(request)) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        if (groupDetails.getGroupNameOptionOne()!=null) {
            handleGroupService.addUserToGroup(groupDetails.getUserName(), groupDetails.getGroupNameOptionOne());
        } else {
            List<String> ldapRoles = getAllLdapRoles();
            if (!ldapRoles.contains(groupDetails.getLdapGroupName())) {
                model.addAttribute("msgOne", LDAP_GROUP_NOT_EXIST);
                return "message";
            }
            handleGroupService.addUserToGroup(groupDetails.getLdapGroupName(), groupDetails.getGroupNameOptionTwo());
        }
        refreshUserGroupResults(request);
        return "redirect:/v2/admintools/list";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam("crGroupName") String groupName, @RequestParam("memberName") String userName, Model model, HttpServletRequest request)
            throws XmlMalformedException, AclPropertiesInitializationException, LdapDaoException, AclLibraryAccessControllerModifiedException {
        if (!UserUtil.hasAuthPermission(request, "/admintools", "d") || !UserUtil.hasCrPermission(request)) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        handleGroupService.removeUserFromGroup(userName, groupName);
        refreshUserGroupResults(request);
        return "redirect:/v2/admintools/list";
    }

    protected void refreshUserGroupResults(HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        HttpSession session = request.getSession();
        CRUser user = session == null ? null : (CRUser) session.getAttribute(WebConstants.USER_SESSION_ATTR);
        if (user!=null) {
            UserUtil userUtil = new UserUtil();
            ArrayList<String> results = userUtil.getUserOrGroup(user, true);
            user.setGroupResults(results);
            session.setAttribute(WebConstants.USER_SESSION_ATTR, user);
        }
    }

    @PostMapping(value ="/roleUsers/{ldapGroup}")
    @ResponseBody
    public String getLdapRoleUsers(@PathVariable String ldapGroup) {
        List<String> users;
        try {
            users = ldapService.getRoleUsers(ldapGroup);
        } catch (LdapDaoException e) {
            return "Unable to retrieve users of ldap role " + ldapGroup;
        }
        String result = "";
        if (users !=null && users.size()>0) {
            for (String user : users) {
                if (!result.equals("")) {
                    result += ", ";
                }
                result += user;
            }
            return result;
        }
        return "No users found for ldap role " + ldapGroup;
    }

    @ExceptionHandler({UserExistsException.class, XmlMalformedException.class, AclLibraryAccessControllerModifiedException.class})
    public String handleExceptions(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", exception.getMessage());
        return "message";
    }

    @ExceptionHandler(LdapDaoException.class)
    public String handleLdapDaoException(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", PageErrorConstants.LDAP_ERROR);
        return "message";
    }

    @ExceptionHandler(AclPropertiesInitializationException.class)
    public String handleAclPropertiesException(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", PageErrorConstants.ACL_PROPS_INIT);
        return "message";
    }
}
