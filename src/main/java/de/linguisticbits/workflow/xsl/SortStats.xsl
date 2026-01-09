<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="corpus-statistics">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="interview">
                <xsl:sort select="tokenize(@id, '-')[2]" data-type="number"/>
                <xsl:sort select="tokenize(@id, '-')[1]" data-type="number"/>
            </xsl:apply-templates>
        </xsl:copy>        
    </xsl:template>
    
    
</xsl:stylesheet>