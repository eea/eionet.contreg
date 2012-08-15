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
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:void="http://rdfs.org/ns/void#"
  xmlns:dct="http://purl.org/dc/terms/"
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
		<void:Document rdf:about="">
			<rdfs:label>WHO datasets from http://apps.who.int/athena</rdfs:label>
			<dct:source>WHO</dct:source>
			<dct:date rdf:datatype="&xsd;date">2012-08-22</dct:date>
<!--
			<cc:license rdf:resource="http://creativecommons.org/licenses/by/2.5/dk/"/>
			<cc:morePermissions rdf:resource="http://www.eea.europa.eu/legal/copyright"/>
-->
		</void:Document>

		<foaf:Organization rdf:ID="WHO">
			<foaf:name>World Health Organization</foaf:name>
			<foaf:homepage rdf:resource="http://www.who.int/"/>
		</foaf:Organization>

		<xsl:apply-templates select="code"/>
</xsl:template>

<xsl:template match="code">
	<void:Dataset>
		<xsl:attribute name="rdf:ID"><xsl:value-of select="@label"/></xsl:attribute>
                <dct:creator rdf:resource="#WHO"/>
		<skos:notation><xsl:value-of select="@label"/></skos:notation>
		<dct:title><xsl:value-of select="@name"/></dct:title>
		<rdfs:label><xsl:value-of select="@name"/></rdfs:label>
		<void:dataDump><xsl:attribute name="rdf:resource">http://apps.who.int/athena/data/GHO/<xsl:value-of select="@label"/>.xml</xsl:attribute></void:dataDump>
		<xsl:apply-templates/>
	</void:Dataset>
</xsl:template>

<xsl:template match="description">
   <rdfs:seeAlso><xsl:attribute name="rdf:resource"><xsl:value-of select="@url"/></xsl:attribute></rdfs:seeAlso>
</xsl:template>

<xsl:template match="*"/>

</xsl:stylesheet>
