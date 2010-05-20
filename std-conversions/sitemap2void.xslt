<?xml version="1.0" encoding="UTF-8"?>
<stylesheet
    xmlns:xsl  ="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns      ="http://www.w3.org/1999/XSL/Transform"
    xmlns:s    ="http://www.sitemaps.org/schemas/sitemap/0.9"
    xmlns:sc   ="http://sw.deri.org/2007/07/sitemapextension/scschema.xsd"
    xmlns:rdf  ="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs ="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:bibo ="http://purl.org/ontology/bibo/"
    xmlns:dcterms ="http://purl.org/dc/terms/"
    xmlns:void ="http://rdfs.org/ns/void#">


<output indent="yes" method="xml" media-type="application/rdf+xml" encoding="UTF-8" omit-xml-declaration="no"/>

<template match="s:urlset">
    <rdf:RDF xmlns:rdf ="http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
        <apply-templates select="sc:dataset" /> 		
        <apply-templates select="s:url" /> 		
    </rdf:RDF>
</template>

<template match="s:url">
    <xsl:element name="bibo:Webpage">
		<!-- map sc:datasetURI to dataset -->	
		<xsl:attribute  name="rdf:about">
			<xsl:value-of select="s:loc" />
		</xsl:attribute> 
    </xsl:element> 
</template>

<template match="sc:dataset">
  
	<xsl:element name="void:Dataset">

		<!-- map sc:datasetURI to dataset -->	
		<xsl:attribute  name="rdf:about">
			<xsl:value-of select="sc:datasetURI" />
		</xsl:attribute> 
		
		<!-- map sc:datasetLabel to rdfs:comment -->
		<xsl:element name="rdfs:label">
			<xsl:value-of select="sc:datasetLabel" />
		</xsl:element>		
		
   		<!-- process sub-elements  -->
		<apply-templates select="sc:sampleURI" /> 		
		<apply-templates select="sc:sparqlEndpointLocation" /> 
		<apply-templates select="sc:dataDumpLocation" /> 	
		<apply-templates select="sc:linkedDataPrefix" /> 		
   </xsl:element> 
   
</template>


<!-- map sc:sampleURI to void:exampleResource -->
<template match="sc:sampleURI">
	<xsl:element name="void:exampleResource">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="." />
		</xsl:attribute> 
   </xsl:element>  
</template>
 
<!-- map sc:sparqlEndpointLocation to void:sparqlEndpoint -->
<template match="sc:sparqlEndpointLocation">
	<xsl:element name="void:sparqlEndpoint">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="." />
		</xsl:attribute> 
   </xsl:element>  
</template>  

<!-- map sc:dataDumpLocation to void:dataDumpLocation -->
<template match="sc:dataDumpLocation">
	<xsl:element name="void:dataDumpLocation">
		<xsl:attribute name="rdf:resource">
			<xsl:value-of select="." />
		</xsl:attribute> 
   </xsl:element>  
</template>  


<!-- map sc:linkedDataPrefix to void:uriPattern -->
<template match="sc:linkedDataPrefix">
	<xsl:element name="void:uriRegexPattern">^<xsl:value-of select="." />$</xsl:element>  
</template>  



<!-- ignore the rest of the DOM -->
<template match="text()|@*|*"><apply-templates /></template>


</stylesheet>
