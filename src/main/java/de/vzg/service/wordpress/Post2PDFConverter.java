/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.service.wordpress;

import de.vzg.service.wordpress.model.Post;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.helpers.DefaultHandler;

public class Post2PDFConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    private FopFactory fopFactory;

    public Post2PDFConverter() throws URISyntaxException {
        initFopFactory();

    }

    public void getPDF(Post post, OutputStream os) throws FOPException, TransformerException, IOException {
        String htmlContent = getXHtml(post);

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("xhtml2fo.xsl")) {
            Transformer transformer = SAXTransformerFactory.newInstance().newTransformer(new StreamSource(is));
            final byte[] bytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                StreamSource htmlSource = new StreamSource(inputStream);
                final FOUserAgent userAgent = fopFactory.newFOUserAgent();
                userAgent.setProducer("Wordpress-Importer-Service");
                DefaultHandler defaultHandler = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, os)
                    .getDefaultHandler();
                final Result res = new SAXResult(defaultHandler);
                transformer.transform(htmlSource, res);
            }
        }
    }

    private String getXHtml(Post post) {
        final Document document = Jsoup
            .parse("<h1>" + post.getTitle().getRendered() + "</h1>" + post.getContent().getRendered());
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String html = document.html();

        LOGGER.info("XHTML: {}", html);
        return "<?xml version=\"1.0\"?> \n"
            + "<!DOCTYPE some_name [ \n"
            + "<!ENTITY nbsp \"&#160;\"> \n"
            + "]> " + html.replace("<html>", "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    }

    private void initFopFactory() throws URISyntaxException {
        fopFactory = FopFactory.newInstance(new File(".").toURI());
    }

}
