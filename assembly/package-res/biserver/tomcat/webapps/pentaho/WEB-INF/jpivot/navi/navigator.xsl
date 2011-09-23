<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="no" encoding="US-ASCII"/>
<xsl:param name="context"/>
<xsl:param name="renderId"/>
<xsl:param name="token"/>
<xsl:param name="pivotId"/>

<xsl:include href="../../wcf/controls.xsl"/>

<!-- buttons with spaces inbetween -->
<xsl:template match="button[@hidden='true']"/>
<xsl:template match="button">
  <xsl:text> </xsl:text>
  <input type="submit" name="{@id}" value="{@label}"/>
  <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="tree-extras-top | tree-extras-bottom">
  <tr>
    <td class="navi-hier">
      <xsl:apply-templates/>
    </td>
  </tr>
</xsl:template>

<xsl:include href="../../wcf/changeorder.xsl"/>
<xsl:include href="hierarchy-navigator.xsl"/>
<xsl:include href="../../wcf/xtree.xsl"/>
<xsl:include href="../../wcf/identity.xsl"/>

</xsl:stylesheet>
