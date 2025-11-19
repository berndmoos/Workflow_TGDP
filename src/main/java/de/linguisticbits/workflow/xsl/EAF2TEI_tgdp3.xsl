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

    <!--     
        <tei:pc xml:id="a22_p1_d97419e1">(</tei:pc>
        <tei:w norm="laugh" xml:id="a22_w2">laugh</tei:w>
        <tei:pc xml:id="a22_p3_d97420e1">)</tei:pc>                  
    -->
    
    <!-- 
        <incident xml:id="n2">
            <desc rend="((lacht 1.63s))">lacht 1.63s</desc>
        </incident>
    
    -->
    
    <xsl:template match="tei:w[preceding-sibling::*[1][self::tei:pc and text()='('] and following-sibling::*[1][self::tei:pc and text()=')']]">
        <tei:incident>
            <xsl:attribute name="xml:id" select="@xml:id"/>
            <tei:desc>
                <xsl:attribute name="rend">(<xsl:value-of select="text()"/>)</xsl:attribute>
                <xsl:value-of select="text()"/>
            </tei:desc>
        </tei:incident>
    </xsl:template>
    
    <xsl:template match="tei:pc[text()='(' and following-sibling::*[2][self::tei:pc and text()=')']]"/>
    <xsl:template match="tei:pc[text()=')' and preceding-sibling::*[2][self::tei:pc and text()='(']]"/>
    
    <!-- 
        <tei:pc xml:id="a10_p4_d97462e1">(</tei:pc>
        <tei:w norm="family" xml:id="a10_w5">family</tei:w>
        <tei:w norm="name" xml:id="a10_w7">name</tei:w>
        <tei:pc xml:id="a10_p8_d97463e1">)</tei:pc>
     -->    
    
    <xsl:template match="tei:w[preceding-sibling::*[1][self::tei:pc and text()='('] and following-sibling::*[1][self::tei:w] and following-sibling::*[2][self::tei:pc and text()=')']]">
        <tei:incident>
            <xsl:attribute name="xml:id" select="@xml:id"/>
            <tei:desc>
                <xsl:attribute name="rend">(<xsl:value-of select="text()"/><xsl:text> </xsl:text><xsl:value-of select="following-sibling::*[1]/text()"/>)</xsl:attribute>
                <xsl:value-of select="text()"/><xsl:text> </xsl:text><xsl:value-of select="following-sibling::*[1]/text()"/>
            </tei:desc>
        </tei:incident>
    </xsl:template>
    
    <xsl:template match="tei:pc[text()='(' and following-sibling::*[3][self::tei:pc and text()=')']]"/>
    <xsl:template match="tei:pc[text()=')' and preceding-sibling::*[3][self::tei:pc and text()='(']]"/>
    <xsl:template match="tei:w[preceding-sibling::*[2][self::tei:pc and text()='('] and following-sibling::*[1][self::tei:pc and text()=')']]"/>
    
</xsl:stylesheet>