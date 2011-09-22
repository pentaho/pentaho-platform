<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="cat-edit">
  <table cellpadding="1" cellspacing="0" border="1" id="{$renderId}">
    <xsl:apply-templates select="cat-category"/>
  </table>
</xsl:template>

<xsl:template match="cat-category">
  <tr>
    <th align="left" class="navi-axis">
      <img src="{$context}/wcf/catedit/{@icon}" width="9" height="9"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="@name"/>
    </th>
  </tr>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="cat-item">
  <tr>
    <td class="navi-hier">
      <div style="margin-left: 1em">
        <xsl:apply-templates select="cat-button"/>
        <xsl:apply-templates select="move-button"/>
        <xsl:value-of select="@name"/>
      </div>
    </td>
  </tr>
</xsl:template>

<xsl:template match="cat-button[@icon]">
  <input border="0" type="image" src="{$context}/wcf/catedit/{@icon}" name="{@id}" width="9" height="9"/>
  <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="cat-button">
  <img src="{$context}/wcf/catedit/empty.png" width="9" height="9"/>
  <xsl:text> </xsl:text>
</xsl:template>


</xsl:stylesheet>
