<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xforms="http://www.w3.org/2002/xforms"
	xmlns:chiba="http://chiba.sourceforge.net/xforms"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:msg="org.pentaho.platform.web.xsl.messages.Messages"
	exclude-result-prefixes="chiba xforms xlink msg">
	<!-- Copyright 2005 Chibacon -->

	<xsl:variable name="data-prefix" select="''" />
	<xsl:variable name="trigger-prefix" select="'t_'" />
	<xsl:variable name="remove-upload-prefix" select="'ru_'" />
	<!--  <xsl:param name="scripted" select="'false'"/> -->

	<!-- change this to your ShowAttachmentServlet -->
	<xsl:variable name="show-attachment-action"
		select="'./ShowAttachmentServlet'" />

	<!-- This stylesheet contains a collection of templates which map XForms controls to HTML controls. -->
	<xsl:output method="html" indent="yes" omit-xml-declaration="yes" />


	<!-- retrieves the default value to pre-populate an xforms control -->
	<xsl:template name="defaultValue">
		<xsl:param name="id_param" />
		<xsl:for-each select="//data/*">
			<xsl:if test="local-name()=$id_param">
				<xsl:value-of select="." />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- determines whether an item in alist of any sort is selected -->
	<xsl:template name="isSelected">
		<xsl:param name="value_param" />
		<xsl:param name="id_param" select="''"/>

		<xsl:for-each select="//data/*">

			<xsl:choose>
				<xsl:when test="$id_param=''">
                                        <!-- Preserve old behavior if ID is not there -->
			<xsl:variable name="value">
				<xsl:value-of select="." />
			</xsl:variable>
			<xsl:if test="contains($value, $value_param)">true</xsl:if>
				</xsl:when>
				<xsl:otherwise>
                                        <!-- Corrected behavior - only select the value for the control -->
					<xsl:if test="local-name()=$id_param">
						<xsl:variable name="value">
							<xsl:value-of select="." />
						</xsl:variable>
						<xsl:if test="contains($value, $value_param)">true</xsl:if>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<!-- ######################################################################################################## -->
	<!-- This stylesheet serves as a 'library' for HTML form controls. It contains only named templates and may   -->
	<!-- be re-used in different layout-stylesheets to create the naked controls.                                 -->
	<!-- ######################################################################################################## -->

	<!-- build input control -->
	<xsl:template name="input">

		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="value">
			<xsl:call-template name="defaultValue">
				<xsl:with-param name="id_param" select="$id" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="$debug-enabled='true'">
			<xsl:message>
				###repeat-id:
				<xsl:value-of select="$repeat-id" />
			</xsl:message>
			<xsl:message>
				###has repeat-id:
				<xsl:value-of
					select="boolean(string-length($repeat-id) > 0)" />
			</xsl:message>
			<xsl:message>
				###position:
				<xsl:value-of select="position()" />
			</xsl:message>
		</xsl:if>

		<xsl:if test="not(ancestor::xforms:group)" xmlns:xforms="http://www.w3.org/2002/xforms">
			<xsl:apply-templates select="xforms:label" />
    </xsl:if>

	<!--  	<xsl:value-of select="child::xforms:label" /> -->
		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="type">text</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="$value" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(xforms:hint)" />
			</xsl:attribute>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-input-field'" />
			</xsl:call-template>
			<xsl:if test="$scripted='true'">
				<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
			</xsl:if>
		</xsl:element>

		<xsl:call-template name="handleRequired" />
	</xsl:template>


	<!-- build hidden control -->
	<xsl:template name="hidden">
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id" select="@id" />
		<xsl:variable name="nm" select="name()" />

		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$nm" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$nm" />
			</xsl:attribute>
			<xsl:attribute name="type">hidden</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="." />
			</xsl:attribute>
		</xsl:element>
	</xsl:template>



	<!-- build image trigger / submit -->
	<xsl:template name="image-trigger">
		<xsl:element name="input">
			<xsl:variable name="id">
				<xsl:choose>
					<xsl:when test="boolean(string-length(@id) > 0)">
						<xsl:value-of select="@id" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@ref" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="repeat-id"
				select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
			<xsl:attribute name="id">
				<xsl:value-of select="concat($id,'-value')" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="concat($trigger-prefix,$id)" />
			</xsl:attribute>
			<xsl:attribute name="type">image</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="xforms:label" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(xforms:hint)" />
			</xsl:attribute>
			<xsl:attribute name="src">
				<xsl:value-of select="xforms:label/@xlink:href" />
			</xsl:attribute>
			<xsl:attribute name="class">
				portlet-form-field
			</xsl:attribute>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<xsl:if test="$scripted='true'">
				<xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
			</xsl:if>
		</xsl:element>

	</xsl:template>

	<!-- build output -->
	<xsl:template name="output">

		<xsl:variable name="css" select="@class" />
		<xsl:choose>

			<xsl:when test="@appearance='minimal'">
				<xsl:variable name="value">
					<xsl:call-template name="defaultValue">
						<xsl:with-param name="id_param" select="@ref" />
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$value" />
			</xsl:when>

			<xsl:when test="@appearance='image'">
				<xsl:element name="img">
					<xsl:attribute name="id">
						<xsl:value-of select="@id" />
					</xsl:attribute>
					<xsl:if test="$css">
						<xsl:attribute name="class">
							<xsl:value-of select="$css" />
						</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="src">
						<xsl:value-of select="@src" />
					</xsl:attribute>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@appearance='anchor'">
				<xsl:element name="a">
					<xsl:attribute name="id">
						<xsl:value-of select="@id" />
					</xsl:attribute>
					<xsl:if test="$css">
						<xsl:attribute name="class">
							<xsl:value-of select="$css" />
						</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="href">
						<xsl:value-of select="@href" />
					</xsl:attribute>
					<xsl:value-of select="." />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="span">
					<xsl:attribute name="id">
						<xsl:value-of select="@id" />
					</xsl:attribute>
					<xsl:if test="$css">
						<xsl:attribute name="class">
							<xsl:value-of select="$css" />
						</xsl:attribute>
					</xsl:if>
						<xsl:variable name="value">
							<xsl:call-template name="defaultValue">
								<xsl:with-param name="id_param" select="@ref" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of select="$value" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- build range -->
	<xsl:template name="range">
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id" select="@id" />
		<xsl:variable name="start" select="@xforms:start" />
		<xsl:variable name="end" select="@xforms:end" />
		<xsl:variable name="step" select="@xforms:step" />
		<xsl:variable name="showInput">
			<xsl:choose>
				<xsl:when test="@xforms:appearance='full'">
					true
				</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>


		<xsl:if test="$debug-enabled='true'">
			<xsl:message>WARN: range not supported yet</xsl:message>
		</xsl:if>


		<xsl:element name="script">
			<xsl:attribute name="language">JavaScript</xsl:attribute>
			createSlider2('
			<xsl:value-of select="$form-id" />
			', '
			<xsl:value-of select="concat($id,'-value')" />
			', '
			<xsl:value-of select="concat($data-prefix,$id)" />
			', '
			<xsl:value-of select="$start" />
			', '
			<xsl:value-of select="$end" />
			', '
			<xsl:value-of select="$step" />
			',
			<xsl:value-of select="$showInput" />
			, "", ""); setSlider('
			<xsl:value-of select="concat($data-prefix,$id)" />
			', '
			<xsl:value-of select="chiba:data/text()" />
			');
		</xsl:element>

		<xsl:call-template name="handleRequired" />
	</xsl:template>

	<!-- build secret control -->
	<xsl:template name="secret">
		<xsl:param name="maxlength" />

		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="value">
			<xsl:call-template name="defaultValue">
				<xsl:with-param name="id_param" select="$id" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:value-of select="child::xforms:label" />

		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="type">password</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="$value" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(./xforms:hint)" />
			</xsl:attribute>
			<xsl:if test="$maxlength">
				<xsl:attribute name="maxlength">
					<xsl:value-of select="$maxlength" />
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-input-field'" />
			</xsl:call-template>
			<xsl:if test="$scripted='true'">
				<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
			</xsl:if>
		</xsl:element>

		<xsl:call-template name="handleRequired" />
	</xsl:template>


	<xsl:template name="select1">

		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="parent" select="." />
		<xsl:value-of select="child::xforms:label" />
		<xsl:choose>
			<xsl:when test="@appearance='compact'">
				<xsl:element name="select">
					<xsl:attribute name="id">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="name">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="size">5</xsl:attribute>
					<xsl:attribute name="title">
						<xsl:value-of
							select="normalize-space(./xforms:hint)" />
					</xsl:attribute>

					<xsl:call-template name="assembleRepeatClasses">
						<xsl:with-param name="repeat-id"
							select="$repeat-id" />
						<xsl:with-param name="pos" select="$pos" />
						<xsl:with-param name="classes"
							select="'portlet-form-field'" />
					</xsl:call-template>
					<xsl:if test="chiba:data/@chiba:readonly='true'">
						<xsl:attribute name="disabled">
							disabled
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="$scripted='true'">
						<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id"/>');</xsl:attribute>
					</xsl:if>
					<xsl:call-template name="build-items">
						<xsl:with-param name="parent" select="$parent" />
					</xsl:call-template>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@appearance='full'">
				<xsl:call-template name="build-radiobuttons">
					<xsl:with-param name="id" select="$id" />
					<xsl:with-param name="name"
						select="concat($data-prefix,$id)" />
					<xsl:with-param name="parent" select="$parent" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="select">
					<xsl:attribute name="id">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="name">
						<xsl:value-of select="concat($data-prefix,$id)" />
					</xsl:attribute>
					<xsl:attribute name="size">1</xsl:attribute>
					<xsl:attribute name="title">
						<xsl:value-of
							select="normalize-space(./xforms:hint)" />
					</xsl:attribute>
					<xsl:call-template name="assembleRepeatClasses">
						<xsl:with-param name="repeat-id"
							select="$repeat-id" />
						<xsl:with-param name="pos" select="$pos" />
						<xsl:with-param name="classes"
							select="'portlet-form-field'" />
					</xsl:call-template>
					<xsl:if test="chiba:data/@chiba:readonly='true'">
						<xsl:attribute name="disabled">
							disabled
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="$scripted='true'">
						<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
					</xsl:if>
					<xsl:call-template name="build-items">
						<xsl:with-param name="parent" select="$parent" />
					</xsl:call-template>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:call-template name="handleRequired" />
	</xsl:template>


	<xsl:template name="select">

		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="parent" select="." />
		<xsl:choose>
			<xsl:when test="@appearance='compact'">
				<xsl:element name="select">
					<xsl:attribute name="id">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="name">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="title">
						<xsl:value-of
							select="normalize-space(./xforms:hint)" />
					</xsl:attribute>
					<xsl:attribute name="multiple">true</xsl:attribute>
					<xsl:attribute name="size">5</xsl:attribute>
					<xsl:if test="chiba:data/@chiba:readonly='true'">
						<xsl:attribute name="disabled">
							disabled
						</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="class">
						portlet-form-field
					</xsl:attribute>
					<xsl:call-template name="assembleRepeatClasses">
						<xsl:with-param name="repeat-id"
							select="$repeat-id" />
						<xsl:with-param name="pos" select="$pos" />
						<xsl:with-param name="classes"
							select="'portlet-form-field'" />
					</xsl:call-template>
					<xsl:if test="$scripted='true'">
						<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
					</xsl:if>
					<xsl:call-template name="build-items">
						<!--  xsl:with-param name="value"
							select="chiba:data/text()" / -->
						<xsl:with-param name="parent" select="$parent" />
					</xsl:call-template>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@appearance='full'">
				<xsl:call-template name="build-checkboxes">
					<xsl:with-param name="id" select="$id" />
					<xsl:with-param name="name" select="$id" />
					<xsl:with-param name="parent" select="$parent" />
					<xsl:with-param name="type" select="@appearance" />
					<xsl:with-param name="columns" select="1" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="@appearance='full-scroll'">
				<xsl:call-template name="build-checkboxes">
					<xsl:with-param name="id" select="$id" />
					<xsl:with-param name="name" select="$id" />
					<xsl:with-param name="parent" select="$parent" />
					<xsl:with-param name="type" select="@appearance" />
					<xsl:with-param name="columns" select="@columns" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="select">
					<xsl:attribute name="id">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="name">
						<xsl:value-of select="$id" />
					</xsl:attribute>
					<xsl:attribute name="title">
						<xsl:value-of
							select="normalize-space(./xforms:hint)" />
					</xsl:attribute>
					<xsl:attribute name="multiple">true</xsl:attribute>
					<xsl:attribute name="size">3</xsl:attribute>
					<xsl:if test="chiba:data/@chiba:readonly='true'">
						<xsl:attribute name="disabled">
							disabled
						</xsl:attribute>
					</xsl:if>

					<xsl:call-template name="assembleRepeatClasses">
						<xsl:with-param name="repeat-id"
							select="$repeat-id" />
						<xsl:with-param name="pos" select="$pos" />
						<xsl:with-param name="classes"
							select="'portlet-form-field'" />
					</xsl:call-template>
					<xsl:if test="$scripted='true'">
						<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
					</xsl:if>
					<xsl:call-template name="build-items">
						<!--  xsl:with-param name="value"
							select="chiba:data/text()" / -->
						<xsl:with-param name="parent" select="$parent" />
					</xsl:call-template>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:call-template name="handleRequired" />
	</xsl:template>

	<!-- build textarea control -->
	<xsl:template name="textarea">
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="value">
			<xsl:call-template name="defaultValue">
				<xsl:with-param name="id_param" select="$id" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:element name="textarea">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(./xforms:hint)" />
			</xsl:attribute>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-input-field'" />
			</xsl:call-template>
			<xsl:if test="$scripted='true'">
				<xsl:attribute name="onchange">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="$value" />
		</xsl:element>

		<xsl:call-template name="handleRequired" />
	</xsl:template>

	<!-- build submit -->
	<xsl:template name="submit">
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="$scripted='true'">
					<xsl:attribute name="type">button</xsl:attribute>
					<xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="type">submit</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="xforms:label" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(xforms:hint)" />
			</xsl:attribute>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
			<!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
			<!--            </xsl:if>-->
			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-button'" />
			</xsl:call-template>

		</xsl:element>
	</xsl:template>

	<!-- build trigger -->
	<!-- ### please note that triggers are always submit buttons cause this stylesheet assumes no javascript ### -->
	<xsl:template name="trigger">
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="$scripted='true'">
					<xsl:attribute name="type">button</xsl:attribute>
					<xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="type">submit</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="value">
				<xsl:value-of select="xforms:label" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(xforms:hint)" />
			</xsl:attribute>
			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-button'" />
			</xsl:call-template>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>
			<!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
			<!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
			<!--            </xsl:if>-->
			<xsl:if test="@xforms:accesskey">
				<xsl:attribute name="accesskey">
					<xsl:value-of select="@xforms:accesskey" />
				</xsl:attribute>
				<xsl:attribute name="title">
					<xsl:value-of select="normalize-space(xforms:hint)" />
					- KEY: [ALT]+
					<xsl:value-of select="@xforms:accesskey" />
				</xsl:attribute>
			</xsl:if>
			<xsl:if
				test="contains(@xforms:src,'.gif') or contains(@xforms:src,'.jpg') or contains(@xforms:src,'.png')">
				<img src="{@xforms:src}" id="{@id}-label" />
			</xsl:if>

		</xsl:element>

	</xsl:template>

	<!-- build upload control -->
	<xsl:template name="upload">
		<!-- the stylesheet using this template has to take care, that form enctype is set to 'multipart/form-data' -->
		<xsl:variable name="repeat-id"
			select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
		<xsl:variable name="pos" select="position()" />
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="boolean(string-length(@id) > 0)">
					<xsl:value-of select="@id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@ref" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:element name="input">
			<xsl:attribute name="id">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$id" />
			</xsl:attribute>
			<xsl:attribute name="type">file</xsl:attribute>
			<xsl:attribute name="value"></xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="normalize-space(xforms:hint)" />
			</xsl:attribute>
			<xsl:if test="chiba:data/@chiba:readonly='true'">
				<xsl:attribute name="disabled">disabled</xsl:attribute>
			</xsl:if>

			<xsl:call-template name="assembleRepeatClasses">
				<xsl:with-param name="repeat-id" select="$repeat-id" />
				<xsl:with-param name="pos" select="$pos" />
				<xsl:with-param name="classes"
					select="'portlet-form-input-field'" />
			</xsl:call-template>

			<!-- Content types accepted, from mediatype xforms:upload attribute
				to accept input attribute -->
			<xsl:attribute name="accept">
				<xsl:value-of
					select="translate(normalize-space(@mediatype),' ',',')" />
			</xsl:attribute>
			<xsl:if test="$scripted='true'">
				<xsl:choose>
					<xsl:when test="@xforms:onchange">
						<xsl:attribute name="onchange">
							<xsl:value-of select="@xforms:onchange" />
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="onchange">javascript:upload('<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$id" />');</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:element>
		<xsl:if test="xforms:filename">
			<input type="hidden" id="{xforms:filename/@id}"
				value="{xforms:filename/chiba:data}" />
		</xsl:if>
		<xsl:if test="@chiba:destination">
			<!-- create hidden parameter for destination -->
			<input type="hidden" id="{$id}-destination"
				value="{@chiba:destination}" />
		</xsl:if>

		<xsl:call-template name="handleRequired" />
	</xsl:template>


	<!-- ######################################################################################################## -->
	<!-- ########################################## HELPER TEMPLATES FOR SELECT, SELECT1 ######################## -->
	<!-- ######################################################################################################## -->

	<xsl:template name="build-items">

		<xsl:param name="parent" />

		<xsl:variable name="messages" select="msg:getInstance()" />

		<!-- add an empty item, cause otherwise deselection is not possible -->
		<option value="">
			<xsl:value-of select="msg:getXslString($messages, 'UI.USER_CHOOSE')" disable-output-escaping="yes"/>
		</option>

		<!-- todo: handle xforms:choice -->
		<xsl:variable name="items"
			select="$parent//xforms:item[not(ancestor::chiba:data)]" />
		<xsl:for-each select="$items">
			<option id="{@id}" value="{xforms:value}"
				title="{xforms:hint}">

				<xsl:variable name="is_selected">
					<xsl:call-template name="isSelected">
						<xsl:with-param name="value_param"
							select="xforms:value" />
						<xsl:with-param name="id_param"
							select="$parent/@id" />
					</xsl:call-template>
				</xsl:variable>

				<xsl:if
					test="boolean(string-length($is_selected) > 0)">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="xforms:label" />
			</option>
		</xsl:for-each>
	</xsl:template>

	<!-- overwrite/change this template, if you don't like the way labels are rendered for checkboxes -->
	<xsl:template name="build-checkboxes">
		<xsl:param name="id" />
		<xsl:param name="name" />
		<xsl:param name="parent" />
		<xsl:param name="type" select="''"/>
		<xsl:param name="columns" select="1"/>

		<xsl:if test="$type='full-scroll'">
			<xsl:text disable-output-escaping="yes">&lt;div style="height:95px;overflow:auto">&lt;table></xsl:text>
		</xsl:if>
		<!-- todo: handle xforms:choice -->
		<xsl:variable name="items"
			select="$parent//xforms:item[not(ancestor::chiba:data)]" />
			<xsl:if test="$type='full-scroll'">
					<xsl:text disable-output-escaping="yes">&lt;tr></xsl:text>
			</xsl:if>
		<xsl:for-each select="$items">
			<xsl:if test="$type='full-scroll'">
				<xsl:if test="((position()-1) mod number($columns)) = 0">
					<xsl:text disable-output-escaping="yes">&lt;/tr>&lt;tr></xsl:text>
				</xsl:if>
				<xsl:text disable-output-escaping="yes">&lt;td></xsl:text>
			</xsl:if>
			<xsl:variable name="title">
				<xsl:choose>
					<xsl:when test="xforms:hint">
						<xsl:value-of select="xforms:hint" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$parent/xforms:hint" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:variable name="is_selected">
				<xsl:call-template name="isSelected">
					<xsl:with-param name="value_param"
						select="xforms:value" />
					<xsl:with-param name="id_param"
						select="$parent/@id" />
				</xsl:call-template>
			</xsl:variable>

			<xsl:for-each
				select="//xforms:model/xforms:instance/data/*">
				<xsl:if test="not(//@ref = local-name())">
					<xsl:call-template name="hidden" />
				</xsl:if>
			</xsl:for-each>

			<input id="{$id}_{xforms:value}" class="portlet-form-field" type="checkbox"
				name="{$name}" value="{xforms:value}" title="{$title}">
				<xsl:if
					test="$parent/chiba:data/@chiba:readonly='true'">
					<xsl:attribute name="disabled">disabled</xsl:attribute>
				</xsl:if>
				<xsl:if
					test="boolean(string-length($is_selected) > 0)">
					<xsl:attribute name="checked">checked</xsl:attribute>
				</xsl:if>
				<xsl:if test="$scripted='true'">
					<xsl:attribute name="onclick">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$parent/@id" />');</xsl:attribute>
				</xsl:if>
			</input>
			<span id="{$id}-label" class="portlet-form-field-label">
				<xsl:if
					test="$parent/chiba:data/@chiba:readonly='true'">
					<xsl:attribute name="disabled">disabled</xsl:attribute>
				</xsl:if>
				<xsl:apply-templates select="xforms:label" />
			</span>
			<xsl:if test="$type='full-scroll'">
				<xsl:text disable-output-escaping="yes">&lt;/td></xsl:text>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="$type='full-scroll'">
			<xsl:text disable-output-escaping="yes">&lt;/tr>&lt;/table>&lt;/div></xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- overwrite/change this template, if you don't like the way labels are rendered for checkboxes -->
	<xsl:template name="build-radiobuttons">
		<xsl:param name="id" />
		<xsl:param name="name" />
		<xsl:param name="parent" />
		<xsl:param name="brbetween" select="'false'"/>

		<!-- todo: handle xforms:choice -->
		<xsl:variable name="items"
			select="$parent//xforms:item[not(ancestor::chiba:data)]" />
		<xsl:for-each select="$items">
			<xsl:variable name="title">
				<xsl:choose>
					<xsl:when test="xforms:hint">
						<xsl:value-of select="xforms:hint" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$parent/xforms:hint" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="is_selected">
				<xsl:call-template name="isSelected">
					<xsl:with-param name="value_param"
						select="xforms:value" />
					<xsl:with-param name="id_param"
						select="$parent/@id" />
				</xsl:call-template>
			</xsl:variable>

			<input id="{$id}_{xforms:value}" class="portlet-form-field" type="radio"
				name="{$name}" value="{xforms:value}" title="{$title}">
				<xsl:if
					test="$parent/chiba:data/@chiba:readonly='true'">
					<xsl:attribute name="disabled">disabled</xsl:attribute>
				</xsl:if>
				<xsl:if
					test="boolean(string-length($is_selected) > 0)">
					<xsl:attribute name="checked">checked</xsl:attribute>
				</xsl:if>
				<xsl:if test="$scripted='true'">
					<xsl:attribute name="onclick">javascript:setXFormsValue('form<xsl:value-of select="/filters/id"/>', '<xsl:value-of select="$parent/@id" />');</xsl:attribute>
				</xsl:if>
			</input>
			<span id="{$id}-label" class="portlet-form-field-label">
				<xsl:if
					test="$parent/chiba:data/@chiba:readonly='true'">
					<xsl:attribute name="disabled">disabled</xsl:attribute>
				</xsl:if>
				<xsl:apply-templates select="xforms:label" />
			</span>
			<xsl:if
			  test="$brbetween='true'">
			  <xsl:element name="br" />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- handles required/optional property -->
	<xsl:template name="handleRequired">
		<xsl:choose>
			<xsl:when test="chiba:data/@chiba:required='true'">
				<span id="{@id}-required" class="required-symbol">
					*
				</span>
			</xsl:when>
			<xsl:otherwise>
				<span id="{@id}-required" class="required-symbol"></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ########## builds indexed classname for styling repeats rendered as tables ########## -->
	<xsl:template name="assembleRepeatClasses">
		<xsl:param name="repeat-id" />
		<xsl:param name="pos" />
		<xsl:param name="classes" />
		<xsl:choose>
			<xsl:when test="boolean(string-length($repeat-id) > 0)">
				<xsl:attribute name="class">
					<xsl:value-of
						select="concat($repeat-id,'-',$pos,' ',$classes)" />
				</xsl:attribute>
			</xsl:when>
			<xsl:when test="boolean(string-length(@class) > 0)">
				<xsl:attribute name="class">
					<xsl:value-of select="concat(@class, ' ',$classes)" />
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="class">
					<xsl:value-of select="$classes" />
				</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
