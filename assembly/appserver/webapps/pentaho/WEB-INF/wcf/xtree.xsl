<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
  if this is a nested component, the global stylesheet parameters $renderId and $border
  are not used. Instead, the NodeHandler may define @border and @renderId attributes
-->

<xsl:param name="tree-border" select="'1'"/>


<xsl:template name="xtree-renderId">
  <xsl:choose>
    <xsl:when test="@renderId">
      <xsl:value-of select="@renderId"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$renderId"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="xtree-border">
  <xsl:choose>
    <xsl:when test="@border">
      <xsl:value-of select="@border"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$tree-border"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--
  for handwritten xtree element where the tree is part of
  a form
-->

<xsl:template match="xtree">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="xtree-component">
  <table cellspacing="0">
    <xsl:attribute name="id">
      <xsl:call-template name="xtree-renderId"/>
    </xsl:attribute>
    <xsl:attribute name="border">
      <xsl:call-template name="xtree-border"/>
    </xsl:attribute>
    <xsl:attribute name="cellpadding">
      <xsl:call-template name="xtree-border"/>
    </xsl:attribute>

    <xsl:if test="@width">
      <xsl:attribute name="width">
        <xsl:value-of select="@width"/>
      </xsl:attribute>
    </xsl:if>

    <xsl:call-template name="xtree-title"/>
    <xsl:apply-templates select="tree-extras-top"/>
    <xsl:apply-templates select="tree-node"/>
    <xsl:apply-templates select="tree-extras-bottom"/>
    <xsl:apply-templates select="buttons"/>

  </table>
</xsl:template>


<xsl:template name="xtree-title">
  <xsl:if test="@title or @closeId">
    <tr>
      <th class="xform-title">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <xsl:if test="@title">
              <th align="left" class="xform-title">
                <xsl:value-of select="@title"/>
              </th>
            </xsl:if>
            <xsl:if test="@closeId">
              <td align="right" class="xform-close-button">
                <input type="image" src="{$context}/wcf/form/cancel.png" name="{@closeId}" width="16" height="16"/>
              </td>
            </xsl:if>
          </tr>
        </table>
      </th>
    </tr>
  </xsl:if>
  <xsl:if test="@error">
    <tr>
      <td class="xform-error">
        <xsl:value-of select="@error"/>
      </td>
    </tr>
  </xsl:if>
</xsl:template>


<xsl:template match="tree-node">
  <tr>
    <td nowrap="nowrap" class="tree-node-{@style}">

      <div style="margin-left: {@level}em">
        <!-- checkbox / radiobox is handled by controls.xsl -->
        <xsl:apply-templates select="checkBox|radioButton"/>
        
        <xsl:if test="@buttonId">
          <xsl:choose>
            <xsl:when test="@selected">
              <input border="0" type="image" name="{@buttonId}" src="{$context}/wcf/tree/select1.png" width="13" height="13"/>
              <xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <input border="0" type="image" name="{@buttonId}" src="{$context}/wcf/tree/select0.png" width="13" height="13"/>
              <xsl:text> </xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>

        <!-- expand/collapse button -->
        <xsl:choose>
          <xsl:when test="@state='bounded'">
            <input border="0" type="image" name="{@id}.unbound" src="{$context}/wcf/tree/unbound.png" width="9" height="9"/>
          </xsl:when>
          <xsl:when test="@state='expanded'">
            <input border="0" type="image" name="{@id}.collapse" src="{$context}/wcf/tree/collapse.png" width="9" height="9"/>
          </xsl:when>
          <xsl:when test="@state='collapsed'">
            <input border="0" type="image" name="{@id}.expand" src="{$context}/wcf/tree/expand.png" width="9" height="9"/>
          </xsl:when>
          <xsl:otherwise>
            <img src="{$context}/wcf/tree/leaf.png" width="9" height="9"/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:apply-templates select="move-button"/>

        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="@hrefId">
            <a href="?{$token}&amp;{@hrefId}=x">
              <xsl:value-of select="@label"/>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@label"/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:apply-templates select="delete-button"/>

      </div>
    </td>
  </tr>
  <xsl:apply-templates select="tree-node"/>
</xsl:template>

<xsl:template match="delete-button">
  <xsl:text> </xsl:text>
  <input type="image" border="0" name="{@id}" src="{$context}/wcf/tree/delete.png" width="9" height="9"/>
</xsl:template>

<xsl:template match="buttons">
  <tr>
    <td align="right">
      <div align="right">
        <xsl:apply-templates/>
      </div>
    </td>
  </tr>
</xsl:template>


</xsl:stylesheet>
