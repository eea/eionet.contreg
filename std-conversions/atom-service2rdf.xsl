<?xml version="1.0" encoding="UTF-8"?>
<stylesheet
    xmlns:xsl  ="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns      ="http://www.w3.org/1999/XSL/Transform"
    xmlns:app  ="http://www.w3.org/2007/app"
    xmlns:atom = "http://www.w3.org/2005/Atom"
    xmlns:rdf  ="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs ="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:bibo ="http://purl.org/ontology/bibo/"
    xmlns:dcterms ="http://purl.org/dc/terms/"
    xmlns:void ="http://rdfs.org/ns/void#">
<!-- Convert service documents into RDF
     See http://www.ietf.org/rfc/rfc5023.txt
-->

<output indent="yes" method="xml" media-type="application/rdf+xml" encoding="UTF-8" omit-xml-declaration="no"/>

<template match="app:service">
    <rdf:RDF>
        <xsl:copy-of select="@xml:lang"/>
        <xsl:copy-of select="@xml:base"/>
        <app:Service rdf:about="">
                <xsl:for-each select="app:workspace">
                  <app:workspace>
                    <app:Workspace>
        <xsl:for-each select="atom:title">
            <rdfs:label>
                <xsl:apply-templates select="." mode="object"/>
            </rdfs:label>
        </xsl:for-each>
        <xsl:for-each select="app:collection">
            <app:collection>
                <xsl:apply-templates select="." />
            </app:collection>
        </xsl:for-each>
                    </app:Workspace>
              </app:workspace>
         </xsl:for-each>
        </app:Service>
    </rdf:RDF>
</template>


<template match="app:collection">

    <xsl:element name="app:Collection">
        <xsl:attribute  name="rdf:about">
            <xsl:value-of select="@href" />
        </xsl:attribute>
        <xsl:for-each select="atom:title">
            <rdfs:label>
                <xsl:apply-templates select="." mode="object"/>
            </rdfs:label>
        </xsl:for-each>

</xsl:element>
</template>

  <xsl:template match="*[not(*)]" mode="object">
    <xsl:copy-of select="@xml:lang"/>
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="*[*]" mode="object">
    <xsl:attribute name="rdf:parseType">Literal</xsl:attribute>
    <xsl:copy-of select="@xml:lang"/>
    <xsl:copy-of select="node()"/>
  </xsl:template>

  <xsl:template name="uri">
    <xsl:param name="uri"/>
    <xsl:choose>
      <xsl:when test="normalize-space($uri)!=''">
        <xsl:attribute name="rdf:about">
          <xsl:value-of select="$uri"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="rdf:nodeID">
          <xsl:text>blank</xsl:text>
          <xsl:value-of select="count(preceding::*)+count(ancestor::*)"/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- ignore the rest of the DOM -->
<template match="text()|@*|*"><apply-templates /></template>


</stylesheet>
