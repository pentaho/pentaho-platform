<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xhtml="http://www.w3.org/2002/06/xhtml2"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    xmlns="http://org.pentaho"
    exclude-result-prefixes="xhtml xforms chiba xlink">



    <!-- ####################################################################################################### -->
    <!-- This stylesheet handles the XForms UI constructs [XForms 1.0, Chapter 9]'group', 'repeat' and           -->
    <!-- 'switch' and offers some standard interpretations for the appearance attribute.                         -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <!-- ############################################ PARAMS ################################################### -->
    <!-- ##### should be declared in html4.xsl ###### -->
    <!-- ############################################ VARIABLES ################################################ -->


    <!--  <xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes"
        doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/> -->

    <!-- ### transcodes the XHMTL namespaced elements to HTML ### -->
    <xsl:namespace-alias stylesheet-prefix="xhtml" result-prefix="#default"/>

    <xsl:preserve-space elements="*"/>

    <!-- ####################################################################################################### -->
    <!-- #################################### GROUPS ########################################################### -->
    <!-- ####################################################################################################### -->

    <!--
    processing of groups and repeats is handled with a computational pattern (as mentioned in Michael Kay's XSLT
    Programmers Reference) in this stylesheet, that means that when a group or repeat is found its children will
    be processed with for-each. this top-down approach seems to be more adequate for transforming XForms markup
    than to follow a rule-based pattern. Also note that whenever nodesets of XForms controls are processed the
    call template 'buildControl' is used to handle the control. In contrast to apply-templates a call-template
    preserves the position() of the control inside its parent nodeset and this can be valuable information for
    annotating controls with CSS classes that refer to their parent.
    -->
    <!-- ###################################### MINIMAL GROUP ################################################## -->
    <!-- handle 'minimal' group - this is the default for groups and only annotates CSS to labels + controls and
    outputs them in a kind of flow-layout -->
    <xsl:template match="xforms:group[@appearance='minimal']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found minimal group</xsl:message>
        </xsl:if>

        <xsl:variable name="group-css">
        <!--
            <xsl:call-template name="assembleClasses"/>
        -->
        </xsl:variable>
        <xsl:variable name="id" select="@id"/>

        <div class="{normalize-space(concat('minimal-group',' ',$group-css))}" id="{$id}">
            <xsl:for-each select="*">
                <xsl:choose>

                    <!-- **** handle group label ***** -->
                    <xsl:when test="self::xforms:label">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>handling group label ...</xsl:message>
                        </xsl:if>
                        <span id="{$id}-label" class="minimal-group-label">
                            <xsl:apply-templates select="."/>
                        </span>
                        <xsl:message>handled group label ...</xsl:message>
                    </xsl:when>

                    <!-- **** handle group alert ***** -->
                    <xsl:when test="self::xforms:alert">
                        <xsl:apply-templates select="xforms:alert"/>
                    </xsl:when>

                    <!-- **** handle sub group ***** -->
                    <xsl:when test="self::xforms:group">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>found group</xsl:message>
                        </xsl:if>
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle repeat ***** -->
                    <xsl:when test="self::xforms:repeat">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>found repeat</xsl:message>
                        </xsl:if>
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle switch ***** -->
                    <xsl:when test="self::xforms:switch">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>found switch</xsl:message>
                        </xsl:if>
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle chiba:data element ***** -->
                    <xsl:when test="self::chiba:data" xmlns:chiba="http://chiba.sourceforge.net/xforms">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>ignoring chiba data element</xsl:message>
                        </xsl:if>
                    </xsl:when>

                    <!-- **** handle trigger + submit ***** -->
                    <xsl:when test="self::xforms:trigger or self::xforms:submit">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>handling trigger:
                                <xsl:value-of select="xforms:label"/>
                            </xsl:message>
                        </xsl:if>
                        <xsl:variable name="css">
        <!--
            <xsl:call-template name="assembleClasses"/>
        -->
                        </xsl:variable>
                        <span class="{$css}" id="{@id}">
                            <xsl:call-template name="buildControl"/>
                        </span>
                    </xsl:when>

                    <!-- **** handle xforms control ***** -->
                    <xsl:when test="self::xforms:*">

                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>handling control label</xsl:message>
                            <xsl:message>
                                <xsl:value-of select="name()"/>-
                                <xsl:value-of select="xforms:label"/>
                            </xsl:message>
                        </xsl:if>

                        <xsl:variable name="css">
        <!--
            <xsl:call-template name="assembleClasses"/>
        -->
                        </xsl:variable>
                        <xsl:variable name="label-class">
                           <!--   <xsl:call-template name="labelClasses"/> -->
                        </xsl:variable>
                        <span id="{@id}" class="{$css}">
                            <span id="{@id}-label" class="{$label-class}">
                                <xsl:apply-templates select="xforms:label"/>
                            </span>
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling control</xsl:message>
                                <xsl:message>
                                    <xsl:value-of select="name()"/>
                                </xsl:message>
                            </xsl:if>
                            <xsl:call-template name="buildControl"/>
                        </span>
                    </xsl:when>

                    <!-- **** handle all other ***** -->
                    <xsl:otherwise>
                        <xsl:call-template name="handle-foreign-elements"/>
                    </xsl:otherwise>

                </xsl:choose>
            </xsl:for-each>
        </div>
    </xsl:template>


    <!-- ###################################### COMPACT GROUP ################################################## -->
    <xsl:template match="xforms:group[@appearance='compact']">
        <xsl:if test="$debug-enabled='yes'">
            <xsl:message>found compact group
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="control-count" select="count(./*/xforms:label)"/>
        <xsl:variable name="group-css">
            <!-- <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>
        <table class="{normalize-space(concat('compact-group',' ',$group-css))}" id="{$id}">
            <!-- ***** build caption with column labels ***** -->
            <tr>
                <td colspan="{$control-count}" id="{$id}-label" class="compact-group-label">
                    <xsl:apply-templates select="xforms:label"/>
                </td>
            </tr>
            <tr>
                <xsl:for-each select="./*/xforms:label">
                    <xsl:variable name="label-class">
                       <!--   <xsl:call-template name="labelClasses"/>  -->
                    </xsl:variable>
                    <td id="{../@id}-label" class="{$label-class}">
                        <xsl:apply-templates select="self::node()[not(name(..)='xforms:trigger' or name(..)='xforms:submit')]"/>
                    </td>
                </xsl:for-each>
            </tr>
            <tr>
                <xsl:for-each select="*">
                    <xsl:choose>

                        <!-- **** handle group label ***** -->
                        <xsl:when test="self::xforms:label">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>ignoring group label ...</xsl:message>
                            </xsl:if>
                        </xsl:when>

                        <!-- **** handle group alert ***** -->
                        <xsl:when test="self::xforms:alert">
                            <xsl:apply-templates select="xforms:alert"/>
                        </xsl:when>

                        <!-- **** handle sub group ***** -->
                        <xsl:when test="self::xforms:group">
                            <td colspan="{$control-count}">
                                <xsl:apply-templates select="."/>
                            </td>
                        </xsl:when>

                        <!-- **** handle repeat ***** -->
                        <xsl:when test="self::xforms:repeat">
                            <td colspan="{$control-count}">
                                <xsl:apply-templates select="."/>
                            </td>
                        </xsl:when>

                        <!-- **** handle switch ***** -->
                        <xsl:when test="self::xforms:switch">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>found switch</xsl:message>
                            </xsl:if>
                            <td colspan="{$control-count}">
                                <xsl:apply-templates select="."/>
                            </td>
                        </xsl:when>

                        <!-- **** handle trigger + submit ***** -->
                        <xsl:when test="self::xforms:trigger or self::xforms:submit">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling trigger:
                                    <xsl:value-of select="xforms:label"/>
                                </xsl:message>
                            </xsl:if>
                            <xsl:variable name="css">
                              <!--   <xsl:call-template name="assembleClasses"/> -->
                            </xsl:variable>
                            <td class="{$css}" id="{@id}">
                                <xsl:call-template name="buildControl"/>
                            </td>
                        </xsl:when>

                        <!-- **** handle xforms control ***** -->
                        <xsl:when test="self::xforms:*">
                            <xsl:variable name="css">
                             <!--     <xsl:call-template name="assembleClasses"/> -->
                            </xsl:variable>
                            <td id="{@id}" class="{$css}">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling control</xsl:message>
                                    <xsl:message>
                                        <xsl:value-of select="name()"/>
                                    </xsl:message>
                                </xsl:if>
                                <xsl:call-template name="buildControl"/>
                            </td>
                        </xsl:when>

                        <!-- **** handle chiba:data element ***** -->
                        <xsl:when test="self::chiba:data" xmlns:xforms="http://chiba.sourceforge.net/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>ignoring chiba data element</xsl:message>
                            </xsl:if>
                        </xsl:when>

                        <!-- **** handle all other ***** -->
                        <xsl:otherwise>
                            <xsl:call-template name="handle-foreign-elements"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </tr>
        </table>
    </xsl:template>

    <!-- ###################################### FULL GROUP ################################################## -->
    <!-- handle group with apprearance 'full' - will render controls in a two-column table with labels on
    the left side. -->
    <xsl:template match="xforms:group" name="full-group">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found full group (the default)
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="group-css">
            <!--  <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>
        <table class="{normalize-space(concat('full-group',' ',$group-css))}" id="{$id}" border="0">
            <!-- handling group children -->
            <xsl:for-each select="*">
                <xsl:message>*=
                    <xsl:value-of select="."/> ...
                </xsl:message>

                <xsl:choose>
 			
                    <!-- ***** build caption with column labels ***** -->
                    <xsl:when test="self::xforms:label">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>handling group label ...</xsl:message>
                        </xsl:if>
                        <tr>
                            <td colspan="2" id="{$id}-label" class="full-group-label">
                                <xsl:apply-templates select="."/>
                            </td>
                        </tr>
                        <xsl:message>handled group label ...</xsl:message>
                    </xsl:when>

                    <!-- **** handle group alert ***** -->
                    <xsl:when test="self::xforms:alert">
                        <xsl:apply-templates select="xforms:alert"/>
                    </xsl:when>

                    <!-- **** handle sub group ***** -->
                    <xsl:when test="self::xforms:group">
                        <tr>
                            <td colspan="2">
                                <xsl:apply-templates select="."/>
                            </td>
                        </tr>
                    </xsl:when>

                    <!-- **** handle repeat ***** -->
                    <xsl:when test="self::xforms:repeat">
                        <tr>
                            <td colspan="2">
                                <xsl:apply-templates select="."/>
                            </td>
                        </tr>
                    </xsl:when>

                    <!-- **** handle switch ***** -->
                    <xsl:when test="self::xforms:switch">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>found switch</xsl:message>
                        </xsl:if>
                        <tr>
                            <td colspan="2">
                                <xsl:apply-templates select="."/>
                            </td>
                        </tr>
                    </xsl:when>

                    <!-- **** handle trigger + submit ***** -->
                    <xsl:when test="self::xforms:trigger or self::xforms:submit">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>handling trigger:
                                <xsl:value-of select="xforms:label"/>
                            </xsl:message>
                        </xsl:if>
                        <tr>
                            <xsl:variable name="css">
                                <!--  <xsl:call-template name="assembleClasses"/> -->
                            </xsl:variable>
                            <td class="{$css}" id="{@id}" colspan="2">
                                <xsl:call-template name="buildControl"/>
                            </td>
                        </tr>
                    </xsl:when>

                    <!-- **** handle xforms control ***** -->
                    <xsl:when test="self::xforms:*">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>element ->
                                <xsl:value-of select="name(.)"/>
                            </xsl:message>
                        </xsl:if>

                        <tr>
                            <xsl:variable name="css">
                                <!--  <xsl:call-template name="assembleClasses"/> -->
                            </xsl:variable>
                            <xsl:variable name="label-class">
                               <!--   <xsl:call-template name="labelClasses"/> -->
                            </xsl:variable>
                            <td id="{@id}-label" class="{$label-class}">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling control label</xsl:message>
                                    <xsl:message>
                                        <xsl:value-of select="name()"/>-
                                        <xsl:value-of select="xforms:label"/>
                                    </xsl:message>
                                </xsl:if>
                                <xsl:apply-templates select="xforms:label"/>
                            </td>

                            <!--
                                                       <xsl:variable name="cssa">
                                                            <xsl:call-template name="assembleClasses"/>
                                                        </xsl:variable>
                            -->
                            <td id="{@id}" class="{$css}">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling control</xsl:message>
                                    <xsl:message>
                                        <xsl:value-of select="name()"/>
                                    </xsl:message>
                                </xsl:if>
                                <xsl:call-template name="buildControl"/>
                            </td>
                        </tr>
                    </xsl:when>

                    <!-- **** handle chiba:data element ***** -->
                    <xsl:when test="self::chiba:data" xmlns:xforms="http://chiba.sourceforge.net/xforms">
                        <xsl:if test="$debug-enabled='true'">
                            <xsl:message>ignoring chiba data element</xsl:message>
                        </xsl:if>
                    </xsl:when>

                    <!-- **** handle all other ***** -->
                    <xsl:otherwise>
                        <xsl:call-template name="handle-foreign-elements"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ###################################### MULTI-COLUMN GROUP ############################################## -->
    <!-- ### based upon compact apprearance the mulit-column group puts child groups into columns of a        ### -->
    <!-- ### table and allow groups to appear side by side.                                                   ### -->
    <!-- ### ATTENTION: ONLY GROUPS ARE PROCESSED BY THIS TEMPLATE - EVERYTHING ELSE IS IGNORED               ### -->
    <!-- ######################################################################################################## -->
    <xsl:template match="xforms:group[@appearance='multi-column']">
        <xsl:if test="$debug-enabled='yes'">
            <xsl:message>found multi-column group
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="group-count" select="count(./xforms:group)"/>
        <xsl:variable name="group-css">
           <!--   <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>
        <table class="{normalize-space(concat('multi-column-group',' ',$group-css))}" id="{$id}">
            <!-- ***** build caption with column labels ***** -->
            <tr>
                <td colspan="{$group-count}" width="100%" id="{$id}-label" class="multi-column-group-label">
                    <xsl:apply-templates select="xforms:label"/>
                </td>
            </tr>
            <tr>
                <xsl:for-each select="xforms:group">
                    <td>
                        <xsl:apply-templates select="."/>
                    </td>
                </xsl:for-each>
            </tr>
        </table>
    </xsl:template>



    <!-- ####################################### GROUP HELPER ################################################### -->
    <xsl:template name="handle-foreign-elements">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling element:
                <xsl:value-of select="name()"/>
            </xsl:message>
        </xsl:if>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ####################################### REPEAT ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- ### handle repeat with 'minimal' appearance ### -->
    <xsl:template match="xforms:repeat">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found minimal repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="group-css">
          <!--    <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>
        <table class="{concat('minimal-repeat',' ',$group-css)}" id="{@id}">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <!-- style attribute for safety in case CSS file is not there -->
                <tr class="repeat-prototype" onclick="setRepeatIndex('{@id}');" style="display:none;">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]">
                        <xsl:call-template name="processMinimalChilds"/>
                    </xsl:for-each>
                </tr>
            </xsl:if>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <!-- ***** loop repeat entries ***** -->
            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:if test="$debug-enabled='true'">
                    <xsl:message>found
                        <xsl:value-of select="name()"/>...
                    </xsl:message>
                    <xsl:message>found
                        <xsl:value-of select="xforms:label"/>...
                    </xsl:message>
                </xsl:if>

                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item repeat-index" onclick="setRepeatIndex('{$repeat-id}');">
                                    <xsl:call-template name="processMinimalChilds"/>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item repeat-index">
                                    <td class="minimal-repeat-selector">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                    </td>
                                    <xsl:call-template name="processMinimalChilds"/>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!--
                        <tr class="repeat-item repeat-index" onclick="setRepeatIndex('{$repeat-id}');">
                            <xsl:if test="not($scripted='true')">
                                <td class="minimal-repeat-selector">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                </td>
                            </xsl:if>
                            <xsl:for-each select="*">
                                <xsl:variable name="css">
                                    <xsl:call-template name="assembleClasses"/>
                                </xsl:variable>
                                <xsl:variable name="label-class">
                                    <xsl:call-template name="labelClasses"/>
                                </xsl:variable>
                                <td id="{@id}" class="{$css}">
                                    <span id="{@id}-label" class="{$label-class}">
                                        <xsl:apply-templates select="./xforms:label"/>
                                    </span>

                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                        -->
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item" onclick="setRepeatIndex('{$repeat-id}');">
                                    <xsl:call-template name="processMinimalChilds"/>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item">
                                    <td class="minimal-repeat-selector">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                    </td>
                                    <xsl:call-template name="processMinimalChilds"/>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!--
                                                <tr class="repeat-item" onclick="setRepeatIndex('{$repeat-id}');">
                                                    <xsl:if test="not($scripted='true')">
                                                        <td class="minimal-repeat-selector">
                                                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                                        </td>
                                                    </xsl:if>
                                                    <xsl:for-each select="*">
                                                        <xsl:variable name="css">
                                                            <xsl:call-template name="assembleClasses"/>
                                                        </xsl:variable>
                                                        <xsl:variable name="label-class">
                                                            <xsl:call-template name="labelClasses"/>
                                                        </xsl:variable>
                                                        <td id="{@id}" class="{$css}">
                                                            <span id="{@id}-label" class="{$label-class}">
                                                                <xsl:apply-templates select="./xforms:label"/>
                                                            </span>

                                                            <xsl:call-template name="buildControl"/>
                                                        </td>
                                                    </xsl:for-each>
                                                </tr>
                        -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ### used by minimal repeat ### -->
    <xsl:template name="processMinimalChilds">
        <xsl:for-each select="*">
            <xsl:variable name="css">
                <!--  <xsl:call-template name="assembleClasses"/> -->
            </xsl:variable>
            <xsl:variable name="label-class">
               <!--  <xsl:call-template name="labelClasses"/> -->
            </xsl:variable>
            <td id="{@id}" class="{$css}">
                <span id="{@id}-label" class="{$label-class}">
                    <xsl:apply-templates select="./xforms:label"/>
                </span>

                <xsl:call-template name="buildControl"/>
            </td>
        </xsl:for-each>
    </xsl:template>

    <!-- ### handle repeat with 'compact' appearance ### -->
    <xsl:template match="xforms:repeat[appearance='compact']" priority="1">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found compact repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="repeat" select="."/>
        <xsl:variable name="group-css">
            <!--  <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>
        <table id="{@id}" class="{concat('compact-repeat',' ',$group-css)}" border="0">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <!-- style attribute for safety in case CSS file is not there -->
                <tr class="repeat-prototype" onclick="setRepeatIndex('{@id}');" style="display:none;">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]">
                        <xsl:call-template name="processCompactChilds"/>
                    </xsl:for-each>
                </tr>
            </xsl:if>
            <tr class="compact-repeat-label">
                <xsl:if test="not($scripted='true')">
                    <!-- ***** build empty selector cell ***** -->
                    <td>&#160;</td>
                </xsl:if>
                <!-- ***** build header ***** -->
                <xsl:for-each select="xforms:group[1]/*/xforms:label">
                    <xsl:variable name="label-class">
                       <!--   <xsl:call-template name="labelClasses"/> -->
                    </xsl:variable>
                    <td id="{../@id}-label" class="{$label-class}">
                        <xsl:apply-templates select="self::node()[not(name(..)='xforms:trigger' or name(..)='xforms:submit')]"/>
                    </td>
                </xsl:for-each>
            </tr>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item repeat-index" onclick="setRepeatIndex('{$repeat/@id}');">
                                    <xsl:call-template name="processCompactChilds"/>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item repeat-index">
                                    <td class="selector-cell">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                    </td>
                                    <xsl:call-template name="processCompactChilds"/>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>

                        <!--
                                                <tr class="repeat-item repeat-index" onclick="setRepeatIndex('{$repeat/@id}');">
                                                    <xsl:if test="not($scripted='true')">
                                                        <td class="selector-cell">
                                                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                                        </td>
                                                    </xsl:if>
                                                    <xsl:for-each select="*">
                                                        <xsl:variable name="css">
                                                            <xsl:call-template name="assembleClasses"/>
                                                        </xsl:variable>
                                                        <td id="{@id}" class="{$css}">
                                                            <xsl:call-template name="buildControl"/>
                                                        </td>
                                                    </xsl:for-each>
                                                </tr>
                        -->
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item" onclick="setRepeatIndex('{$repeat/@id}');">
                                    <xsl:call-template name="processCompactChilds"/>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item">
                                    <td class="selector-cell">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                    </td>
                                    <xsl:call-template name="processCompactChilds"/>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!--
                                                <tr class="repeat-item" onclick="setRepeatIndex('{$repeat/@id}');">
                                                    <xsl:if test="not($scripted='true')">
                                                        <td class="selector-cell">
                                                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                                        </td>
                                                    </xsl:if>
                                                    <xsl:for-each select="*">
                                                        <xsl:variable name="css">
                                                            <xsl:call-template name="assembleClasses"/>
                                                        </xsl:variable>
                                                        <td id="{@id}" class="{$css}">
                                                            <xsl:call-template name="buildControl"/>
                                                        </td>
                                                    </xsl:for-each>
                                                </tr>
                        -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ### used by compact repeat ### -->
    <xsl:template name="processCompactChilds">
        <xsl:for-each select="*">
            <xsl:variable name="css">
                <!--  <xsl:call-template name="assembleClasses"/> -->
            </xsl:variable>
            <td id="{@id}" class="{$css}">
                <xsl:call-template name="buildControl"/>
            </td>
        </xsl:for-each>
    </xsl:template>

    <!-- ### handle repeat with 'full' appearance ### -->
    <xsl:template match="xforms:repeat[appearance='full']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found full repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="repeat" select="."/>
        <xsl:variable name="group-css">
         <!--     <xsl:call-template name="assembleClasses"/> -->
        </xsl:variable>

        <table class="{normalize-space(concat('full-repeat',' ',$group-css))}" id="{@id}">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <!-- style attribute for safety in case CSS file is not there -->
                <tr class="repeat-prototype" onclick="setRepeatIndex('{@id}');" style="display:none;">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]">
                        <td>
                            <xsl:call-template name="full-group"/>
                        </td>
                    </xsl:for-each>
                </tr>
            </xsl:if>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <!-- ***** loop repeat entries ***** -->
            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item repeat-index" onclick="setRepeatIndex('{$repeat-id}');">
                                    <td>
                                        <xsl:call-template name="full-group"/>
                                    </td>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item repeat-index">
                                    <td class="selector-cell">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                    </td>
                                    <td>
                                        <xsl:call-template name="full-group"/>
                                    </td>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <tr class="repeat-item" onclick="setRepeatIndex('{$repeat-id}');">
                                    <td>
                                        <xsl:call-template name="full-group"/>
                                    </td>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr class="repeat-item">
                                    <td class="selector-cell">
                                        <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                    </td>
                                    <td>
                                        <xsl:call-template name="full-group"/>
                                    </td>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ### handle repeats attribute on foreign elements ### -->
    <xsl:template match="*[repeat-bind]|*[repeat-nodeset]">
        <xsl:apply-templates/>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ####################################### SWITCH ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- ### handle xforms:switch ### -->
    <xsl:template match="xforms:switch">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling switch</xsl:message>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ### handle selected xforms:case ### -->
    <xsl:template match="xforms:case[selected='true']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling selected case</xsl:message>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ### skip unselected xforms:case ### -->
    <xsl:template match="xforms:case">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling unselected case</xsl:message>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
