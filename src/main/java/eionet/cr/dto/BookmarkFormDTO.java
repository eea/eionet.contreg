package eionet.cr.dto;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class BookmarkFormDTO {

    private Long id;
    private String source;
    private String title;
    private String description;
    private String tags;
    private String methodology;

    /**
     *
     */
    public BookmarkFormDTO() {
    }

    /**
     * 
     * @param id
     * @param source
     * @param title
     * @param description
     * @param tags
     */
    public BookmarkFormDTO(Long id, String source, String title, String description, String tags, String methodology) {
        this.id = id;
        this.source = source;
        this.description = description;
        this.tags = tags;
        this.title = title;
        this.methodology = methodology;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    /**
     * @return the methodology
     */
    public String getMethodology() {
        return methodology;
    }

    /**
     * @param methodology the methodology to set
     */
    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }
}
