<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	
	<stripes:layout-component name="contents">
	
        <h1>Simple search</h1>
        
        <p>Here we should have some sort of text explaining how to formulate your search expression...</p>
	    
	    <stripes:form action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
			
	    	<stripes:label for="expressionField">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionField" size="30"/>
	    	<stripes:submit name="doSimpleSearch" value="Search" id="searchButton"/>
	    	
	    </stripes:form>
	    
	    <display:table name="${hits}" class="sortable" pagesize="20" sort="list" requestURI="/simpleSearch.action">
			<c:forEach var="col" items="${collist}">
				<display:column property="${col.property}" title="${col.title}" sortable="${col.sortable}"/>
			</c:forEach>
		</display:table>


	    <%	    
	    //<c:import url="/pages/genericHits.jsp"/>
	    %>
                     
	</stripes:layout-component>
</stripes:layout-render>
