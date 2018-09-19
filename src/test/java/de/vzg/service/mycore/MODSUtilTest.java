package de.vzg.service.mycore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;

public class MODSUtilTest {

    private static final String EXPECTED_CHILDREN = "mir_mods_00000216,mir_mods_00001394,mir_mods_00001395,mir_mods_00001396,mir_mods_00001397,mir_mods_00001398";

    private static final String EXPECTED_URL = "https://verfassungsblog.de/a-new-page-in-protecting-european-constitutional-values-how-to-best-use-the-new-eu-rule-of-law-framework-vis-a-vis-poland/";

    private static Document readDocument(String file) throws IOException, JDOMException {
        try (InputStream is = MODSUtilTest.class.getClassLoader().getResourceAsStream(file)) {
            return new SAXBuilder().build(is);
        }
    }

    @org.junit.Test
    public void getChildren() throws IOException, JDOMException {
        final Document documentWithChildren = readDocument("test_children.xml");
        final List<String> expectedChildren = Arrays.asList(EXPECTED_CHILDREN.split(","));
        final List<String> children = MODSUtil.getChildren(documentWithChildren);

        for (String child : expectedChildren) {
            Assert.assertTrue("Expected children contains: " + child, children.contains(child));
        }

        for (String child : children) {
            Assert.assertTrue("Expected children contains: " + child, expectedChildren.contains(child));
        }
    }

    @org.junit.Test
    public void getFulltextURL() throws IOException, JDOMException {
        final Document document = readDocument("test_url.xml");
        final String fulltextURL = MODSUtil.getFulltextURL(document).get();
        Assert.assertEquals("URLs should be same!", EXPECTED_URL, fulltextURL);
    }

    @org.junit.Test
    public void isLockedOrDeleted() throws IOException, JDOMException {
        final Document document = readDocument("locked.xml");
        final Document document2 = readDocument("test_children.xml");
        Assert.assertTrue("Should be locked!", MODSUtil.isLockedOrDeleted(document));
        Assert.assertTrue("Should not be locked!", !MODSUtil.isLockedOrDeleted(document2));
    }
}
