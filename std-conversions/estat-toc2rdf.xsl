<?xml version="1.0" encoding="UTF-8"?>
<!-- This stylesheet converts the Eurostat table of content into RDF
     http://epp.eurostat.ec.europa.eu/NavTree_prod/everybody/BulkDownloadListing?file=table_of_contents.xml

     The ontology file could be placed at: http://epp.eurostat.ec.europa.eu/NavTree_prod/htdocs/ontologies/TableOfContent.owl
  -->
<xsl:stylesheet
        xmlns:nt="urn:eu.europa.ec.eurostat.navtree"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:owl="http://www.w3.org/2002/07/owl#"
        xmlns:dctype="http://purl.org/dc/dcmitype/"
        xmlns:dcterms="http://purl.org/dc/terms/"
        xmlns:skos="http://www.w3.org/2004/02/skos/core#"
        xmlns:cc="http://creativecommons.org/ns#"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:toc="http://rdfdata.eionet.europa.eu/eurostatdata/ontology/" version="1.0" exclude-result-prefixes="nt">

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <rdf:RDF xml:base="http://epp.eurostat.ec.europa.eu/NavTree_prod/everybody/BulkDownloadListing?file=table_of_contents.xml">
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="nt:tree">
    <foaf:Organization rdf:ID="Eurostat">
      <foaf:name>Eurostat</foaf:name>
      <foaf:homepage rdf:resource="http://epp.eurostat.ec.europa.eu/portal/page/portal/eurostat/home/"/>
      <owl:sameAs rdf:resource="http://dbpedia.org/resource/Eurostat"/>
      <xsl:for-each select="nt:branch">
        <toc:hasTOC>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </toc:hasTOC>
      </xsl:for-each>
    </foaf:Organization>

 <!-- Declaration of the formats that data is exported in -->
    <dcterms:MediaType rdf:ID="MT-sdmx">
      <rdfs:label>SDMX – Statistical Data and Metadata Exchange</rdfs:label>
      <rdfs:seeAlso rdf:resource="http://sdmx.org/"/>
      <owl:sameAs rdf:resource="http://dbpedia.org/resource/SDMX"/>
    </dcterms:MediaType>
    <dcterms:MediaType rdf:ID="MT-dft">
      <rdfs:label>DFT file</rdfs:label>
      <rdfs:comment>DFT files are intended to efficiently store data organised as multi-dimensional tables.</rdfs:comment>
    </dcterms:MediaType>
    <dcterms:MediaType rdf:ID="MT-tsv">
      <rdfs:label>TSV – Tab-separated values</rdfs:label>
      <rdfs:comment>‘TSV’ files are flat files that include a ‘tab delimited’ sequence of values in each line instead of
      one value per line/record.</rdfs:comment>
    </dcterms:MediaType>

    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="nt:children">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/nt:tree/nt:branch">
    <skos:ConceptScheme>
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="nt:code"/>
      </xsl:attribute>
      <xsl:apply-templates select="nt:title" mode="skos"/>
      <xsl:for-each select="nt:children/nt:branch">
        <skos:hasTopConcept>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </skos:hasTopConcept>
      </xsl:for-each>
    </skos:ConceptScheme>
    <xsl:apply-templates select="nt:children"/>
  </xsl:template>

  <xsl:template match="nt:branch">
    <skos:Concept>
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="nt:code"/>
      </xsl:attribute>
      <xsl:apply-templates select="nt:title" mode="skos"/>
      <xsl:for-each select="nt:children/nt:branch">
        <skos:narrower>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </skos:narrower>
      </xsl:for-each>
      <xsl:for-each select="nt:children/nt:leaf">
        <toc:hasDataset>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </toc:hasDataset>
      </xsl:for-each>
    </skos:Concept>
    <xsl:apply-templates select="nt:children"/>
  </xsl:template>

  <xsl:template match="nt:leaf[@type = 'dataset']">
    <dctype:Dataset>
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="nt:code"/>
      </xsl:attribute>
      <dcterms:creator rdf:resource="#Eurostat"/>
      <cc:license rdf:resource="http://creativecommons.org/licenses/by/3.0/lu/"/>
      <cc:morePermissions rdf:resource="http://ec.europa.eu/geninfo/copyright_en.htm"/>
      <dcterms:subject>
        <xsl:attribute name="rdf:resource">#<xsl:value-of select="../../nt:code"/>
        </xsl:attribute>
      </dcterms:subject>
      <toc:hasView><xsl:attribute name="rdf:resource">http://appsso.eurostat.ec.europa.eu/nui/show.do?lang=en&amp;dataset=<xsl:value-of select="nt:code"/></xsl:attribute></toc:hasView>
      <xsl:apply-templates/>
    </dctype:Dataset>
  </xsl:template>

  <xsl:template match="nt:leaf[@type = 'table']">
    <dctype:Dataset>
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="nt:code"/>
      </xsl:attribute>
      <dcterms:creator rdf:resource="#Eurostat"/>
      <cc:license rdf:resource="http://creativecommons.org/licenses/by/3.0/lu/"/>
      <cc:morePermissions rdf:resource="http://ec.europa.eu/geninfo/copyright_en.htm"/>
      <toc:hasView><xsl:attribute name="rdf:resource">http://epp.eurostat.ec.europa.eu/tgm/table.do?tab=table&amp;plugin=1&amp;language=en&amp;pcode=<xsl:value-of select="nt:code"/></xsl:attribute></toc:hasView>
      <xsl:apply-templates/>
    </dctype:Dataset>
  </xsl:template>

  <xsl:template match="nt:title">
    <dcterms:title>
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="@language"/>
      </xsl:attribute>
      <xsl:value-of select="."/>
    </dcterms:title>
  </xsl:template>

  <xsl:template match="nt:title" mode="skos">
    <skos:prefLabel>
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="@language"/>
      </xsl:attribute>
      <xsl:value-of select="."/>
    </skos:prefLabel>
  </xsl:template>

  <xsl:template match="nt:shortDescription">
    <xsl:if test="normalize-space(.) != ''">
      <dcterms:description>
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="@language"/>
        </xsl:attribute>
        <xsl:value-of select="."/>
      </dcterms:description>
    </xsl:if>
  </xsl:template>

  <xsl:template match="nt:lastUpdate">
    <dcterms:modified>
      <xsl:value-of select="."/>
    </dcterms:modified>
  </xsl:template>

  <xsl:template match="nt:downloadLink">
    <toc:downloadLink>  <!-- Could also use dcterms:hasFormat -->
      <dctype:Text>
        <xsl:attribute name="rdf:about">
          <xsl:value-of select="."/>
        </xsl:attribute>
        <rdfs:label>
        Download file in compressed <xsl:value-of select="@format"/> format
        </rdfs:label>
        <dcterms:format>
          <xsl:attribute name="rdf:resource">#MT-<xsl:value-of select="@format"/></xsl:attribute>
        </dcterms:format>
      </dctype:Text>
    </toc:downloadLink>
  </xsl:template>

  <xsl:template match="nt:metadata">
    <xsl:if test="normalize-space(.) != ''">
      <toc:metadata>
        <dctype:Text>
          <xsl:attribute name="rdf:about">
            <xsl:value-of select="."/>
          </xsl:attribute>
          <rdfs:label>
          Metadata as HTML web page
          </rdfs:label>
        </dctype:Text>
      </toc:metadata>
    </xsl:if>
  </xsl:template>

  <!-- XML elements that have no language code -->
  <xsl:template match="nt:code|nt:dataStart|nt:dataEnd|nt:values">
    <xsl:if test="normalize-space(.) != ''">
      <xsl:element name="{concat('toc:',local-name())}">
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <!-- XML elements that have language code -->
  <xsl:template match="nt:unit">
    <xsl:if test="normalize-space(.) != ''">
      <xsl:element name="{concat('toc:',local-name())}">
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="@language"/>
        </xsl:attribute>
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*"/>

</xsl:stylesheet>
