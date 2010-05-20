<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:xr="http://w3future.com/xr/ns/"
xmlns:atom="http://www.w3.org/2005/Atom"
xmlns:foaf="http://xmlns.com/foaf/0.1/"
xmlns:dcterms="http://purl.org/dc/terms/"
xmlns:dc="http://purl.org/dc/elements/1.1/"
xmlns:admin="http://webns.net/mvcb/" version="1.0">

  <xsl:output indent="yes"/>

  <xsl:template match="atom:feed">
    <rdf:RDF>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:copy-of select="@xml:base"/>
<!--
      <xsl:if test="@xml:base != ''">
        <xsl:attribute name="xml:base">
          <xsl:value-of select="@xml:base"/>
        </xsl:attribute>
      </xsl:if>
-->
        <atom:Feed>
          <xsl:call-template name="uri">
            <xsl:with-param name="uri">
              <xsl:value-of select="atom:id"/>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:for-each select="atom:title">
            <dc:title>
              <xsl:apply-templates select="." mode="object"/>
            </dc:title>
          </xsl:for-each>
          <xsl:for-each select="atom:link">
            <atom:link>
              <rdfs:Resource>
                <xsl:call-template name="uri">
                  <xsl:with-param name="uri">
                    <xsl:value-of select="."/>
                  </xsl:with-param>
                </xsl:call-template>
              </rdfs:Resource>
            </atom:link>
          </xsl:for-each>
          <xsl:for-each select="atom:modified">
            <dcterms:modified>
              <xsl:apply-templates select="." mode="object"/>
            </dcterms:modified>
          </xsl:for-each>
          <xsl:for-each select="tagline">
            <dc:description>
              <xsl:apply-templates select="." mode="object"/>
            </dc:description>
          </xsl:for-each>
          <xsl:for-each select="atom:generator">
            <admin:generatorAgent>
              <rdfs:Resource>
                <xsl:call-template name="uri">
                  <xsl:with-param name="uri">
                    <xsl:value-of select="."/>
                  </xsl:with-param>
                </xsl:call-template>
                <xsl:for-each select="@name">
                  <dc:title>
                    <xsl:apply-templates select="." mode="object"/>
                  </dc:title>
                </xsl:for-each>
              </rdfs:Resource>
            </admin:generatorAgent>
          </xsl:for-each>
          <xsl:for-each select="atom:copyright">
            <dc:rights>
              <xsl:apply-templates select="." mode="object"/>
            </dc:rights>
          </xsl:for-each>
          <atom:entries rdf:parseType="Collection">
            <xsl:for-each select="atom:entry">
              <atom:Entry>
                <xsl:call-template name="uri">
                  <xsl:with-param name="uri">
                    <xsl:value-of select="atom:id"/>
                  </xsl:with-param>
                </xsl:call-template>
                <xsl:for-each select="atom:title">
                  <dc:title>
                    <xsl:apply-templates select="." mode="object"/>
                  </dc:title>
                </xsl:for-each>
                <xsl:for-each select="atom:link">
                  <atom:link>
                    <rdfs:Resource>
                      <xsl:call-template name="uri">
                        <xsl:with-param name="uri">
                          <xsl:value-of select="."/>
                        </xsl:with-param>
                      </xsl:call-template>
                    </rdfs:Resource>
                  </atom:link>
                </xsl:for-each>
                <xsl:for-each select="atom:issued">
                  <dcterms:issued>
                    <xsl:apply-templates select="." mode="object"/>
                  </dcterms:issued>
                </xsl:for-each>
                <xsl:for-each select="atom:modified">
                  <dcterms:modified>
                    <xsl:apply-templates select="." mode="object"/>
                  </dcterms:modified>
                </xsl:for-each>
                <xsl:for-each select="atom:created">
                  <dcterms:created>
                    <xsl:apply-templates select="." mode="object"/>
                  </dcterms:created>
                </xsl:for-each>
                <xsl:for-each select="atom:summary">
                  <dc:description>
                    <xsl:apply-templates select="." mode="object"/>
                  </dc:description>
                </xsl:for-each>
                <xsl:for-each select="ancestor-or-self::*[atom:author][1]/atom:author">
                  <foaf:maker>
                    <foaf:Person>
                      <xsl:for-each select="atom:name">
                        <foaf:name>
                          <xsl:apply-templates select="." mode="object"/>
                        </foaf:name>
                      </xsl:for-each>
                      <xsl:for-each select="atom:url">
                        <foaf:weblog>
                          <rdfs:Resource>
                            <xsl:call-template name="uri">
                              <xsl:with-param name="uri">
                                <xsl:value-of select="."/>
                              </xsl:with-param>
                            </xsl:call-template>
                          </rdfs:Resource>
                        </foaf:weblog>
                      </xsl:for-each>
                      <xsl:for-each select="atom:email">
                        <foaf:mbox>
                          <rdfs:Resource>
                            <xsl:call-template name="uri">
                              <xsl:with-param name="uri">
                                <xsl:value-of select="."/>
                              </xsl:with-param>
                            </xsl:call-template>
                          </rdfs:Resource>
                        </foaf:mbox>
                      </xsl:for-each>
                    </foaf:Person>
                  </foaf:maker>
                </xsl:for-each>
                <atom:contributors rdf:parseType="Collection">
                  <xsl:for-each select="atom:contributor">
                    <foaf:Person>
                      <xsl:for-each select="atom:name">
                        <foaf:name>
                          <xsl:apply-templates select="." mode="object"/>
                        </foaf:name>
                      </xsl:for-each>
                      <xsl:for-each select="atom:url">
                        <foaf:weblog>
                          <rdfs:Resource>
                            <xsl:call-template name="uri">
                              <xsl:with-param name="uri">
                                <xsl:value-of select="."/>
                              </xsl:with-param>
                            </xsl:call-template>
                          </rdfs:Resource>
                        </foaf:weblog>
                      </xsl:for-each>
                      <xsl:for-each select="atom:email">
                        <foaf:mbox>
                          <rdfs:Resource>
                            <xsl:call-template name="uri">
                              <xsl:with-param name="uri">
                                <xsl:value-of select="."/>
                              </xsl:with-param>
                            </xsl:call-template>
                          </rdfs:Resource>
                        </foaf:mbox>
                      </xsl:for-each>
                    </foaf:Person>
                  </xsl:for-each>
                </atom:contributors>
                <xsl:for-each select="atom:content">
                  <atom:content>
                    <rdfs:Resource>
                      <xsl:for-each select="@type">
                        <atom:type>
                          <xsl:apply-templates select="." mode="object"/>
                        </atom:type>
                      </xsl:for-each>
                      <xsl:for-each select="@mode[.='xml']">
                        <atom:encoding>
                          <rdfs:Resource>
                            <xsl:call-template name="uri">
                              <xsl:with-param name="uri">http://purl.org/atom/ns#xml</xsl:with-param>
                            </xsl:call-template>
                          </rdfs:Resource>
                        </atom:encoding>
                      </xsl:for-each>
                      <xsl:for-each select=".">
                        <rdf:value>
                          <xsl:apply-templates select="." mode="object"/>
                        </rdf:value>
                      </xsl:for-each>
                    </rdfs:Resource>
                  </atom:content>
                </xsl:for-each>
              </atom:Entry>
            </xsl:for-each>
          </atom:entries>
        </atom:Feed>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="/atom:feed" mode="object">
    <atom:Feed>
      <xsl:call-template name="uri">
        <xsl:with-param name="uri">
          <xsl:value-of select="atom:id"/>
        </xsl:with-param>
      </xsl:call-template>
    </atom:Feed>
  </xsl:template>

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

</xsl:stylesheet>
