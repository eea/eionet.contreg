<?xml version="1.0" encoding="UTF-8"?>
<stylesheet
    version="1.0"
    xmlns      ="http://www.w3.org/1999/XSL/Transform"
    xmlns:cr   ="http://cr.eionet.europa.eu/ontologies/contreg.rdf#"
    xmlns:s    ="http://www.sitemaps.org/schemas/sitemap/0.9"
    xmlns:sc   ="http://sw.deri.org/2007/07/sitemapextension/scschema.xsd"
    xmlns:rdf  ="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs ="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:bibo ="http://purl.org/ontology/bibo/"
    xmlns:dcterms ="http://purl.org/dc/terms/"
    xmlns:dcat ="http://www.w3.org/ns/dcat#"
    xmlns:void ="http://rdfs.org/ns/void#">

<!-- sitemapindex2void.xslt is identical to sitemap2void.xslt
     $Id$
 -->

<output indent="yes" method="xml" media-type="application/rdf+xml" encoding="UTF-8" omit-xml-declaration="no"/>

<template match="s:sitemapindex">
    <rdf:RDF xmlns:rdf ="http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
        <apply-templates select="s:sitemap" />
    </rdf:RDF>
</template>

<template match="s:sitemap">
    <cr:File>
        <attribute  name="rdf:about">
            <value-of select="normalize-space(s:loc)" />
        </attribute>
    </cr:File>
</template>

<template match="s:urlset">
    <rdf:RDF xmlns:rdf ="http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
        <apply-templates select="sc:dataset" />
        <apply-templates select="s:url" />
    </rdf:RDF>
</template>

<template match="s:url">
    <element name="bibo:Webpage">
        <!-- map sc:datasetURI to dataset -->
        <attribute  name="rdf:about">
            <value-of select="normalize-space(s:loc)" />
        </attribute>
        <!-- This means we'll harvest the page -->
        <!--
        <element name="rdf:type">
            <attribute  name="rdf:resource">http://cr.eionet.europa.eu/ontologies/contreg.rdf#File</attribute>
        </element>
        -->
    </element>
</template>

<template match="sc:dataset">
    <element name="dcat:Dataset">
        <!-- map sc:datasetURI to dataset -->
        <if test="normalize-space(sc:datasetURI) = ''">
            <attribute  name="rdf:ID">
                <value-of select="generate-id()"/>
            </attribute>
        </if>
        <if test="normalize-space(sc:datasetURI) != ''">
            <attribute  name="rdf:about">
                <value-of select="sc:datasetURI" />
            </attribute>
        </if>

        <!-- map sc:datasetLabel to rdfs:label -->
        <element name="rdfs:label">
                <value-of select="sc:datasetLabel" />
        </element>

        <!-- process sub-elements  -->
        <apply-templates select="sc:sampleURI" />
        <apply-templates select="sc:sparqlEndpointLocation" />
        <apply-templates select="sc:dataDumpLocation" />
        <apply-templates select="sc:linkedDataPrefix" />
   </element>
</template>


<!-- map sc:sampleURI to void:exampleResource -->
<template match="sc:sampleURI">
    <element name="void:exampleResource">
        <attribute name="rdf:resource">
            <value-of select="." />
        </attribute>
   </element>
</template>

<!-- map sc:sparqlEndpointLocation to void:sparqlEndpoint -->
<template match="sc:sparqlEndpointLocation">
    <element name="void:sparqlEndpoint">
        <attribute name="rdf:resource">
            <value-of select="." />
        </attribute>
   </element>
</template>

<!-- map sc:dataDumpLocation to void:dataDumpLocation -->
<template match="sc:dataDumpLocation">
    <element name="void:dataDumpLocation">
              <cr:File> <!-- Could also use http://purl.org/dc/dcmitype/Dataset -->
        <attribute name="rdf:about">
            <value-of select="." />
        </attribute>
              </cr:File>
   </element>
</template>


<!-- map sc:linkedDataPrefix to void:uriPattern -->
<template match="sc:linkedDataPrefix">
    <element name="void:uriRegexPattern">^<value-of select="." />$</element>
</template>



<!-- ignore the rest of the DOM -->
<template match="text()|@*|*"><apply-templates /></template>


</stylesheet>
