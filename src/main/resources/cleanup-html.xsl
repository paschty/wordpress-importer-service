<!--
  ~ This file is part of ***  M y C o R e  ***
  ~ See http://www.mycore.de/ for details.
  ~
  ~ MyCoRe is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ MyCoRe is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/1999/xhtml">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template match="@text-align[translate(text(), $uppercase, $lowercase) = 'justify']">
    <!-- remove this -->
  </xsl:template>

  <xsl:template match="html:script">
    <!-- remove scripts -->
  </xsl:template>

  <xsl:template match="@*[translate(local-name(), $uppercase, $lowercase) = 'onclick']">
    <!-- remove onclick -->
  </xsl:template>

  <xsl:template match="html:*[contains(@style, 'display') and contains(@style, 'none')]">
    <!-- remove this -->
  </xsl:template>

  <xsl:template match="html:table[contains(@class,'footnote-reference-container')]">
    <xsl:if test="html:tbody/html:tr">
      <html:ul>
        <xsl:for-each select="html:tbody/html:tr">
          <html:li>
            <xsl:value-of select="concat(html:td[1], ' ', html:td[3])" />
            <br />
          </html:li>
        </xsl:for-each>
      </html:ul>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
