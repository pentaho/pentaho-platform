<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="popup-menu">
    <a href="#" onMouseover="cssdropdown.dropit(this, event, '{@id}')">
      <xsl:call-template name="image-name" />
    </a>
    <div id="{@id}" class="dropmenudiv">
      <xsl:apply-templates />
    </div>
  </xsl:template>

  <xsl:template match="popup-group">
    <strong style="padding-left: {@level}em">
      <xsl:call-template name="image-name" />
    </strong>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="popup-item">
    <a href="{@href}" style="padding-left: {@level}em">
      <xsl:call-template name="image-name" />
    </a>
  </xsl:template>

  <xsl:template name="image-name">
    <xsl:if test="@image">
      <img border="0" src="{$context}{@image}" />
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:value-of select="@label" />
  </xsl:template>

</xsl:stylesheet>
