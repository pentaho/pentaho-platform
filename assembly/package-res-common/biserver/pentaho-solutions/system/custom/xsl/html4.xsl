<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xhtml="http://www.w3.org/2002/06/xhtml2"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xf="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:pho="http://www.w3.org/1999/homl"
    xmlns:loc="org.pentaho.platform.util.messages.LocaleHelper"
    xmlns="http://org.pentaho"
    exclude-result-prefixes="xhtml xforms xlink pho xf loc">
 
    <xsl:include href="system/custom/xsl/ui.xsl"/>
    <xsl:include href="system/custom/xsl/html-form-controls.xsl"/>

    <!-- ############################################ PARAMS ################################################### -->

    <xsl:param name="action-url" select="''"/> 
    <xsl:param name="form-id" select="'pentaho-form'"/>
    <xsl:param name="form-method" select="'GET'"/>
		<xsl:param name="form-enctype" select="application/x-www-form-urlencoded"/>
		<xsl:param name="output-encoding" select="UTF-8"/>

    <xsl:param name="debug-enabled" select="'false'"/>

    <!-- ### specifies the parameter prefix for repeat selectors ### -->
    <xsl:param name="selector-prefix" select="''"/>

    <!-- ### contains the full user-agent string as received from the servlet ### -->
    <xsl:param name="user-agent" select="'default'"/>

    <!-- ### this parameter is used when the Adapter wants to specify the CSS to use ### -->
    <xsl:param name="css-file" select="''"/>


    <xsl:param name="scripted" select="'false'"/>

    <!-- ############################################ VARIABLES ################################################ -->

    <!-- ### checks, whether this form uses uploads. Used to set form enctype attribute ### -->
    <xsl:variable name="uses-upload" select="boolean(//*/xforms:upload)"/>

    <!-- ### the CSS stylesheet to use ### -->
    <xsl:variable name="default-css" select="'styles/styles.css'"/>
    <xsl:variable name="mozilla-css" select="'styles/mozilla-xforms.css'"/>
    <xsl:variable name="ie-css" select="'styles/ie-xforms.css'"/>

		<!--  =============================================================== -->
		<!--  JIRA case PLATFORM-115: since this is an HTML snippet, do not 
																	include the <XML ver...> declaration
																	as the first line in the document;      -->

    
    <xsl:output method="html" encoding="UTF-8" omit-xml-declaration="yes" />
		<!-- <xsl:output method="xml" encoding="{$output-encoding}" omit-xml-declaration="yes" /> -->

    <!--  =============================================================== -->
		
    <!--  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" -->

    <!-- ### transcodes the XHMTL namespaced elements to HTML ### -->
		<xsl:namespace-alias stylesheet-prefix="xhtml" result-prefix="#default"/> 

    <xsl:preserve-space elements="*"/>
    <xsl:strip-space elements="xforms:action"/>

    <!-- ####################################################################################################### -->
    <!-- ##################################### TEMPLATES ####################################################### -->
    <!-- ####################################################################################################### -->

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$debug-enabled='true'">
                <xsl:message>********** INFO: XSLT MESSAGES ARE ENABLED</xsl:message>
                <xsl:message>RECEIVED PARAMETERS...</xsl:message>
                <xsl:message>css-file=
                    <xsl:value-of select="$css-file"/>
                </xsl:message>
                <xsl:message>data-prefix=
                    <xsl:value-of select="$data-prefix"/>
                </xsl:message>
                <xsl:message>trigger-prefix=
                    <xsl:value-of select="$trigger-prefix"/>
                </xsl:message>
                <xsl:message>user-agent=
                    <xsl:value-of select="$user-agent"/>
                </xsl:message>
                <xsl:message>scripting=
                    <xsl:value-of select="$scripted"/>
                </xsl:message>
            </xsl:when>
            <!--  xsl:otherwise>
                <xsl:message>********** INFO: XSLT MESSAGES ARE DISABLED</xsl:message>
            </xsl:otherwise -->
        </xsl:choose>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- copy unmatched mixed markup, comments, whitespace, and text -->
    <!-- ### copy elements from the xhtml2 namespace to html (without any namespace) by re-creating the     ### -->
    <!-- ### elements. Other Elements are just copied.                                                      ### -->
    
    <xsl:template match="*|@*|text()">
        <xsl:choose>
            <xsl:when test="namespace-uri(.)='http://www.w3.org/2002/06/xhtml2'">
                <xsl:element name="{local-name(.)}">
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:element>
            </xsl:when>

		<!--  =============================================================== -->

		<!--  JIRA case PLATFORM-115: shallow copy the pho tags and all children
																	to eliminate namespaces from source 
																	document;                               -->

            <xsl:when test="namespace-uri(.)='http://www.w3.org/1999/homl'">
                <xsl:element name="{local-name(.)}">
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:element>
            </xsl:when>
		<!--  =============================================================== -->
            
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
		<!--  =============================================================== -->
		<!--  JIRA case PLATFORM-115: exclude pentaho tags from xfrom results -->
    
    <xsl:template match="pho:*">
	    <xsl:apply-templates/>
    </xsl:template>
		<!--  =============================================================== -->

    <xsl:template match="xhtml:html | html">
                        <xsl:element name="html">
	      		  <xsl:copy-of select="@*"/>
                            <xsl:apply-templates/>
                        </xsl:element>
    </xsl:template>

    <xsl:template match="xhtml:head | head">

                        <xsl:element name="head">
	      		  <xsl:copy-of select="@*"/>

<xsl:for-each select="meta">
                        <xsl:element name="meta">
	      		  <xsl:copy-of select="@*"/>
                        </xsl:element>
</xsl:for-each>

                            <xsl:apply-templates/>
                        </xsl:element>

    </xsl:template>
    
		<!--  =============================================================== -->

		<!--  JIRA case PLATFORM-115: shallow copy the link tags to eliminate 
																	namespaces from source document;
																	not sure why link tag inherits namespaces 
																	from pho: tags, while other	tags do not -->
    
    <xsl:template match="xhtml:link | link">
			<xsl:element name="{local-name(.)}">
        <xsl:apply-templates select="*|@*|text()"/>
      </xsl:element>
		</xsl:template>
		<!--  =============================================================== -->

    <xsl:template match="form">
                        <xsl:element name="form">
                            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
	      		  <xsl:copy-of select="@*"/>
										        <xsl:for-each select="//xforms:model/xforms:instance/data/*">
										        	<xsl:if test="not(//@ref = local-name())">
											            <xsl:call-template name="hidden"/>
										           </xsl:if>
										        </xsl:for-each>
                            <xsl:apply-templates/>
                        </xsl:element>
		</xsl:template>


	<xsl:template name="doForm">
		<xsl:param name="form" select="'bogus'"/>

		    <!-- 
            <xsl:message>start</xsl:message>
            <xsl:message><xsl:value-of select="count($form)"/></xsl:message>
            <xsl:message><xsl:value-of select="$form"/></xsl:message>
            <xsl:message>end</xsl:message>
 			-->
 			
			<body>
			  <xsl:attribute name="dir"><xsl:value-of select="loc:getTextDirection()"/></xsl:attribute>
      		  <xsl:copy-of select="@*"/>

	<xsl:choose>
		<xsl:when test="count($form)>0">
            <!--xsl:message>have form</xsl:message-->
            <xsl:apply-templates/>
		</xsl:when>
		<xsl:otherwise>
                        <xsl:element name="form">
                            <xsl:attribute name="name">
                                <xsl:value-of select="$form-id"/>
                            </xsl:attribute>
                            <xsl:attribute name="action">
                                <xsl:value-of select="//xforms:submission/@action"/>
                            </xsl:attribute>
                            <xsl:attribute name="method"><xsl:value-of select="$form-method"/></xsl:attribute>
                            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
                            <xsl:if test="$uses-upload">
                                <xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="$scripted='true'">
                                <xsl:attribute name="onsubmit">javascript:submit();</xsl:attribute>
                            </xsl:if>


														<!--  any elements in the xforms model that is not bound to a control 
																	will be converted to a hidden data field -->
										        <xsl:for-each select="//xforms:model/xforms:instance/data/*">
										        	<xsl:if test="not(//@ref = local-name())">
											            <xsl:call-template name="hidden"/>
										           </xsl:if>
										        </xsl:for-each>
                            
                            <xsl:apply-templates/>
                        </xsl:element>
		</xsl:otherwise>
	</xsl:choose>
			</body>
	</xsl:template>

    <xsl:template match="xhtml:body | body" >

	<xsl:call-template name="doForm">
		<xsl:with-param name="form" select=".//form"/>
	</xsl:call-template>

    </xsl:template>

    <xsl:template match="xhtml:span">
        <span>
            <xsl:copy-of select="@xhtml:class"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>


    <!-- ### skip model section ### -->
    <xsl:template match="xforms:model"/>

    <!-- ######################################################################################################## -->
    <!-- #####################################  CONTROLS ######################################################## -->
    <!-- ######################################################################################################## -->

    <!-- ### handle xforms:input ### -->
    <xsl:template match="xforms:input">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:output ### -->
    <xsl:template match="xforms:output">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:range ### -->
    <xsl:template match="xforms:range">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:secret ### -->
    <xsl:template match="xforms:secret">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select ### -->
    <xsl:template match="xforms:select">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select1 ### -->
    <xsl:template match="xforms:select1">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:submit ### -->
    <xsl:template match="xforms:submit">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:trigger ### -->
    <xsl:template match="xforms:trigger">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:textarea ### -->
    <xsl:template match="xforms:textarea">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:upload ### -->
    <xsl:template match="xforms:upload">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle label ### -->
    <xsl:template match="xforms:label">
        <xsl:variable name="group-id" select="ancestor::xforms:group[1]/@id"/>
        <xsl:variable name="img" select="@xforms:src"/>

        <xsl:choose>
            <xsl:when test="name(..)='xforms:item'">
                <span id="{@id}" class="label">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <!-- suppress trigger labels - they are handle by the control itself -->
            <xsl:when test="parent::xforms:trigger" xmlns:xforms="http://www.w3.org/2002/xforms">
            </xsl:when>
            <!-- if there's an output child -->
            <xsl:when test="self::xforms:output" xmlns:xforms="http://www.w3.org/2002/xforms">
                <xsl:apply-templates select="xforms:output"/>
            </xsl:when>
            <!-- if there's a src attribute pointing to some image file the image is linked in -->
            <xsl:when test="boolean($img) and ( contains($img,'.gif') or contains($img,'.jpg') or contains($img,'.png') )">
                <img src="{$img}" id="{@id}-label"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ### handle hint ### -->
    <xsl:template match="xforms:hint">
        <!--  already handled by individual controls in html-form-controls.xsl -->
    </xsl:template>

    <!-- ### handle help ### -->
    <!-- ### only reacts on help elements with a 'src' attribute and interprets it as html href ### -->
    <xsl:template match="xforms:help">
        ?
        <!--
                <span style="font-color:blue;vertical-align:top;padding-left:2px;font-weight:bold">
                    <xsl:if test="@xforms:src">
                        <a href="{@xforms:src}">?</a>
                    </xsl:if>
                </span>
        -->
        <!--        <img src="images/kasten_blau.gif"/>-->
        <!-- this implementation renders a button to display a javascript message -->
        <!--        <img src="images/help.gif" onClick="javascript:xf_help('{normalize-space(.)}');return true;"/>-->
    </xsl:template>

    <!-- ### handle explicitely enabled alert ### -->
    <!--    <xsl:template match="xforms:alert[../chiba:data/@chiba:valid='false']">-->
    <xsl:template match="xforms:alert">
        <span id="{../@id}-alert" class="alert">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <!-- ### handle extensions ### -->
    <xsl:template match="xforms:extension">
        <xsl:apply-templates/>
    </xsl:template>


    <!-- ########################## ACTIONS ####################################################### -->
    <!-- these templates serve no real purpose here but are shown for reference what may be over-   -->
    <!-- written by customized stylesheets importing this one. -->
    <!-- ########################## ACTIONS ####################################################### -->

    <!-- action nodes are simply copied to output without any modification -->
    <xsl:template match="xforms:action">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="xforms:dispatch"/>
    <xsl:template match="xforms:rebuild"/>
    <xsl:template match="xforms:recalculate"/>
    <xsl:template match="xforms:revalidate"/>
    <xsl:template match="xforms:refresh"/>
    <xsl:template match="xforms:setfocus"/>
    <xsl:template match="xforms:load"/>
    <xsl:template match="xforms:setvalue"/>
    <xsl:template match="xforms:send"/>
    <xsl:template match="xforms:reset"/>
    <xsl:template match="xforms:message"/>
    <xsl:template match="xforms:toggle"/>
    <xsl:template match="xforms:insert"/>
    <xsl:template match="xforms:delete"/>
    <xsl:template match="xforms:setindex"/>


    <!-- ####################################################################################################### -->
    <!-- #####################################  HELPER TEMPLATES '############################################## -->
    <!-- ####################################################################################################### -->

    <xsl:template name="buildControl">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>buildControl:
                <xsl:value-of select="name(.)"/>
            </xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="local-name()='input'">
                <xsl:call-template name="input"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='output'">
                <xsl:call-template name="output"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='range'">
                <xsl:call-template name="range"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='secret'">
                <xsl:call-template name="secret"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select'">
                <xsl:call-template name="select"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select1'">
                <xsl:call-template name="select1"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='submit'">
                <xsl:call-template name="submit"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='trigger'">
                <xsl:call-template name="trigger"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='textarea'">
                <xsl:call-template name="textarea"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='upload'">
                <xsl:call-template name="upload"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='repeat'">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:when test="local-name()='group'">
                <xsl:apply-templates select="."/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='switch'">
                <xsl:apply-templates select="."/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- chooses the CSS stylesheet to use -->
    <xsl:template name="getCSS">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>user agent:
                <xsl:value-of select="$user-agent"/>
            </xsl:message>
        </xsl:if>

        <xsl:choose>
            <!-- if the 'css-file' parameter has been set this takes precedence -->
            <xsl:when test="string-length($css-file) > 0">
                <link rel="stylesheet" type="text/css" href="{$css-file}"/>
            </xsl:when>
            <!-- if there's a stylesheet linked plainly, then take this stylesheet. -->
            <xsl:when test="xhtml:link">
                <link rel="stylesheet" type="text/css" href="{xhtml:link/@href}"/>
            </xsl:when>
            <!--  if nothings present standard stylesheets for Mozilla and IE are choosen. -->
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="contains($user-agent,'IE')">
                        <link rel="stylesheet" type="text/css" href="{$ie-css}"/>
                    </xsl:when>
                    <xsl:when test="contains($user-agent,'Mozilla')">
                        <link rel="stylesheet" type="text/css" href="{$mozilla-css}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <link rel="stylesheet" type="text/css" href="{$default-css}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="xhtml:style">
            <style type="text/css">
                <xsl:value-of select="xhtml:style"/>
            </style>
        </xsl:if>
    </xsl:template>

    <!-- ***** builds a string containing the correct css-classes reflecting UI-states like
    readonly/readwrite, enabled/disabled, valid/invalid ***** -->
<!--
    <xsl:template name="assembleClasses">

        <xsl:variable name="authorClasses">
            <xsl:call-template name="collectExistingClasses"/>
        </xsl:variable>


 
        only execute if there's a data element which is e.g. not the case for unbound groups 
        <xsl:variable name="pseudoClasses">
            <xsl:if test="chiba:data">
                <xsl:variable name="valid">
                    <xsl:choose>
                        <xsl:when test="string-length(chiba:data) = 0 and chiba:data/@chiba:visited='false'">valid</xsl:when>
                        <xsl:when test="chiba:data/@chiba:valid='true'">valid</xsl:when>
                        <xsl:otherwise>invalid</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="readonly">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:readonly='true'">readonly</xsl:when>
                        <xsl:otherwise>readwrite</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="required">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:required='true'">required</xsl:when>
                        <xsl:otherwise>optional</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="enabled">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:enabled='true'">enabled</xsl:when>
                        <xsl:otherwise>disabled</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:value-of select="concat(' ',$valid,' ',$readonly,' ',$required, ' ', $enabled)"/>
            </xsl:if>
        </xsl:variable>

        <xsl:value-of select="normalize-space(concat(local-name(),' ',$authorClasses,$pseudoClasses))"/>

    </xsl:template>
-->
    <xsl:template name="collectExistingClasses">
        <xsl:variable name="classes">
            <xsl:choose>
                <xsl:when test="@class">
                    <xsl:value-of select="@class"/>
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="$classes"/>
    </xsl:template>
    
    <!--

    <xsl:template name="labelClasses">

         only execute if there's a data element which is e.g. not the case for unbound groups 
        <xsl:choose>
            <xsl:when test="self::chiba:data" xmlns:chiba="http://chiba.sourceforge.net/xforms">
                <xsl:variable name="enabled">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@zqq:enabled='true'">enabled</xsl:when>
                        <xsl:otherwise>disabled</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="concat('label ',$enabled)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'label'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    -->
    
    
    <xsl:template name="selectorName">
        <xsl:variable name="repeat-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
        <xsl:value-of select="concat($selector-prefix, $repeat-id)"/>
    </xsl:template>

</xsl:stylesheet>
