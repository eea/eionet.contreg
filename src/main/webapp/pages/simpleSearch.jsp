<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">
	
        <h1>Simple search</h1>
        
        <p>Here we should have some sort of text explaining how to formulate your search expression...</p>
	    
	    <stripes:form action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
			
	    	<stripes:label for="expressionText">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionText" size="30"/>
	    	<stripes:submit name="search" value="Search" id="searchButton"/>
	    	
	    	<c:if test="${not empty param.search}">
	    		<stripes:layout-render name="/pages/common/resourcesResultList.jsp" tableClass="sortable"/>
	    	</c:if>
	    	
	    </stripes:form>
		
	</stripes:layout-component>
</stripes:layout-render>
