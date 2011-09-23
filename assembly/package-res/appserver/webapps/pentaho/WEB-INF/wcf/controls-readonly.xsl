<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- ignore buttons -->
<xsl:template match="buttons"/>
<xsl:template match="button"/>
<xsl:template match="imgButton"/>

<xsl:template match="label[@value]">
  <xsl:value-of select="@value"/>
</xsl:template>

<xsl:template match="label">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="textField | textArea">
  <xsl:value-of select="@value"/>
</xsl:template>

<xsl:template match="password">
  <xsl:text>****</xsl:text>
</xsl:template>

<xsl:template match="fileUpload">
  <xsl:value-of select="@filename"/>
</xsl:template>

<xsl:template match="checkBox[@selected='true']">
  <xsl:text>X</xsl:text>
</xsl:template>

<xsl:template match="checkBox"/>

<xsl:template match="radioButton[@selected='true']">
  <xsl:text>X</xsl:text>
</xsl:template>

<xsl:template match="radioButton"/>


<xsl:template match="listBox1">
  <xsl:for-each select="listItem[@selected='true']">
    <xsl:value-of select="@label"/>
  </xsl:for-each>
</xsl:template>


<xsl:template match="listBoxN">
  <ul>
    <xsl:for-each select="listItem[@selected='true']">
      <li>
        <xsl:value-of select="@label"/>
      </li>
    </xsl:for-each>
  </ul>
</xsl:template>

</xsl:stylesheet>
