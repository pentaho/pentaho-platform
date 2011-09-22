<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- **************************** Error Handling ************************************* -->

<xsl:template match="imgButton|button|label|textField|textArea|password|checkBox|radioButton|listBox1|listBoxN|fileUpload">
  <xsl:choose>
    <xsl:when test="@hidden='true'"/>  <!-- ignore hidden elements -->
    <xsl:when test="@error">
      <table border="0" cellspacing="0" cellpadding="3">
        <tr>
          <td bgcolor="red">
            <xsl:apply-templates mode="ctrl" select="."/>
          </td>
        </tr>
      </table>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates mode="ctrl" select="."/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- **************************** Buttons ************************************* -->

<xsl:template mode="ctrl" match="button[@disabled='true']">
  <input type="button" value="{@label}" disabled="disabled"/>
</xsl:template>

<xsl:template mode="ctrl" match="button">
  <input type="submit" name="{@id}" value="{@label}">
    <xsl:call-template name="stdattrs"/>
  </input>
</xsl:template>

<xsl:template mode="ctrl" match="imgButton[@disabled='true']">
  <img src="{$context}/{@src}" title="{@label}" alt="{@label}"/>
</xsl:template>

<xsl:template mode="ctrl" match="imgButton">
  <input type="image" src="{$context}/{@src}" name="{@id}" title="{@label}" alt="{@label}">
    <xsl:call-template name="stdattrs"/>
  </input>
</xsl:template>

<!-- **************************** Label ************************************* -->

<xsl:template mode="ctrl" match="label[@value]">
  <xsl:value-of select="@value"/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template mode="ctrl" match="label[@default-value and @value='']">
  <xsl:value-of select="@default-value"/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template mode="ctrl" match="label">
  <xsl:apply-templates/>
</xsl:template>

<!-- **************************** Text ************************************* -->

<xsl:template mode="ctrl" match="textField[@disabled='true']">
  <xsl:value-of select="@value"/>
</xsl:template>

<xsl:template mode="ctrl" match="textField">
  <xsl:choose >
    <xsl:when test="@calendar">
      <table>
        <tr>
          <td>
            <input type="text" name="{@id}" value="{@value}">
              <xsl:apply-templates select="@maxlength"/>
              <xsl:call-template name="stdattrs"/>
            </input>
          </td>
          <td>
            <img src="{$context}/{@src}" onclick="jscalendarPopUpCalendar(this,document.getElementById('{@id}'), '{@calendar}')"/>  
          </td>
        </tr>
      </table>
    </xsl:when>
    <xsl:otherwise> 
      <input type="text" name="{@id}" value="{@value}">
        <xsl:apply-templates select="@maxlength"/>
        <xsl:call-template name="stdattrs"/>
      </input>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates/>
</xsl:template>


<!-- **************************** Text Area ************************************* -->

<xsl:template mode="ctrl" match="textArea[@disabled='true']">
  <span class="xform-textarea">
    <xsl:value-of select="@value"/>
  </span>
</xsl:template>

<xsl:template mode="ctrl" match="textArea">
 <textarea name="{@id}" rows="{@rows}" cols="{@cols}" class="xform-textarea">
    <xsl:if test="@wrap">
      <xsl:attribute name="wrap"><xsl:value-of select="@wrap"/></xsl:attribute>
    </xsl:if>
    <xsl:if test="@readonly">
      <xsl:attribute name="readonly">true</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="stdattrs"/>
    <xsl:value-of select="@value"/>
 </textarea>
 <xsl:apply-templates/>
</xsl:template>


<!-- **************************** Password ************************************* -->

<xsl:template mode="ctrl" match="password[@disabled='true']">
  <xsl:text>****</xsl:text>
</xsl:template>

<xsl:template mode="ctrl" match="password">
  <input type="password" name="{@id}" value="{@value}">
    <xsl:apply-templates select="@maxlength"/>
    <xsl:call-template name="stdattrs"/>
  </input>
  <xsl:apply-templates/>
</xsl:template>


<!-- **************************** CheckBox ************************************* -->

<xsl:template mode="ctrl" match="checkBox">
  <input type="checkbox" name="{@id}" value="x">
    <xsl:call-template name="stdattrs"/>
    <xsl:if test="@selected='true'">
      <xsl:attribute name="checked">checked</xsl:attribute>
    </xsl:if>
    <xsl:if test="@disabled='true'">
      <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
  </input>
  <input type="hidden" name="{@id}.valid" value="x"/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template mode="ctrl"  match="checkBoxes">
  <xsl:apply-templates/>
</xsl:template>

<!-- **************************** Radio Button ************************************* -->

<xsl:template mode="ctrl"  match="radioButton">
  <input type="radio" name="{@group-id}" value="{@id}">
    <xsl:call-template name="stdattrs"/>
    <xsl:if test="@selected='true'">
      <xsl:attribute name="checked">checked</xsl:attribute>
    </xsl:if>
    <xsl:if test="@disabled='true'">
      <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
  </input>
  <input type="hidden" name="{@id}.valid" value="x"/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template mode="ctrl"  match="radioButtons">
  <xsl:apply-templates/>
</xsl:template>

<!-- **************************** ListBox / ComboBox ************************************* -->

<!-- single selection list -->
<xsl:template mode="ctrl" match="listBox1">
  <select name="{@id}">
    <xsl:if test="@disabled='true'">
      <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@rows"/>
    <xsl:call-template name="stdattrs"/>
    <xsl:apply-templates/>
  </select>
  <input type="hidden" name="{@id}.valid" value="x"/>
</xsl:template>


<!-- multiple selection lisbox -->
<xsl:template mode="ctrl" match="listBoxN">
  <select name="{@id}" multiple="multiple">
    <xsl:if test="@disabled='true'">
      <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@rows"/>
    <xsl:call-template name="stdattrs"/>
    <xsl:apply-templates/>
  </select>
  <input type="hidden" name="{@id}.valid" value="x"/>
</xsl:template>

<!-- option of list -->
<xsl:template match="listItem">
  <xsl:if test="not(@hidden='true')">  <!-- ignore hidden elements -->
    <option value="{@id}">
      <xsl:if test="@selected='true'">
        <xsl:attribute name="selected">selected</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="@label"/>
    </option>
  </xsl:if>
</xsl:template>

<!-- **************************** File Upload ************************************* -->

<xsl:template mode="ctrl" match="fileUpload[@disabled='true']">
  <xsl:value-of select="@filename"/>
</xsl:template>

<xsl:template mode="ctrl" match="fileUpload">
  <input type="file" name="{@id}" value="{@filename}">
    <xsl:apply-templates select="@maxlength"/>
    <xsl:apply-templates select="@accept"/>
    <xsl:call-template name="stdattrs"/>
  </input>
  <xsl:apply-templates/>
</xsl:template>

<!-- **************************** Default Rule ************************************* -->

<!-- ignore parameter elements -->
<xsl:template match="param"/>

<!-- copies the common html attributes -->
<xsl:template name="stdattrs">
  <xsl:apply-templates select="@align"/>
  <xsl:apply-templates select="@id"/>
  <xsl:apply-templates select="@title"/>
  <xsl:apply-templates select="@size"/>
  <xsl:apply-templates select="@tabindex"/>
  <xsl:apply-templates select="@accesskey"/>
  <xsl:apply-templates select="@style"/>
  <xsl:apply-templates select="@onclick"/>
  <xsl:apply-templates select="@ondblclick"/>
  <xsl:apply-templates select="@onchange"/>
  <xsl:apply-templates select="@onfocus"/>
  <xsl:apply-templates select="@onblur"/>
</xsl:template>

</xsl:stylesheet>
