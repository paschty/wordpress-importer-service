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

package de.vzg.service.mycore;

import java.io.IOException;
import java.io.InputStream;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class DerivateCreater {

    private static final String DERIVATE_INTERNALS = "mycorederivate/derivate/internals/internal/";

    private static final String DERIVATE_LINKMETAS = "mycorederivate/derivate/linkmetas/linkmeta";

    public Document createDerivate(String derID, String parentID, String fileName) {
        try (InputStream derivateTemplateStream = getClass().getClassLoader()
            .getResourceAsStream("derivate_template.xml")) {
            SAXBuilder sb = new SAXBuilder();

            Document build = sb.build(derivateTemplateStream);

            Element rootElement = build.getRootElement();
            rootElement.setAttribute("ID", derID);

            Attribute mainDocAttribute = getElement(rootElement, DERIVATE_INTERNALS)
                .getAttribute("maindoc");
            mainDocAttribute.setValue(fileName);

            Attribute href = getElement(rootElement, DERIVATE_LINKMETAS)
                .getAttribute("href", MODSUtil.MODS_NAMESPACE);
            href.setValue(parentID);

            return build;
        } catch (IOException e) {
            throw new RuntimeException("Could not load derivate_template.xml", e);
        } catch (JDOMException e) {
            throw new RuntimeException("Could not parse derivate_template.xml", e);
        }
    }

    private Element getElement(final Element root, final String xpath) {
        final XPathExpression<Element> xpathFac = XPathFactory
            .instance().compile(xpath, Filters.element());
        return xpathFac.evaluateFirst(root);
    }

}
