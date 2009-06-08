<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">
	<stripes:layout-component name="contents">

		<h1>Spatial search</h1>
		<p>
		Search for all spatial objects having the coordinates you specify below. 
		</p>
	
		<stripes:form action="/spatialSearch.action" method="get" style="padding-bottom:20px">

			<stripes:label for="txtLat1">Latitude:&nbsp;&nbsp;&nbsp;</stripes:label>
			<stripes:text name="lat1" id="txtLat1" size="10"/> - <stripes:text name="lat2" id="txtLat2" size="10"/>
			<br/><br/>
			<stripes:label for="txtLong1">Longitude:</stripes:label>
			<stripes:text name="long1" id="txtLong1" size="10"/> - <stripes:text name="long2" id="txtLong2" size="10"/>			
			<br/><br/>
			<stripes:submit name="search" value="Search" id="searchButton"/>
			
		</stripes:form>
		
		<c:if test="${not empty param.search}">
			<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
		</c:if>
	
	</stripes:layout-component>
</stripes:layout-render>
