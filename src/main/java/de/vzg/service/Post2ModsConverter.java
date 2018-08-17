package de.vzg.service;

import static de.vzg.service.mycore.MODSUtil.MODS_NAMESPACE;
import static de.vzg.service.mycore.MODSUtil.XLINK_NAMESPACE;

import de.vzg.service.configuration.ImporterConfiguration;
import de.vzg.service.mycore.MODSUtil;
import de.vzg.service.wordpress.UserFetcher;
import de.vzg.service.wordpress.model.Post;
import de.vzg.service.wordpress.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.ConfigurationException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.Jsoup;

/**
 * Reads a template with a name from {@link ImporterConfiguration#getConfigPath()} and sets some values with xpath from
 * a {@link Post} to it.
 */
public class Post2ModsConverter {

    public static final String MODS_TEMPLATE_FILE = "mods_template.xml";

    private static final String MODS_XPATH = "/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods";

    private static final String TITLE_XPATH = MODS_XPATH + "/mods:titleInfo/mods:title";

    private static final String SUB_TITLE_XPATH = MODS_XPATH + "/mods:titleInfo/mods:subTitle";

    private static final String AUTHOR_XPATH =
        MODS_XPATH + "/mods:name"; // mods:displayForm & mods:namePart type="family" && mods:namePart type="given"

    private static final String DISPLAY_FORM = AUTHOR_XPATH + "/mods:displayForm";

    private static final String FAMILY_NAME = AUTHOR_XPATH + "/mods:namePart[@type='family']";

    private static final String GIVEN_NAME = AUTHOR_XPATH + "/mods:namePart[@type='given']";

    private static final String PUBLICATION_XPATH = MODS_XPATH + "/mods:originInfo[@eventType='publication']";

    private static final String PUBLICATION_DATE_XPATH = PUBLICATION_XPATH + "/mods:dateIssued";

    private static final String LANGUAGE_XPATH = MODS_XPATH + "/mods:language/mods:languageTerm";

    private static final String PARENT_XPATH = "/mycoreobject/structure/parents/parent";

    private static final String RELATED_PARENT_XPATH = MODS_XPATH + "/mods:relatedItem";

    private static final String URL_XPATH = MODS_XPATH + "/mods:location/mods:url";

    private final Post blogPost;

    private final String parentID;

    private final String blogURL;

    private final String templateName;

    private final Document modsTemplate;

    public Post2ModsConverter(Post blogPost, String parentID, String blogURL, String template) {
        this.blogPost = blogPost;
        this.parentID = parentID;
        this.blogURL = blogURL;
        this.templateName = template;
        modsTemplate = loadModsTemplate();

    }

    private Document loadModsTemplate() {
        try (final InputStream is = getTemplateStream()) {
            return new SAXBuilder().build(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not load mods template!", e);
        } catch (JDOMException e) {
            throw new RuntimeException("Could not parse mods template!", e);
        }
    }

    private InputStream getTemplateStream() throws IOException {
        if (templateName != null) {
            Path templatePath = ImporterConfiguration.getConfigPath().resolve(templateName);
            if (Files.exists(templatePath)) {
                return Files.newInputStream(templatePath, StandardOpenOption.READ);
            } else {
                throw new RuntimeException(
                    new ConfigurationException("The template file '" + templatePath.toString() + "' does not exist! "));
            }
        }
        return Post2ModsConverter.class.getClassLoader().getResourceAsStream(MODS_TEMPLATE_FILE);
    }

    private void setTitle() {
        final Element titleElement = getElement(TITLE_XPATH);
        final Element subTitleElement = getElement(SUB_TITLE_XPATH);

        final String completeTitle = Jsoup.parseBodyFragment(this.blogPost.getTitle().getRendered()).text();
        String subTitle = blogPost.getWps_subtitle();

        if (completeTitle.contains(":") && (subTitle == null || subTitle
            .isEmpty())) {
            final String[] titles = completeTitle.split(":", 2);
            final String mainTitle = titles[0];
            subTitle = titles[1];

            titleElement.setText(mainTitle);
            subTitleElement.setText(subTitle);
        } else if (subTitle !=null && !subTitle.isEmpty()) {
            titleElement.setText(completeTitle);
            subTitleElement.setText(subTitle);
        } else {
            titleElement.setText(completeTitle);
            subTitleElement.getParent().removeContent(subTitleElement);
        }
    }

    private void setDateIssued(){
        final Element element = getElement(PUBLICATION_DATE_XPATH);
        final String dateIssuedAsString = this.blogPost.getDate();

        try {
            final Date wpDate = Utils.getWPDate(dateIssuedAsString);
            final String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(wpDate);
            element.setText(formattedDate);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse date: " + dateIssuedAsString,e);
        }
    }

    private void setAuthor(){
        final Element displayFormElement = getElement(DISPLAY_FORM);
        final Element givenNameElement = getElement(GIVEN_NAME);
        final Element familyNameElement = getElement(FAMILY_NAME);

        final int author = blogPost.getAuthor();
        try {
            final User user = UserFetcher.fetchUser(blogURL, author);
            final String nameDisplayForm = user.getName();

            // only handles the form givenName familyName
            if(nameDisplayForm.contains(" ")){
                final String[] split = nameDisplayForm.split(" ",2);

                final String foreName = split[0];
                final String sureName = split[1];

                givenNameElement.setText(foreName);
                familyNameElement.setText(sureName);
            } else {
                givenNameElement.getParent().removeContent(givenNameElement);
                familyNameElement.getParent().removeContent(familyNameElement);
            }

            displayFormElement.setText(nameDisplayForm);
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching Author " + author, e);
        }
    }

    private void setParentID(){
        getElement(PARENT_XPATH).setAttribute("href", parentID, MODSUtil.XLINK_NAMESPACE);
        getElement(RELATED_PARENT_XPATH).setAttribute("href", parentID, XLINK_NAMESPACE);
    }

    private void setURL(){
        getElement(URL_XPATH).setText(blogPost.getLink());
    }

    private Element getElement(final String xpath) {
        final XPathExpression<Element> xpathFac = XPathFactory.instance().compile(xpath, Filters.element(), null, MODS_NAMESPACE);
        return xpathFac.evaluateFirst(modsTemplate);
    }

    public Document getMods() {
        setTitle();
        setDateIssued();
        setAuthor();
        setParentID();
        setURL();

        return modsTemplate;
    }

}
