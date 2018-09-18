package de.vzg.service.configuration;

public class ImporterConfigurationPart {

    /**
     * The url to the wordpress blog
     */
    private String blog;

    /**
     * The url to the mycore repository
     */
    private String repository;

    /**
     * The parent object to which the posts will be appended
     */
    private String parentObject;

    /**
     * The template which will be used to generate a object
     */
    private String postTemplate;

    private String license;

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getParentObject() {
        return parentObject;
    }

    public void setParentObject(String parentObject) {
        this.parentObject = parentObject;
    }

    public String getPostTemplate() {
        return postTemplate;
    }

    public void setPostTemplate(String postTemplate) {
        this.postTemplate = postTemplate;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
