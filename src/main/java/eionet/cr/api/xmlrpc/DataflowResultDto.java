package eionet.cr.api.xmlrpc;


import java.io.Serializable;

/**
 * DTO class to exchange information between QAW and CR2
 *
 * @author Aleksandr Ivanov
 *         <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class DataflowResultDto implements Serializable {


    /**
     * serial version uid.
     */
    private static final long serialVersionUID = 3645288493049918190L;

    //title
    private String title;

    //link to resource
    private String resource;

    //dataflow
    private String[] dataflow;

    //country
    private String[] locality;

    //type
    private String[] type;

    //date. We don't need real date, just string representation
    private String date;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the dataflow
     */
    public String[] getDataflow() {
        return dataflow;
    }

    /**
     * @param dataflow the dataflow to set
     */
    public void setDataflow(String[] dataflow) {
        this.dataflow = dataflow;
    }

    /**
     * @return the locality
     */
    public String[] getLocality() {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(String[] locality) {
        this.locality = locality;
    }

    /**
     * @return the type
     */
    public String[] getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String[] type) {
        this.type = type;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }


}