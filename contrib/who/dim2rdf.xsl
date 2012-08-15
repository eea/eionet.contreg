<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
  <!ENTITY ontology  "http://rdfdata.eionet.europa.eu/who/ontology/">
  <!ENTITY prefix  "http://rdfdata.eionet.europa.eu/habdir/species/">
]>

<xsl:stylesheet
  xmlns:h="&ontology;"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:cc="http://creativecommons.org/ns#"
version="1.0">

<xsl:output method="xml" indent="yes"/>

<xsl:template match="dimensions">
	<rdf:RDF>
		<!-- <xsl:attribute name="xml:base">&prefix;</xsl:attribute> -->
		<xsl:apply-templates select="dimension"/>
	</rdf:RDF>
</xsl:template>

<xsl:template match="dimension">
		<rdf:Description rdf:about="">
			<rdfs:label>WHO code list for <xsl:value-of select="@name"/> from http://apps.who.int/athena</rdfs:label>
			<dcterms:source>WHO</dcterms:source>
<!--
			<dcterms:date rdf:datatype="&xsd;date">2012-08-22</dcterms:date>
			<cc:license rdf:resource="http://creativecommons.org/licenses/by/2.5/dk/"/>
			<cc:morePermissions rdf:resource="http://www.eea.europa.eu/legal/copyright"/>
-->
		</rdf:Description>
		<xsl:apply-templates select="code"/>
</xsl:template>

<xsl:template match="code">
	<skos:Concept>
		<xsl:attribute name="rdf:ID"><xsl:value-of select="@label"/></xsl:attribute>
		<skos:notation><xsl:value-of select="@label"/></skos:notation>
		<skos:prefLabel><xsl:value-of select="@name"/></skos:prefLabel>
	</skos:Concept>
</xsl:template>

<xsl:template match="*"/>

</xsl:stylesheet>
