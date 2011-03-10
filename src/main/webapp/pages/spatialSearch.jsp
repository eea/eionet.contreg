<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">
    <stripes:layout-component name="contents">

        <h1>Spatial search</h1>
        <p>
        Search for all spatial objects having the coordinates you specify below.<br/>
        Coordinates can be any positive or negative number. For example -60.257, 0.15, 17.
        </p>

        <crfn:form action="/spatialSearch.action" method="get" style="padding-bottom:20px">

            <stripes:label for="txtLatS">Latitude (south,north):</stripes:label>
            <stripes:text name="latS" id="txtLatS" size="10"/> - <stripes:text name="latN" id="txtLatN" size="10"/>
            <br/><br/>
            <stripes:label for="txtLongW">Longitude (west,east):</stripes:label>
            <stripes:text name="longW" id="txtLongW" size="10"/> - <stripes:text name="longE" id="txtLongE" size="10"/>
            <br/><br/>
            <stripes:submit name="search" value="Search" id="searchButton"/>

        </crfn:form>

        <c:if test="${not empty param.search}">
            <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
