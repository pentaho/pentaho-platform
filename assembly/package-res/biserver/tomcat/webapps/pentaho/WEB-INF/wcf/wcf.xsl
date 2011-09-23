<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="no" encoding="US-ASCII"/>
<xsl:param name="context"/>
<xsl:param name="renderId"/>
<xsl:param name="token"/>

<xsl:include href="catedit.xsl"/>
<xsl:include href="changeorder.xsl"/>
<xsl:include href="controls.xsl"/>
<xsl:include href="xform.xsl"/>
<xsl:include href="xtable.xsl"/>
<xsl:include href="xtree.xsl"/>
<xsl:include href="xtabbed.xsl"/>
<xsl:include href="popup.xsl"/>

<xsl:template match="skip[@hidden='true']"/>

<xsl:template match="skip">
  <xsl:apply-templates/>
</xsl:template>

<!-- identity transform -->
<xsl:template match="*|@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>