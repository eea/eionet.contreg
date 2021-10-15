package eionet.cr.ldap.model;

public class LdapRole {

    private String name;

    private String fullDn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullDn() {
        return fullDn;
    }

    public void setFullDn(String fullDn) {
        this.fullDn = fullDn;
    }
}
