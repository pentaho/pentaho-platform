<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
  xmlns:html="http://www.w3.org/TR/REC-html40" 
  exclude-result-prefixes="html">

  <xsl:variable name="USEPOSTFORFORMS" select="'false'" />
  <xsl:include href="system/custom/xsl/ParameterFormUtil.xsl" />
  <xsl:param name="baseUrl" select="''" />
  <xsl:param name="actionUrl" select="''" />
  <xsl:param name="displayUrl" select="''" />

  <xsl:output method="html" encoding="UTF-8" />
  
  <xsl:template match="filters">
    <xsl:call-template name="doFilters"/>
  </xsl:template>

  <xsl:template name="doSelections">
    <table cellpadding="8" border="0">
      <tr></tr>
      <xsl:for-each select="filter">
        <xsl:call-template name="doFilter"/>
      </xsl:for-each>
      <xsl:for-each select="error">
        <xsl:value-of select="." />
      </xsl:for-each>
      <xsl:if test="/filters/@parameterView='false'">
        <tr>
              <td class="portlet-font"  style="padding:3px" >
               <table class="parameter_table" border="0" cellpadding="0" cellspacing="0">
                    <tbody><tr>
                       <td>
                          
                            <fieldset class="parameter_fieldset">
                              <legend>
                                  Run in Background
                              </legend>

                              <input id="run_as_background_yes" name="run_as_background" class="portlet-form-field" value="Yes" type="radio"/>
                              <span class="portlet-form-field-label">Yes</span>
                              <input name="run_as_background" class="portlet-form-field" value="No" checked="checked" type="radio"/>
                              <span class="portlet-form-field-label">No</span>
                              
                            </fieldset>
                          </td>
                    </tr>
                 </tbody></table>
                 
              </td>
          </tr>
      </xsl:if> 
    <xsl:apply-templates select="input" />
    </table>
  </xsl:template>

  <xsl:template name="doOptions">
    <xsl:choose>
      <xsl:when test="/filters/@parameterView='false'">
        <tr>
          <td class="portlet-font" colspan="2">
            <div class="run3div"> <!-- run3div -->
              <xsl:attribute name="id">run3div<xsl:value-of select="/filters/id" /></xsl:attribute>
              <table align="right">
                <tr>
                  <td valign="top">
                    <input type="button" class="portlet-form-button">
                      <xsl:attribute name="value">OK</xsl:attribute>
                      <xsl:attribute name="onClick">doRun("<xsl:value-of select="/filters/id" />", 'generatedContent?', '<xsl:value-of select="/filters/target"/>', document.getElementById('run_as_background_yes').checked);</xsl:attribute>
                      <xsl:attribute name="id">run2button<xsl:value-of select="/filters/id" /></xsl:attribute>
                    </input>
                  </td>
                  <td valign="top">
                    <input type="button" class="portlet-form-button"  id="cancelBtn" style="display:none">
                      <xsl:attribute name="value">Cancel</xsl:attribute>
                      <xsl:attribute name="onClick">closeMantleTab()</xsl:attribute>
                    </input>
                    <script type="text/javascript">
                          // show cancel when under mantle
                          if(window.parent.mantle_initialized == true){
                                document.getElementById("cancelBtn").style.display="inline";
                          }
                    </script>
                  </td>
                </tr>
              </table>
            </div> <!-- /run3div -->
            </td>
          </tr>       
       </xsl:when>
     </xsl:choose>
     <script type="text/javascript">
       var pentaho_formId = '<xsl:value-of select="/filters/id" />';
     </script>
  </xsl:template>

</xsl:stylesheet>
