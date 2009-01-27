<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">
	
        <h1>Simple search</h1>
        
        <p>
        	This page enables you to find content by case-insensitive text in any metadata element. For example:<br/>
        	to search for content that contains words "air" or "soil" or both, enter <span class="searchExprSample">air soil</span>.
        	Entering <span class="searchExprSample">"air pollution"</span><br/> will search for the exact phrase "air pollution".
        </p>
	    
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
