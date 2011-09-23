<?xml version="1.0"?>

<!-- renders a "table" with one single cell to a <span>value</span> -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="no" encoding="US-ASCII"/>
<xsl:param name="context"/>
<xsl:param name="renderId"/>
<xsl:param name="token"/>
<xsl:param name="imgpath" select="'jpivot/table'"/>


<xsl:template match="mdxtable">
  <xsl:if test="@message">
    <div class="table-message"><xsl:value-of select="@message"/></div>
  </xsl:if>
  <xsl:apply-templates select="body/row/cell"/>
</xsl:template>

<xsl:template match="cell">
   <span nowrap="nowrap" >
   <xsl:choose>
     <xsl:when test="not(contains('odd,even,rot,gelb,gruen',@style))"> <!-- for any style other than odd,even,rot,gruen,gelb -->
      <xsl:attribute name="class"><xsl:text>cell-red</xsl:text></xsl:attribute> <!-- hack to force cascade of non-color styles -->
      <xsl:attribute name="style"><xsl:text>background-color:</xsl:text><xsl:value-of select="@style"/></xsl:attribute>
   </xsl:when>
   <xsl:otherwise>
     <xsl:attribute name="class"><xsl:text>cell-</xsl:text><xsl:value-of select="@style" /></xsl:attribute>
   </xsl:otherwise>
   </xsl:choose>
    <xsl:call-template name="render-label">
      <xsl:with-param name="label">
        <xsl:value-of select="@value"/>
      </xsl:with-param>
    </xsl:call-template>
  </span>
 </xsl:template>


<xsl:template name="render-label">
  <xsl:param name="label"/>
  <xsl:choose>
    <xsl:when test="property[@name='link']">
      <a href="{property[@name='link']/@value}" target="_blank">
        <xsl:value-of select="$label"/>
      <xsl:call-template name="properties"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$label"/>
      <xsl:call-template name="properties"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="properties">
  <xsl:apply-templates select="property[@name='arrow']"/>
  <xsl:apply-templates select="property[@name='cyberfilter']"/>
  <xsl:apply-templates select="property[@name='image']"/>
</xsl:template>

<xsl:template match="property[@name='arrow']">
  <span style="margin-left: 0.5ex">
    <img border="0" src="{$context}/{$imgpath}/arrow-{@value}.gif" width="10" height="10"/>
  </span>
</xsl:template>

<xsl:template match="property[@name='image']">
  <span style="margin-left: 0.5ex">
    <xsl:choose>
      <xsl:when test="starts-with(@value, '/')">
        <img border="0" src="{$context}{@value}"/>
      </xsl:when>
      <xsl:otherwise>
        <img border="0" src="{@value}"/>
      </xsl:otherwise>
    </xsl:choose>
  </span>
</xsl:template>

<xsl:template match="property[@name='cyberfilter']">
  <span style="margin-left: 0.5ex">
    <img align="middle" src="{$context}/{$imgpath}/filter-{@value}.gif" width="53" height="14"/>
  </span>
</xsl:template>

<xsl:template match="property"/>

</xsl:stylesheet>
