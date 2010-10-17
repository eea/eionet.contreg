<?xml version="1.0" encoding="UTF-8"?>
<!-- This stylesheet converts the Eurostat table of content into RDF
     http://epp.eurostat.ec.europa.eu/NavTree_prod/everybody/BulkDownloadListing?file=table_of_contents.xml
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
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:prop="http://rdfdata.eionet.europa.eu/eurostatdata/ontology/" version="1.0" exclude-result-prefixes="nt">

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
        <prop:hasTOC>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </prop:hasTOC>
      </xsl:for-each>
    </foaf:Organization>
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
        <prop:hasDataset>
          <xsl:attribute name="rdf:resource">#<xsl:value-of select="nt:code"/></xsl:attribute>
        </prop:hasDataset>
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
      <dcterms:subject>
        <xsl:attribute name="rdf:resource">#<xsl:value-of select="../../nt:code"/>
        </xsl:attribute>
      </dcterms:subject>
      <prop:hasView><xsl:attribute name="rdf:resource">http://appsso.eurostat.ec.europa.eu/nui/show.do?lang=en&amp;dataset=<xsl:value-of select="nt:code"/></xsl:attribute></prop:hasView>
      <xsl:apply-templates/>
    </dctype:Dataset>
  </xsl:template>

  <xsl:template match="nt:leaf[@type = 'table']">
    <dctype:Dataset>
      <xsl:attribute name="rdf:ID">
        <xsl:value-of select="nt:code"/>
      </xsl:attribute>
      <dcterms:creator rdf:resource="#Eurostat"/>
      <prop:hasView><xsl:attribute name="rdf:resource">http://epp.eurostat.ec.europa.eu/tgm/table.do?tab=table&amp;plugin=1&amp;language=en&amp;pcode=<xsl:value-of select="nt:code"/></xsl:attribute></prop:hasView>
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
    <prop:downloadLink>
      <dctype:Text>
        <xsl:attribute name="rdf:about">
          <xsl:value-of select="."/>
        </xsl:attribute>
        <rdfs:label>
        Download as <xsl:value-of select="@format"/>
        </rdfs:label>
      </dctype:Text>
    </prop:downloadLink>
  </xsl:template>

  <xsl:template match="nt:metadata">
    <xsl:if test="normalize-space(.) != ''">
      <prop:metadata>
        <dctype:Text>
          <xsl:attribute name="rdf:about">
            <xsl:value-of select="."/>
          </xsl:attribute>
          <rdfs:label>
          Metadata as HTML web page
          </rdfs:label>
        </dctype:Text>
      </prop:metadata>
    </xsl:if>
  </xsl:template>

  <xsl:template match="nt:code|nt:dataStart|nt:dataEnd|nt:values">
    <xsl:if test="normalize-space(.) != ''">
      <xsl:element name="{concat('prop:',local-name())}">
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="nt:unit">
    <xsl:if test="normalize-space(.) != ''">
      <xsl:element name="{concat('prop:',local-name())}">
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="@language"/>
        </xsl:attribute>
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*"/>

</xsl:stylesheet>
