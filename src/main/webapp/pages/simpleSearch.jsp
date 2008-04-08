<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	
	<stripes:layout-component name="contents">
	
        <h1>Simple search</h1>
        
        <p>Here we should have some sort of text explaining how to formulate your search expression...</p>
	    
	    <stripes:form action="/simpleSearch.action" focus="searchExpression" style="padding-bottom:20px">
			
	    	<stripes:label for="expressionField">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionField" size="30"/>
	    	<stripes:submit name="doSimpleSearch" value="Search" id="searchButton"/>
	    	
	    </stripes:form>
	    
	    <c:import url="/pages/genericHits.jsp"/>
                     
	</stripes:layout-component>
</stripes:layout-render>
