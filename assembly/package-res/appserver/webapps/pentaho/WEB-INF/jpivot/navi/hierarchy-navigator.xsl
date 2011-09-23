<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="cat-edit">
  <table cellpadding="1" cellspacing="0" border="1" id="{$renderId}">
    <xsl:apply-templates select="cat-category"/>
    <!-- buttons -->
    <tr>
      <td align="right">
        <div align="right">
          <input type="submit" value="{@ok-title}" name="{@ok-id}"/>
          <xsl:text> </xsl:text>
          <input type="submit" value="{@cancel-title}" name="{@cancel-id}"/>
        </div>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="cat-category">
  <tr>
    <th align="left" class="navi-axis">
      <xsl:choose>
        <!-- the first category gets the close button -->
        <xsl:when test="position() = 1">

          <table border="0" cellspacing="0" cellpadding="0" width="100%">
            <tr>
              <th align="left" class="navi-axis">
                <img src="{$context}/jpivot/navi/{@icon}" width="9" height="9"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@name"/>
              </th>
              <td align="right" class="xform-close-button">
                <input type="image" src="{$context}/wcf/form/cancel.png" value="{../@cancel-title}" name="{../@cancel-id}" width="16" height="16"/>
              </td>
            </tr>
          </table>
        </xsl:when>
        <xsl:otherwise>
          <img src="{$context}/jpivot/navi/{@icon}" width="9" height="9"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="@name"/>
        </xsl:otherwise>
      </xsl:choose>
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
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="@id">
            <a href="?pivotId={$pivotId}&amp;{$token}&amp;{@id}=x">
              <xsl:value-of select="@name"/>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="property-button"/>
        <xsl:apply-templates select="function-button"/>
        <xsl:apply-templates select="slicer-value"/>
      </div>
    </td>
  </tr>
</xsl:template>

<xsl:template match="slicer-value">
  <xsl:text> (</xsl:text>
  <xsl:value-of select="@level"/>
  <xsl:text>=</xsl:text>
  <xsl:value-of select="@label"/>
  <xsl:text>)</xsl:text>
</xsl:template>

<xsl:template match="cat-button[@icon]">
  <input border="0" type="image" src="{$context}/jpivot/navi/{@icon}" name="{@id}" width="9" height="9"/>
  <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="cat-button">
  <img src="{$context}/jpivot/navi/empty.png" width="9" height="9"/>
  <xsl:text> </xsl:text>
</xsl:template>


<xsl:template match="property-button">
  <input border="0" type="image" src="{$context}/jpivot/navi/properties.png" name="{@id}"/>
  <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="function-button">
  <input border="0" type="image" src="{$context}/jpivot/navi/functions.png" name="{@id}"/>
  <xsl:text> </xsl:text>
</xsl:template>


</xsl:stylesheet>
