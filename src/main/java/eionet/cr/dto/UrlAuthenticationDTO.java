package eionet.cr.dto;

/**
 *
 * Data object for authenticated sources with defined url beginning, username and password
 *
 * @author Jaak
 */
public class UrlAuthenticationDTO {

    private int id;
    private String urlBeginning;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrlBeginning() {
        return urlBeginning;
    }

    public void setUrlBeginning(String urlBeginning) {
        this.urlBeginning = urlBeginning;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
