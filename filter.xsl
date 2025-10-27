<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:oai="http://www.openarchives.org/OAI/2.0/"
    xmlns:sx="java:nl.mpi.tla.saxon" exclude-result-prefixes="sx">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
    <xsl:param name="provider_uri" select="()"/>
    <xsl:param name="config" select="()"/>
    
    <xsl:variable name="rec" select="/"/>
    
    <xsl:template match="/" priority="1">
        <xsl:copy>
            <xsl:variable name="filter" select="$config//provider[@url=$provider_uri]/filter"/>
            <xsl:choose>
                <xsl:when test="normalize-space($filter)!=''">
                    <xsl:choose>
                        <xsl:when test="sx:evaluate($rec, $filter, $filter)">
                            <xsl:next-match/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:message>INF: skipped record</xsl:message> 
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:next-match/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
        
</xsl:stylesheet>