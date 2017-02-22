<?xml version="1.0"?>
<!--
$Id: str-util.xsl,v 1.2 2005/11/29 08:15:34 jdixon Exp $

This XSLT stylesheet offers functions to manipulate strings
-->
<?xml-stylesheet href="http://www.w3.org/StyleSheets/base.css" type="text/css"?>
<?xml-stylesheet href="http://www.w3.org/2002/02/style-xsl.css" type="text/css"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:str="http://www.w3.org/2001/10/str-util.xsl" version="2.0">

<div xmlns="http://www.w3.org/1999/xhtml">
           <h1>
    <a href="http://www.w3.org/">
      <img src="http://www.w3.org/Icons/w3c_home" alt="W3C" />
    </a>

    XSLT Templates to manipulate strings</h1>
    <h2>Status</h2>
    <p>The templates in this stylesheet are used in several other ones. Please don't modify it without letting me know and don't break the current exposed API.</p>
    <h2>Variables</h2>
    <dl>
    <dt>str:uc</dt>
    <dd>L'ensemble des lettres en majuscule (pratique pour conversion majuscule-minuscule)</dd>
    <dt>str:lc</dt>
    <dd>L'ensemble des lettres en minuscule (pratique pour conversion majuscule-minuscule)</dd>
    </dl>
    <h2>Templates</h2>

    <dl>
        <dt>str:keep-before</dt>
        <dd>Takes a <var>string</var> and a <var>delimiter</var> parameters and returns the part of the string before the first appearance of <var>delimiter</var> in <var>string</var>, the complete <var>string</var> if <var>delimiter</var> is not in <var>string</var>.</dd>
	<dt>str:ends-with</dt>
	<dd>Takes a <var>string</var> and a <var>string2</var> parameters and returns <code>yes</code> if <var>string</var> ends with <var>string2</var>, nothing otherwise (please use <code>normalize-space()</code> on the results to avoid bad surprises</dd>
    <dt>str:has-token</dt>
    <dd>Takes a <var>string</var>, a <var>token</var> and an optinal <var>delimiter</var> (default at " ") parameters and returns if <var>token</var> is a token of <var>string</var> delimited by <var>delimiter</var> the value <var>token</var>, nothing (that is, an unspecified number of spaces; please use <code>normalize-space()</code> on the results to avoid bad surprises) else.</dd>
<dt>str:keep-before-last</dt>
        <dd>Takes a <var>string</var> and a <var>delimiter</var> parameters and returns the part of the string before the last appearance of <var>delimiter</var> in <var>string</var>, the complete <var>string</var> if <var>delimiter</var> is not in <var>string</var>.</dd>

    </dl>
<hr/>
<address>$Id: str-util.xsl,v 1.2 2005/11/29 08:15:34 jdixon Exp $<br/><a href="../../People/Dom/">Dominique Hazael-Massieux</a></address>
<hr/>
</div>

  <xsl:variable name="str:uc">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

  <xsl:variable name="str:lc">abcdefghijklmnopqrstuvwxyz</xsl:variable>



  <xsl:template name="str:keep-before">
    <xsl:param name="string"/>
    <xsl:param name="delimiter"/>
    <xsl:choose>
      <xsl:when test="$string">
        <xsl:choose>
          <xsl:when test="contains($string,$delimiter)">
            <xsl:value-of
            select="normalize-space(substring-before($string,$delimiter))" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="normalize-space($string)" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space($string)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="str:ends-with">
    <xsl:param name="string"/>
    <xsl:param name="string2"/>
    <xsl:if test="contains($string,$string2)">
      <xsl:variable name="length" select="string-length($string)"/>
      <xsl:variable name="suffix_length" select="string-length($string2)"/>
      <xsl:if test="boolean(substring($string,number($length - 
$suffix_length + 1))=$string2)">
        <xsl:text>yes</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template name="str:has-token">
    <xsl:param name="string"/>
    <xsl:param name="token"/>
    <xsl:param name="delimiter" select="' '"/>
    <xsl:if test="$string=$token or starts-with($string,concat($token,$delimiter)) or contains($string,concat($delimiter,$token,$delimiter)) or (contains($string,concat($delimiter,$token)) and substring($string,string-length($string) - string-length($token) - string-length($delimiter) + 1)=concat($delimiter,$token))"><xsl:value-of select="$token"/></xsl:if>
  </xsl:template>

  <xsl:template name="str:keep-before-last">
    <xsl:param name="string"/>
    <xsl:param name="delimiter"/>
    <xsl:choose>
      <!-- If the string is empty, we don't need to go further (@@@ really?)-->
      <xsl:when test="$string">
        <xsl:choose>
          <!-- Does the string contains the said delimiter? -->
          <xsl:when test="contains($string,$delimiter)">
            <xsl:choose>
              <!-- Does the part of the string after the delimiter still contains the delimiter? -->
              <xsl:when test="contains(substring-after($string,$delimiter),$delimiter)">
                <!-- if yes, we concatene the first part of the string with the result of the (recursive) call to this template on the second part of the string -->
                <xsl:value-of select="concat(substring-before($string,$delimiter),$delimiter)"/><xsl:call-template name="str:keep-before-last">
                <xsl:with-param name="string" select="substring-after($string,$delimiter)"/>
                <xsl:with-param name="delimiter" select="$delimiter"/>
              </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <!-- Otherwise, we have delimited what we want -->
                <xsl:value-of
            select="substring-before($string,$delimiter)" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- The delimiter is not in the string, end of story -->
            <xsl:value-of select="$string" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- If the string has a null value, we just send it back as is -->
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="keep-after-last">
    <xsl:param name="string"/>
    <xsl:param name="delimiter"/>
    <xsl:choose>
      <!-- If the string is empty, we don't need to go further (@@@ really?)-->
      <xsl:when test="$string">
        <xsl:choose>
          <!-- Does the string contains the said delimiter? -->
          <xsl:when test="contains($string,$delimiter)">
            <xsl:choose>
              <!-- Does the part of the string after the delimiter still contains the delimiter? -->
              <xsl:when test="contains(substring-after($string,$delimiter),$delimiter)">
                <!-- if yes, we concatene the first part of the string with the result of the (recursive) call to this template on the second part of the string -->
                <xsl:value-of select="substring-after($string,$delimiter)"/><xsl:call-template name="keep-after-last">
                <xsl:with-param name="string" select="substring-after($string,$delimiter)"/>
                <xsl:with-param name="delimiter" select="$delimiter"/>
              </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <!-- Otherwise, we have delimited what we want -->
                <xsl:value-of
            select="substring-after($string,$delimiter)" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- The delimiter is not in the string, end of story -->
            <xsl:value-of select="$string" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- If the string has a null value, we just send it back as is -->
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>