<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/> 
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
<xsl:template match="table">
  <table>
    <xsl:copy-of select="@*" />
    <xsl:apply-templates select="*"/>
  </table>
</xsl:template>
  
  <xsl:template match="thead">
    <thead>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="tr"/>
    </thead>
  </xsl:template>
  <xsl:template match="tbody">
    <tbody>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="tr"/>
    </tbody>  
  </xsl:template>
  <xsl:template match="tfoot">
    <tfoot>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="tr"/>
    </tfoot>
  </xsl:template>

  <xsl:template match="tr">
    <xsl:variable name="evenOddClass">
      <xsl:choose>
        <xsl:when test="(position() mod 2) != 1">
            <xsl:text>evenRow</xsl:text>
        </xsl:when>
        <xsl:otherwise>
            <xsl:text>oddRow</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <tr>
      <xsl:copy-of select="@*[name()!='class']"/>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@class">
            <xsl:value-of select="concat(concat(@class, ' '), $evenOddClass)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$evenOddClass"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
	
      <xsl:apply-templates select="*|node()|text()"/>
    </tr>
    
  </xsl:template>
</xsl:stylesheet>
