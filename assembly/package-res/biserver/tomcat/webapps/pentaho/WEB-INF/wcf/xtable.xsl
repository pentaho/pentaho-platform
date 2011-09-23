<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
stylesheet for wcf table component
-->

<xsl:param name="table-border" select="'1'"/>

<xsl:template name="xtable-renderId">
  <xsl:choose>
    <xsl:when test="@renderId">
      <xsl:value-of select="@renderId"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$renderId"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="xtable-border">
  <xsl:choose>
    <xsl:when test="@border">
      <xsl:value-of select="@border"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$table-border"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--
  for handwritten xtable element where the table is part of
  a form
-->

<xsl:template match="xtable">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="xtable-component">
  <table cellspacing="0" cellpadding="2">
    <xsl:if test="@width">
      <xsl:attribute name="width">
        <xsl:value-of select="@width"/>
      </xsl:attribute>
    </xsl:if>

    <xsl:attribute name="id">
      <xsl:call-template name="xtable-renderId"/>
    </xsl:attribute>
    <xsl:attribute name="border">
      <xsl:call-template name="xtable-border"/>
    </xsl:attribute>

    <xsl:call-template name="xtable-title"/>
    <xsl:call-template name="xtable-error"/>
    <xsl:apply-templates/>
  </table>
</xsl:template>


<xsl:template name="xtable-title">
  <xsl:if test="@title or @closeId or @editId">
    <tr>
      <th colspan="{@visibleColumns}" class="xtable-title">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <xsl:if test="@editId">
              <th align="left" class="xtable-title">
                <input type="image" src="{$context}/wcf/table/edit.png" name="{@editId}" width="16" height="16"/>
              </th>
            </xsl:if>
            <xsl:if test="@title">
              <th align="left" class="xtable-title" width="100%">
                <xsl:text>&#160;</xsl:text>
                <xsl:value-of select="@title"/>
              </th>
            </xsl:if>
            <xsl:if test="@closeId">
              <td align="right" class="xtable-title">
                <input type="image" src="{$context}/wcf/form/cancel.png" name="{@closeId}" width="16" height="16"/>
              </td>
            </xsl:if>
          </tr>
        </table>
      </th>
    </tr>
  </xsl:if>
</xsl:template>


<xsl:template name="xtable-error">
  <xsl:if test="@error">
    <tr>
      <th colspan="{@visibleColumns}" class="xtable-error">
        <xsl:value-of select="@error"/>
      </th>
    </tr>
  </xsl:if>
</xsl:template>

<xsl:template match="xtr">
  <tr>
    <xsl:apply-templates/>
  </tr>
</xsl:template>

<!--  
  setzt das CSS class attribut aus dem  DOM oder aus dem Parameter. Hat die
  Applikation ein class Attribut in den DOM geschrieben wird dieses genommen,
  andernfalls der default-wert (= Parameter).
-->
<xsl:template name="set-class">
  <xsl:param name="class"/>
  <xsl:choose>
    <xsl:when test="@class">
      <xsl:attribute name="class">
        <xsl:value-of select="@class"/>
      </xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
      <xsl:attribute name="class">
        <xsl:value-of select="$class"/>
      </xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xtd">
  <td>
    <xsl:call-template name="set-class">
      <xsl:with-param name="class">xtable-data</xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="@align"/>
    <xsl:apply-templates select="@colspan"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>

<xsl:template match="xth[@selectId]">
  <th>
    <xsl:call-template name="set-class">
      <xsl:with-param name="class">xtable-heading</xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="@colspan"/>
    <input type="image" border="0" name="{@selectId}" src="{$context}/wcf/table/select.png" width="16" height="16"/>
    <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="xth[@sort]">
  <th>
    <xsl:call-template name="set-class">
      <xsl:with-param name="class">xtable-heading</xsl:with-param>
    </xsl:call-template>
    <input type="image" border="0" name="{@id}" src="{$context}/wcf/table/sort-{@sort}.png" width="9" height="9"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="xth">
  <th>
    <xsl:call-template name="set-class">
      <xsl:with-param name="class">xtable-heading</xsl:with-param>
    </xsl:call-template>
    <xsl:apply-templates select="@colspan"/>
    <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="ximgcell[@href]">
  <a href="{@href}">
    <img src="{@src}" border="0" alt="{@alt}"/>
  </a>
</xsl:template>

<xsl:template match="ximgcell">
  <img src="{@src}" border="0" alt="{@alt}"/>
</xsl:template>

<xsl:template match="xpagenav">
  <input type="image" border="0" name="{@id}" src="{$context}/wcf/table/page-{@direction}.png" width="16" height="16"/>
</xsl:template>

<xsl:template match="xgotopage">
  <xsl:text> </xsl:text>
  <xsl:value-of select="@label"/>
  <xsl:text> </xsl:text>
  <input type="text" name="{@inputId}" value="{@value}" maxlength="3" size="3"/>
  <xsl:text> </xsl:text>
  <input type="image" border="0" name="{@buttonId}" src="{$context}/wcf/table/gotopage.png" width="16" height="16"/>
</xsl:template>

</xsl:stylesheet>
