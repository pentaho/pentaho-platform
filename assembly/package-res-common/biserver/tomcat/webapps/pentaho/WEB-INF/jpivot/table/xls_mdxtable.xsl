<?xml version="1.0" encoding="iso-8859-1"?>

<!-- renders the JPivot Table -->

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://www.w3.org/TR/REC-html40"
xmlns:x="urn:schemas-microsoft-com:office:excel" >

<!-- the id of the table for httpUnit -->
<xsl:param name="renderId"/>
<xsl:param name="context"/>
<xsl:param name="imgpath" select="'jpivot/table'"/>
<xsl:param name="chartimage"/>
<xsl:param name="chartheight" select="'50'"/>
<xsl:param name="chartwidth" select="'150'"/>
<xsl:param name="reportTitle"/>

<xsl:output method="html" indent="yes" encoding="US-ASCII"/>

<xsl:template match="mdxtable">
    <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
        <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta name="ProgId content=Excel.Sheet"/>
          <style>
            table
            {mso-displayed-decimal-separator:"\.";
            mso-displayed-thousand-separator:"\,";}

            @page
                    {margin:1.0in .75in 1.0in .75in;
                    mso-header-margin:.5in;
                    mso-footer-margin:.5in;}
            tr
                    {mso-height-source:auto;}
            col
                    {mso-width-source:auto;}
            br
                    {mso-data-placement:same-cell;}
            .basestyle
                    {mso-number-format:General;
                    text-align:general;
                    vertical-align:bottom;
                    white-space:nowrap;
                    mso-rotate:0;
                    mso-background-source:auto;
                    mso-pattern:auto;
                    color:windowtext;
                    font-size:10.0pt;
                    font-weight:400;
                    font-style:normal;
                    text-decoration:none;
                    font-family:Arial;
                    mso-generic-font-family:auto;
                    mso-font-charset:0;
                    border:none;
                    mso-protection:locked visible;
                    mso-style-name:Normal;
                    mso-style-id:0;}

              .col-heading
                    {mso-style-parent:basestyle;
                    font-weight:700;
                    font-family:"Arial Unicode MS";
                    mso-generic-font-family:auto;
                    mso-font-charset:0;
                    text-align:left;
                    vertical-align:middle;
                    border:.5pt solid black;
                    mso-pattern:auto none;}

              .row-heading
                    {mso-style-parent:basestyle;
                    font-weight:700;
                    font-family:"Arial Unicode MS";
                    mso-generic-font-family:auto;
                    mso-font-charset:0;
                    text-align:left;
                    vertical-align:top;
                    border-top:.5pt solid black;
                    border-right:.5pt solid black;
                    border-bottom:none;
                    border-left:.5pt solid black;
                    mso-pattern:auto none;}

              .dataitem
                    {mso-style-parent:basestyle;
                    font-family:"Arial Unicode MS";
                    mso-generic-font-family:auto;
                    mso-font-charset:0;
                    <!--mso-number-format:"\#\,\#\#0";-->
                    <!-- mso-number-format:Standard;-->
                    text-align:right;
                    vertical-align:top;
                    border:.5pt solid black;
                    background:white;
                    mso-pattern:auto none;}
            </style>

        </head>
        <body>
              <!-- Title -->
              <xsl:if test="$reportTitle">
                <h2><xsl:value-of select="$reportTitle"/></h2>
              </xsl:if>
               <!-- Chart -->
              <xsl:if test = "$chartimage">
                    <p>
                    <img>
                     <xsl:attribute name="height">
                        <xsl:value-of select ="$chartheight"/>
                    </xsl:attribute>
                     <xsl:attribute name="width">
                        <xsl:value-of select ="$chartwidth"/>
                    </xsl:attribute>
                    <xsl:attribute name="src">
                        <xsl:value-of select ="$chartimage"/>
                    </xsl:attribute>
                    </img>
                    </p>
              </xsl:if>
                <!-- Table -->
              <table xmlns:x="urn:schemas-microsoft-com:office:excel" border="1">
                <xsl:apply-templates select="head"/>
                <xsl:apply-templates select="body"/>
              </table>
        </body>
    </html>
</xsl:template>

<xsl:template name="setcellcolor">
    <xsl:attribute name="bgcolor">
    <!-- for row headings -->
      <xsl:if test="count(../../preceding-sibling::*) mod 2 = 1">
        <xsl:choose>
         <xsl:when test="count(//head/row) mod 2 = 0">
                <xsl:choose>
                     <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
                        <xsl:value-of select = "'#C0C0C0'"/>
                     </xsl:when>
                    <xsl:otherwise>
                       <xsl:value-of select = "'#F0F0F0'"/>
                    </xsl:otherwise>
                  </xsl:choose>
           </xsl:when>
           <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
                        <xsl:value-of select = "'#F0F0F0'"/>
                    </xsl:when>
                    <xsl:otherwise>
                       <xsl:value-of select = "'#C0C0C0'"/>
                    </xsl:otherwise>
                </xsl:choose>
           </xsl:otherwise>
        </xsl:choose>
       </xsl:if>
       <!-- for column headings -->
       <xsl:if test="count(../../preceding-sibling::*) mod 2 = 0">
        <xsl:choose>
           <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
             <xsl:value-of select = "'#C0C0C0'"/>
           </xsl:when>
           <xsl:otherwise>
               <xsl:value-of select = "'#F0F0F0'"/>
           </xsl:otherwise>
         </xsl:choose>
       </xsl:if>
      </xsl:attribute>
</xsl:template>

<xsl:template match="head|body">
  <xsl:apply-templates select=".//row"/>
</xsl:template>

<!--  I can determine the row color at row level, but I need it at cell.. how?? -->
<xsl:template match="row">
  <tr>
    <xsl:apply-templates/>
  </tr>
</xsl:template>

<xsl:template match="corner">
  <th text-align="left" vertical-align="top" nowrap="nowrap" bgcolor="#FFFFFF" colspan="{@colspan}" rowspan="{@rowspan}">
    <xsl:apply-templates/>
    <!-- &#160; == &nbsp; -->
    <xsl:text>&#160;</xsl:text>
  </th>
</xsl:template>

<xsl:template match="column-heading[@indent]">
  <th class='col-heading' text-align="left" vertical-align="top" nowrap="nowrap" colspan="{@colspan}" rowspan="{@rowspan}">
  <xsl:call-template name="setcellcolor"/>
      <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="row-heading[@indent]">
  <th class='row-heading' text-align="left" vertical-align="top" nowrap="nowrap" colspan="{@colspan}" rowspan="{@rowspan}">
   <xsl:call-template name="setcellcolor"/>
      <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="column-heading">
  <th class='col-heading' text-align="left" vertical-align="top" nowrap="nowrap" colspan="{@colspan}" rowspan="{@rowspan}">
  <xsl:call-template name="setcellcolor"/>
  <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="row-heading">
  <th class='row-heading' text-align="left" vertical-align="top" nowrap="nowrap" colspan="{@colspan}" rowspan="{@rowspan}">
   <xsl:call-template name="setcellcolor"/>
  <xsl:apply-templates/>
  </th>
</xsl:template>

<xsl:template match="heading-heading">
  <th class='col-heading' text-align="left" vertical-align="top" nowrap="nowrap" colspan="{@colspan}" rowspan="{@rowspan}">
  <xsl:call-template name="setcellcolor"/>
  <xsl:apply-templates/>
  </th>
</xsl:template>

<!-- caption of a member in row/column heading -->
<!--
<xsl:template match="caption[@href]">
  <a href="{@href}">
    <xsl:value-of select="@caption"/>
  </a>
</xsl:template>
-->
<xsl:template match="caption">
  <xsl:call-template name="render-label">
    <xsl:with-param name="label">
      <xsl:value-of select="@caption"/>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- navigation: expand / collapse / leaf node -->

<xsl:template match="drill-expand | drill-collapse">
 <!-- <input type="image" title="{@title}" name="{@id}" src="{$context}/{$imgpath}/{@img}.gif" border="0" width="9" height="9"/>-->
</xsl:template>

<xsl:template match="drill-other">
  <!--<img src="{$context}/{$imgpath}/{@img}.gif" border="0" width="9" height="9"/>-->
</xsl:template>

<!-- navigation: sort -->

<xsl:template match="sort">
  <!--<input name="{@id}" title="{@title}" type="image" src="{$context}/{$imgpath}/{@mode}.gif" border="0" width="9" height="9"/>-->
</xsl:template>

<xsl:template match="drill-through">
  <!--<input name="{@id}" title="{@title}" type="image" src="{$context}/{$imgpath}/drill-through.gif" border="0" width="9" height="9"/>-->
</xsl:template>

<!-- OPENOFFICE cell format -->
<!--
<xsl:template match="cell">
  <td align="right" valign="top" nowrap="nowrap" bgcolor="#FFFFFF">
  <xsl:if test = "not(@value='&#160;')">
      <xsl:if test = "@mso-number-format">
           <xsl:attribute name="class">
            <xsl:value-of select = "'dataitem'"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test = "@rawvalue">
          <xsl:attribute name="SDVAL">
            <xsl:value-of select="@rawvalue"/>
          </xsl:attribute>
          <xsl:attribute name="SDNUM">
            <xsl:value-of select="'1033;1038;# ##0'"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="drill-through"/>
      <xsl:call-template name="render-label">
          <xsl:with-param name="label">
            <xsl:value-of select="@value"/>
          </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </td>
</xsl:template>
-->
<!-- EXCEL cell format -->
<xsl:template match="cell">
  <td align="right" valign="top" nowrap="nowrap" bgcolor="#FFFFFF">

  <xsl:if test = "not(@value='&#160;')">
      <xsl:if test = "@mso-number-format">
            <xsl:attribute name="class">
                <xsl:value-of select = "'dataitem'"/>
            </xsl:attribute>

             <xsl:attribute name="style">
                <xsl:value-of select="concat('mso-number-format:',@mso-number-format)"/>
           </xsl:attribute>

      </xsl:if>
      <xsl:if test = "@rawvalue">
          <xsl:attribute name="x:num">
            <xsl:value-of select="@rawvalue"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="drill-through"/>
      <xsl:call-template name="render-label">
          <xsl:with-param name="label">
            <xsl:value-of select="@value"/>
          </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </td>
</xsl:template>

<xsl:template name="render-label">
  <xsl:param name="label"/>
  <xsl:choose>
    <xsl:when test="property[@name='link']">
      <a href="{property[@name='link']/@value}" target="_blank">
        <xsl:value-of select="$label"/>
        <xsl:apply-templates select="property"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$label"/>
      <xsl:apply-templates select="property"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--
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
-->

<xsl:template match="*|@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
