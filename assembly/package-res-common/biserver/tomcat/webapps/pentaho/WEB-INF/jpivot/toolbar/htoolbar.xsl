<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="no" encoding="US-ASCII"/>
<xsl:param name="context"/>
<xsl:param name="renderId"/>
<xsl:param name="token"/>
<xsl:param name="imgpath" select="'jpivot/toolbar'"/>

<xsl:template match="tool-bar">
  <table border="0" cellspacing="1" cellpadding="0"  id="{$renderId}">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </table>
</xsl:template>

<xsl:template match="tool-button">
  <td>
    <input type="image" name="{@id}" src="{$context}/{$imgpath}/{@img}.png" border="0" title="{@title}" width="24" height="24"/>
  </td>
</xsl:template>

<xsl:template match="tool-sep">
  <td>
    <div style="width: 1ex"/>
  </td>
</xsl:template>

<xsl:template match="img-button">
  <td>
    <a href="{@href}">
      <xsl:if test="@target">
        <xsl:attribute name="target"><xsl:value-of select="@target"/></xsl:attribute>
      </xsl:if>
      <img src="{$context}/{$imgpath}/{@img}.png" border="0" title="{@title}" width="24" height="24"/>
    </a>
  </td>
</xsl:template>

</xsl:stylesheet>
