<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="no" encoding="US-ASCII"/>

<!-- transform for chart image tag -->

<!-- xchart tag is root -->
<xsl:template match="xchart">
  <xsl:apply-templates/>
</xsl:template>


<xsl:template match="area">
     <xsl:copy>
     	<xsl:apply-templates select="*|@*|node()"/>
     </xsl:copy>
</xsl:template>

<xsl:template match="*|@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
