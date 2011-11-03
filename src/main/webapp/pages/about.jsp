<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="About ${initParam.appDispName}">

    <stripes:layout-component name="contents">

        <h1>About ${initParam.appDispName}</h1>
        <p>${initParam.appDispName} is an object-oriented search engine
        where you can search in the content of Reportnet deliveries. Being
        object-oriented means it understands what e.g. a measuring
        station is and can show what measurements it has made. Not all
        of the Eionet services are included, only those that have been
        specified by the administrators of this site.</p>

    <p>
        There are currently ${actionBean.triplesCount} triples in the database.
    </p>

    <p>
    The ${initParam.appDispName} is an Open Source product.
    Learn more at <a href="http://svn.eionet.europa.eu/projects/Reportnet/wiki/ContentRegistry">http://svn.eionet.europa.eu/projects/Reportnet/wiki/ContentRegistry</a>.
    Get the <a href="http://svn.eionet.europa.eu/projects/Reportnet/wiki/CR3Design/InstallationGuide">installation guide</a>.
    </p>

    <h2>People</h2>
    <dl>
      <dt>Development team:</dt>
	<dd>Jaanus Heinlaid, TripleDev</dd>
	<dd>Risto Alt, TripleDev</dd>
	<dd>Enriko Käsper, TripleDev</dd>
	<dd>Kaido Laine, TripleDev</dd>
	<dd>Juhan Voolaid, TripleDev</dd>
      <dt>Contributors:</dt>
	<dd>Aleksandr Ivanov, Tieto Estonia</dd>    
	<dd>Jaak Kapten, Tieto Estonia</dd>    
      <dt>Architectural design:</dt>
	<dd>Søren Roug, European Environment Agency</dd>    
    </dl>
    </stripes:layout-component>
</stripes:layout-render>
