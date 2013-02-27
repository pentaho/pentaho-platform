<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:fo="http://www.w3.org/1999/XSL/Format"
exclude-result-prefixes="fo">

<xsl:param name="renderId"/>
<xsl:param name="context"/>
<xsl:param name="imgpath" select="'jpivot/table'"/>

<xsl:param name="chartimage"/>
<xsl:param name="chartheight"/>
<xsl:param name="chartwidth"/>
<xsl:param name="chartPageBreak"/>

<xsl:param name="tableWidth"/>
<xsl:param name="reportTitle"/>

<xsl:param name="pageHeight"/>
<xsl:param name="pageWidth"/>
<xsl:param name="pageOrientation"/>
<xsl:param name="paper.type" select="'A4'"></xsl:param>

<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes" encoding="US-ASCII"/>
    <xsl:template match="mdxtable">
    <!-- make a try at calculating the page size -->
    <xsl:variable name="page.width.calculated">
        <xsl:for-each select="/mdxtable/body/row">
            <xsl:sort select="count(child::*)" data-type="number" order="descending" />
            <xsl:if test="position() = 1">
                <xsl:value-of select = "concat(count(child::*)*3.8,'cm')"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <!-- set page width -->
    <xsl:variable name="page.width.portrait">
      <xsl:choose>
        <xsl:when test="$paper.type = 'USletter'">8.5in</xsl:when>
        <xsl:when test="$paper.type = '4A0'">1682mm</xsl:when>
        <xsl:when test="$paper.type = '2A0'">1189mm</xsl:when>
        <xsl:when test="$paper.type = 'A0'">841mm</xsl:when>
        <xsl:when test="$paper.type = 'A1'">594mm</xsl:when>
        <xsl:when test="$paper.type = 'A2'">420mm</xsl:when>
        <xsl:when test="$paper.type = 'A3'">297mm</xsl:when>
        <xsl:when test="$paper.type = 'A4'">210mm</xsl:when>
        <xsl:when test="$paper.type = 'A5'">148mm</xsl:when>
        <xsl:when test="$paper.type = 'A6'">105mm</xsl:when>
        <xsl:when test="$paper.type = 'A7'">74mm</xsl:when>
        <xsl:when test="$paper.type = 'A8'">52mm</xsl:when>
        <xsl:when test="$paper.type = 'A9'">37mm</xsl:when>
        <xsl:when test="$paper.type = 'A10'">26mm</xsl:when>
        <xsl:when test="$paper.type = 'B0'">1000mm</xsl:when>
        <xsl:when test="$paper.type = 'B1'">707mm</xsl:when>
        <xsl:when test="$paper.type = 'B2'">500mm</xsl:when>
        <xsl:when test="$paper.type = 'B3'">353mm</xsl:when>
        <xsl:when test="$paper.type = 'B4'">250mm</xsl:when>
        <xsl:when test="$paper.type = 'B5'">176mm</xsl:when>
        <xsl:when test="$paper.type = 'B6'">125mm</xsl:when>
        <xsl:when test="$paper.type = 'B7'">88mm</xsl:when>
        <xsl:when test="$paper.type = 'B8'">62mm</xsl:when>
        <xsl:when test="$paper.type = 'B9'">44mm</xsl:when>
        <xsl:when test="$paper.type = 'B10'">31mm</xsl:when>
        <xsl:when test="$paper.type = 'C0'">917mm</xsl:when>
        <xsl:when test="$paper.type = 'C1'">648mm</xsl:when>
        <xsl:when test="$paper.type = 'C2'">458mm</xsl:when>
        <xsl:when test="$paper.type = 'C3'">324mm</xsl:when>
        <xsl:when test="$paper.type = 'C4'">229mm</xsl:when>
        <xsl:when test="$paper.type = 'C5'">162mm</xsl:when>
        <xsl:when test="$paper.type = 'C6'">114mm</xsl:when>
        <xsl:when test="$paper.type = 'C7'">81mm</xsl:when>
        <xsl:when test="$paper.type = 'C8'">57mm</xsl:when>
        <xsl:when test="$paper.type = 'C9'">40mm</xsl:when>
        <xsl:when test="$paper.type = 'C10'">28mm</xsl:when>
        <xsl:when test="$paper.type = 'custom'">
            <xsl:value-of select="concat($pageWidth,'cm')"/>
        </xsl:when>
        <xsl:when test="$paper.type = 'auto'">
             <xsl:value-of select="$page.width.calculated"/>
        </xsl:when>
        <xsl:otherwise>8.5in</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- set page height -->
    <xsl:variable name="page.height.portrait">
      <xsl:choose>
        <xsl:when test="$paper.type = 'A4landscape'">210mm</xsl:when>
        <xsl:when test="$paper.type = 'USletter'">11in</xsl:when>
        <xsl:when test="$paper.type = 'USlandscape'">8.5in</xsl:when>
        <xsl:when test="$paper.type = '4A0'">2378mm</xsl:when>
        <xsl:when test="$paper.type = '2A0'">1682mm</xsl:when>
        <xsl:when test="$paper.type = 'A0'">1189mm</xsl:when>
        <xsl:when test="$paper.type = 'A1'">841mm</xsl:when>
        <xsl:when test="$paper.type = 'A2'">594mm</xsl:when>
        <xsl:when test="$paper.type = 'A3'">420mm</xsl:when>
        <xsl:when test="$paper.type = 'A4'">297mm</xsl:when>
        <xsl:when test="$paper.type = 'A5'">210mm</xsl:when>
        <xsl:when test="$paper.type = 'A6'">148mm</xsl:when>
        <xsl:when test="$paper.type = 'A7'">105mm</xsl:when>
        <xsl:when test="$paper.type = 'A8'">74mm</xsl:when>
        <xsl:when test="$paper.type = 'A9'">52mm</xsl:when>
        <xsl:when test="$paper.type = 'A10'">37mm</xsl:when>
        <xsl:when test="$paper.type = 'B0'">1414mm</xsl:when>
        <xsl:when test="$paper.type = 'B1'">1000mm</xsl:when>
        <xsl:when test="$paper.type = 'B2'">707mm</xsl:when>
        <xsl:when test="$paper.type = 'B3'">500mm</xsl:when>
        <xsl:when test="$paper.type = 'B4'">353mm</xsl:when>
        <xsl:when test="$paper.type = 'B5'">250mm</xsl:when>
        <xsl:when test="$paper.type = 'B6'">176mm</xsl:when>
        <xsl:when test="$paper.type = 'B7'">125mm</xsl:when>
        <xsl:when test="$paper.type = 'B8'">88mm</xsl:when>
        <xsl:when test="$paper.type = 'B9'">62mm</xsl:when>
        <xsl:when test="$paper.type = 'B10'">44mm</xsl:when>
        <xsl:when test="$paper.type = 'C0'">1297mm</xsl:when>
        <xsl:when test="$paper.type = 'C1'">917mm</xsl:when>
        <xsl:when test="$paper.type = 'C2'">648mm</xsl:when>
        <xsl:when test="$paper.type = 'C3'">458mm</xsl:when>
        <xsl:when test="$paper.type = 'C4'">324mm</xsl:when>
        <xsl:when test="$paper.type = 'C5'">229mm</xsl:when>
        <xsl:when test="$paper.type = 'C6'">162mm</xsl:when>
        <xsl:when test="$paper.type = 'C7'">114mm</xsl:when>
        <xsl:when test="$paper.type = 'C8'">81mm</xsl:when>
        <xsl:when test="$paper.type = 'C9'">57mm</xsl:when>
        <xsl:when test="$paper.type = 'C10'">40mm</xsl:when>
        <xsl:when test="$paper.type = 'custom'">
            <xsl:value-of select="concat($pageHeight,'cm')"/>
        </xsl:when>
        <xsl:otherwise>11in</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

  <xsl:processing-instruction name="cocoon-format">type="text/xslfo"</xsl:processing-instruction>
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master
                    master-name="simple"
                    margin="1cm">

                    <!-- swap height/width for landscape -->
                    <xsl:attribute name="page-height">
                      <xsl:choose>
                        <xsl:when test="$pageOrientation = 'portrait'">
                          <xsl:value-of select="$page.height.portrait"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="$page.width.portrait"></xsl:value-of>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:attribute name="page-width">
                      <xsl:choose>
                        <xsl:when test="$pageOrientation = 'portrait'">
                          <xsl:value-of select="$page.width.portrait"></xsl:value-of>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="$page.height.portrait"></xsl:value-of>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>

                    <fo:region-before extent="1cm"/>
                    <fo:region-after extent="1cm"/>
                    <fo:region-body margin-bottom="1.5cm" margin-top="1.5cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="simple">
            <!-- Page header -->
                <fo:static-content flow-name="xsl-region-before">
                  <fo:block border-bottom-width="1pt"
                            border-bottom-style="solid"
                            border-bottom-color="black"
                            font-weight="bold"
                            font-size="8pt">
                    <!-- retrieve current page title -->
                    <fo:retrieve-marker retrieve-class-name="page-head"/>
                  </fo:block>
                </fo:static-content>

                <!-- Page footer -->
                <fo:static-content flow-name="xsl-region-after">
                  <fo:block padding-top="3pt"
                            font-weight="bold"
                            font-size="8pt"
                            text-align="center">
                    <fo:inline font-style="italic"> INSERT FOOTER TEXT HERE</fo:inline>
                  </fo:block>
                </fo:static-content>

                <!-- Page content -->
                <fo:flow flow-name="xsl-region-body">
                  <fo:block font-size="8pt"
                        font-family="Verdana, Geneva, Arial, Helvetica, sans-serif">
                            <fo:block>
                              <fo:marker marker-class-name="page-head">
                                <!-- FOP compliant implementation, should use fo:leader with later versions -->
                                <fo:table table-layout="fixed" width="100%">
                                  <fo:table-column column-width="proportional-column-width(4)"/>
                                  <fo:table-column column-width="proportional-column-width(1)"/>
                                  <fo:table-body>
                                    <fo:table-row>
                                      <fo:table-cell>
                                        <fo:block text-align="left">
                                           HEADER TEXT (e.g Company Name)
                                        </fo:block>
                                      </fo:table-cell>
                                      <fo:table-cell>
                                        <fo:block text-align="right">
                                          Page
                                          <fo:page-number/>
                                          of
                                          <fo:page-number-citation ref-id="EndOfDocument"/>
                                        </fo:block>
                                      </fo:table-cell>
                                    </fo:table-row>
                                  </fo:table-body>
                                </fo:table>
                              </fo:marker>
                            </fo:block>

                        <!-- report title -->
                        <fo:block font-size="12pt" font-weight="bold" text-align="center" space-after="1em">
                          <xsl:value-of select = "$reportTitle"/>
                        </fo:block>

                          <!-- chart on first page only -->
                            <xsl:if test = "$chartimage">
                                <fo:block text-align="center" space-after="8em">
                                    <fo:external-graphic xmlns:fo="http://www.w3.org/1999/XSL/Format" scaling="uniform">
                                     <xsl:attribute name="content-height">
                                        <xsl:value-of select ="concat($chartheight,'px')"/>
                                    </xsl:attribute>
                                     <xsl:attribute name="content-width">
                                        <xsl:value-of select ="concat($chartwidth,'px')"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select ="concat(concat('url(',$chartimage),')')"/>
                                    </xsl:attribute>
                                    </fo:external-graphic>
                                 </fo:block>

                                <xsl:if test="$chartPageBreak='true'">
                                    <fo:block break-after="page"/>
                                </xsl:if>
                            </xsl:if>

                            <fo:block font-size="10pt">
                                <fo:table table-layout="fixed">
                                <xsl:attribute name="width">
                                        <xsl:choose>
                                        <xsl:when test="$tableWidth">
                                            <xsl:value-of select ="concat($tableWidth,'cm')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select = "'100%'"/>
                                        </xsl:otherwise>
                                        </xsl:choose>
                                </xsl:attribute>
                                <!-- for each corner, create #columns=col-span -->
                                    <xsl:for-each select="/mdxtable/body/row">
                                        <xsl:sort select="count(cell)" data-type="number" order="descending" />
                                        <xsl:if test="position() = 1">
                                            <xsl:call-template name="do_columns">
                                                <xsl:with-param name="colnum">
                                                <!-- if there is a corner, then this works, otherwise need to count row heading-heading colspans -->
                                                    <xsl:choose>
                                                        <xsl:when test="count(/mdxtable/head/row/corner)=0">
                                                            <xsl:value-of select="sum(/mdxtable/head/row/heading-heading/@colspan)" />
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="count(cell) + /mdxtable/head/row/corner/@colspan" />
                                                        </xsl:otherwise>
                                                     </xsl:choose>
                                                </xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:if>
                                    </xsl:for-each>
                                    <fo:table-body>
                                            <xsl:apply-templates select="head"/>
                                            <xsl:apply-templates select="body"/>
                                    </fo:table-body>
                                </fo:table>
                            </fo:block>
                            <!--<fo:block text-align="center" id="EndOfDocument"/>-->
                        </fo:block>
                     <fo:block text-align="center" id="EndOfDocument"/>
                 </fo:flow>
            </fo:page-sequence>
        </fo:root>
</xsl:template>

 <xsl:template name="calcwidth">
 <xsl:param name="col"/>

    <xsl:for-each select="/mdxtable/body/row/*[position() = $col]">
         <xsl:sort select="string-length(@value)" data-type="number" order="descending" />
           <xsl:if test="position() = 1">
                <xsl:variable name="colmax" select="string-length(@value)*5"/>
                        <xsl:choose>
                            <xsl:when test="$colmax &gt; 36">
                                <xsl:value-of select="concat($colmax,'px')"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'3cm'"/>
                            </xsl:otherwise>
                        </xsl:choose>
             </xsl:if>
    </xsl:for-each>

 </xsl:template>

 <xsl:template name="do_columns">
 <xsl:param name="colnum"/>
 <xsl:param name="num">1</xsl:param>
     <xsl:if test="not($num > $colnum)">
        <xsl:text></xsl:text>
        <fo:table-column  column-width="proportional-column-width(1)"/>
          <!--<xsl:attribute name="column-width">
                <xsl:call-template name="calcwidth">
                    <xsl:with-param name="col">
                        <xsl:value-of select="$num"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:attribute>
        </fo:table-column>-->
        <xsl:call-template name="do_columns">
             <xsl:with-param name="num">
                <xsl:value-of select="$num + 1"/>
            </xsl:with-param>
            <xsl:with-param name="colnum">
                <xsl:value-of select="$colnum"/>
            </xsl:with-param>
        </xsl:call-template>
     </xsl:if>
 </xsl:template>

<xsl:template name="odd-even">
<xsl:param name="style"/>
        <xsl:attribute name= "background-color">
            <xsl:choose>
               <xsl:when test='$style="even"'>
                 <xsl:value-of select = "'#f0f0f0'"/>
               </xsl:when>
               <xsl:otherwise>
               <xsl:value-of select = "'#FFFFFF'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
</xsl:template>

<!--
<xsl:template name="odd-even-heading">
<xsl:param name="style"/>
        <xsl:attribute name= "background-color">
            <xsl:choose>
               <xsl:when test='$style="even"'>
                 <xsl:value-of select = "'#dee3ef'"/>
               </xsl:when>
               <xsl:otherwise>
               <xsl:value-of select = "'#eef3ff'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
</xsl:template>-->

<xsl:template name="setcellcolor">
    <xsl:attribute name="background-color">
      <xsl:if test="count(../../preceding-sibling::*) mod 2 = 1">
        <xsl:choose>
         <xsl:when test="count(/mdxtable/head/row) mod 2 = 0">
                <xsl:choose>
                <!-- choose local-name='cell' / otherwise -->
                     <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
                        <xsl:value-of select = "'#dee3ef'"/>
                     </xsl:when>
                    <xsl:otherwise>
                       <xsl:value-of select = "'#eef3ff'"/>
                    </xsl:otherwise>
                  </xsl:choose>
           </xsl:when>
           <xsl:otherwise>
                <xsl:choose>
                <!-- choose local-name='cell' / otherwise -->
                    <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
                        <xsl:value-of select = "'#eef3ff'"/>
                    </xsl:when>
                    <xsl:otherwise>
                       <xsl:value-of select = "'#dee3ef'"/>
                    </xsl:otherwise>
                </xsl:choose>
           </xsl:otherwise>
        </xsl:choose>
       </xsl:if>

       <xsl:if test="count(../../preceding-sibling::*) mod 2 = 0">
        <xsl:choose>
           <xsl:when test="count(../preceding-sibling::*) mod 2 = 1">
             <xsl:value-of select = "'#eef3ff'"/>
           </xsl:when>
           <xsl:otherwise>
               <xsl:value-of select = "'#dee3ef'"/>
           </xsl:otherwise>
         </xsl:choose>
       </xsl:if>
      </xsl:attribute>
</xsl:template>

<xsl:template match="head | body">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="row">
  <fo:table-row> <!--<tr>-->
    <xsl:apply-templates/>
  </fo:table-row><!--</tr>-->
</xsl:template>


<xsl:template match="corner">
  <!--<th nowrap="nowrap" class="corner-heading" colspan="{@colspan}" rowspan="{@rowspan}">-->
  <fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm" background-color="#FFFFFF">
    <xsl:apply-templates/>
    <!-- &#160; == &nbsp; -->
    <xsl:text></xsl:text>
    <!--</th>-->
  </fo:table-cell>

</xsl:template>


<xsl:template match="column-heading[@indent]">
  <!--<th nowrap="nowrap" class="column-heading-{@style}" colspan="{@colspan}" rowspan="{@rowspan}">-->
  <fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm" background-color="#FFFFFF">
      <xsl:call-template name="setcellcolor">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
     </xsl:call-template>
  <fo:block text-align="left" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.5mm" space-after="0.5mm">
    <xsl:if test="@indent">
        <xsl:attribute name= "text-indent">
        <xsl:value-of select  = "concat(@indent,'mm')"/>
      </xsl:attribute>
    </xsl:if>
        <xsl:apply-templates/>
    </fo:block>
   <!--</th>-->
  </fo:table-cell>
</xsl:template>


<xsl:template match="row-heading[@indent]">
<!--  <th nowrap="nowrap" class="row-heading-{@style}" colspan="{@colspan}" rowspan="{@rowspan}">-->
<fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm">
    <xsl:call-template name="setcellcolor">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
     </xsl:call-template>
    <fo:block text-align="left" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.5mm" space-after="0.5mm">
    <xsl:if test="@indent">
        <xsl:attribute name= "text-indent">
        <xsl:value-of select  = "concat(@indent,'mm')"/>
      </xsl:attribute>
    </xsl:if>
      <xsl:apply-templates/>
    </fo:block>
   <!--</th>-->
  </fo:table-cell>
</xsl:template>


<xsl:template match="column-heading">
<!--  <th nowrap="nowrap" class="column-heading-{@style}" colspan="{@colspan}" rowspan="{@rowspan}">-->
<fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm" background-color="#FFFFFF">
      <xsl:call-template name="setcellcolor">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
     </xsl:call-template>
<fo:block text-align="left" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.5mm" space-after="0.5mm">
    <xsl:if test="@indent">
        <xsl:attribute name= "text-indent">
        <xsl:value-of select  = "concat(@indent,'mm')"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
    </fo:block>
   <!--</th>-->
  </fo:table-cell>
</xsl:template>


<xsl:template match="row-heading">
 <!--<th nowrap="nowrap" class="row-heading-{@style}" colspan="{@colspan}" rowspan="{@rowspan}">-->
 <fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm">
         <xsl:call-template name="setcellcolor">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
        </xsl:call-template>
 <fo:block text-align="left" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.5mm" space-after="0.5mm">
    <xsl:if test="@indent">
        <xsl:attribute name= "text-indent">
        <xsl:value-of select  = "concat(@indent,'mm')"/>
      </xsl:attribute>
    </xsl:if>
      <xsl:apply-templates/>
    </fo:block>
   <!--</th>-->
  </fo:table-cell>
</xsl:template>


<xsl:template match="heading-heading">
 <!--<th nowrap="nowrap" class="heading-heading" colspan="{@colspan}" rowspan="{@rowspan}">-->
 <fo:table-cell number-columns-spanned="{@colspan}" number-rows-spanned="{@rowspan}" border-style="solid" border-width="0.2mm" background-color="#FFFFFF">
     <xsl:call-template name="setcellcolor">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
     </xsl:call-template>

<fo:block text-align="left" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.5mm" space-after="0.5mm">
    <xsl:if test="@indent">
        <xsl:attribute name= "text-indent">
        <xsl:value-of select  = "concat(@indent,'mm')"/>
      </xsl:attribute>
    </xsl:if>
      <xsl:apply-templates/>
    </fo:block>
   <!--</th>-->
  </fo:table-cell>
</xsl:template>


<!-- caption of a member in row/column heading -->
<xsl:template match="caption[@href]">
  <!--<a href="{@href}">-->
    <xsl:value-of select="@caption"/>
  <!--</a>-->
</xsl:template>


<xsl:template match="caption">
  <xsl:call-template name="render-label">
    <xsl:with-param name="label">
      <xsl:value-of select="@caption"/>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>


<!-- navigation: expand / collapse / leaf node -->
<xsl:template match="drill-expand | drill-collapse">
  <!--<input type="image" title="{@title}" name="{@id}" src="{$context}/{$imgpath}/{@img}.gif" border="0" width="9" height="9"/>-->
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


<xsl:template match="cell">
    <fo:table-cell border="solid black 0.5pt" padding="2pt" border-collapse="collapse"> <!--border-style="solid" border-width="0.2mm"> -->
        <xsl:call-template name="odd-even">
            <xsl:with-param name="style">
                <xsl:value-of select="@style" />
            </xsl:with-param>
        </xsl:call-template>
         <fo:block text-align="right" padding="2pt" font-size="10pt" font-family="serif" line-height="14pt" space-before="0.2mm" space-after="0.2mm" text-indent="1mm">
        <!--<td nowrap="nowrap" class="cell-{@style}">-->
            <xsl:apply-templates select="drill-through"/>
            <xsl:call-template name="render-label">
              <xsl:with-param name="label">
                <xsl:value-of select="@value"/>
              </xsl:with-param>
            </xsl:call-template>
      <!--</td>-->
        </fo:block>
    </fo:table-cell>

</xsl:template>


<xsl:template name="render-label">
  <xsl:param name="label"/>
  <xsl:choose>
    <xsl:when test="property[@name='link']">
      <!--<a href="{property[@name='link']/@value}" target="_blank">
        <xsl:value-of select="$label"/>
        <xsl:apply-templates select="property"/>
      </a>-->
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$label"/>
      <xsl:apply-templates select="property"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="property[@name='arrow']">
  <!--
  <span style="margin-left: 0.5ex">
    <img border="0" src="{$context}/{$imgpath}/arrow-{@value}.gif" width="10" height="10"/>
  </span>
  -->
</xsl:template>

<xsl:template match="property[@name='image']">
  <!--
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
  -->
</xsl:template>

<xsl:template match="property[@name='cyberfilter']">
<!--
  <span style="margin-left: 0.5ex">
    <img align="middle" src="{$context}/{$imgpath}/filter-{@value}.gif" width="53" height="14"/>
  </span>
  -->
</xsl:template>

<xsl:template match="property"/>

<xsl:template match="*|@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>