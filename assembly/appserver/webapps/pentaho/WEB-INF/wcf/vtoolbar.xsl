<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="context"/>
<xsl:param name="renderId"/>
<xsl:param name="imgpath" select="'wcf/toolbar'"/>
<xsl:output method="html" indent="no" encoding="US-ASCII"/>

<xsl:template match="tool-bar">
  <table border="0" cellspacing="1" cellpadding="0"  id="{$renderId}">
    <xsl:apply-templates/>
  </table>
</xsl:template>

<xsl:template match="tool-button">
  <tr>
    <td align="left">
      <input type="image" name="{@id}" src="{$context}/{$imgpath}/{@img}.png" border="0" title="{@title}" width="24" height="24"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="tool-sep">
  <tr>
    <td align="left">
      <div style="margin-top: 2px"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="img-button">
  <tr>
    <td align="left">
      <a href="{@href}">
        <xsl:if test="@target">
          <xsl:attribute name="target"><xsl:value-of select="@target"/></xsl:attribute>
        </xsl:if>
        <img src="{$context}/{$imgpath}/{@img}.png" border="0" title="{@title}" width="24" height="24"/>
      </a>
    </td>
  </tr>
</xsl:template>

</xsl:stylesheet>
