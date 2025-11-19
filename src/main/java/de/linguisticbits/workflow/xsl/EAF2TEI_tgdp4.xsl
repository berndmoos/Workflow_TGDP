<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    
    <!-- <tei:spanGrp xml:lang="en" type="translation">
        <tei:span from="a4_w1" to="a4_p12_d26e1">A Ford Model T - yeah.
            (laugh)</tei:span>
    </tei:spanGrp> -->
    
    <xsl:template match="tei:spanGrp[@type='translation']/tei:span">
        <xsl:copy>
            <xsl:attribute name="from" select="ancestor::tei:annotationBlock/descendant::tei:seg[@type='contribution']/descendant::*[@xml:id][1]/@xml:id"/>
            <xsl:attribute name="to" select="ancestor::tei:annotationBlock/descendant::tei:seg[@type='contribution']/descendant::*[@xml:id][last()]/@xml:id"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- 
        <tei:seg type="contribution" xml:id="d97e339">
            <tei:anchor synch="ts12"/>
            <tei:anchor synch="ts13"/>
        </tei:seg>    
    -->
    
    <xsl:template match="//tei:annotationBlock[count(descendant::tei:seg[@type='contribution']/*[name()!='tei:anchor'])=0]"/>
    
    <xsl:template match="@from">
        <xsl:choose>
            <xsl:when test="id(.)"><xsl:copy/></xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="from" select="ancestor::tei:annotationBlock/descendant::tei:seg[@type='contribution']/descendant::*[@xml:id][1]/@xml:id"/>                
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@to">
        <xsl:choose>
            <xsl:when test="id(.)"><xsl:copy/></xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="to" select="ancestor::tei:annotationBlock/descendant::tei:seg[@type='contribution']/descendant::*[@xml:id][last()]/@xml:id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>