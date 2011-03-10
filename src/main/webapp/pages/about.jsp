<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="About Content Registry">

    <stripes:layout-component name="contents">

        <h1>About Content Registry</h1>
        <p>Content Registry (CR) is an object-oriented search engine where you can search for the content of data in Eionet. Being object-oriented means it understands what e.g. a measuring station is and can show what measurements it has made. Not all of the Eionet services are included, only those that have been specified by the administrators of this site.</p>
        <b>There are currently ${actionBean.triplesCount} triples in the database.</b>

    </stripes:layout-component>
</stripes:layout-render>
