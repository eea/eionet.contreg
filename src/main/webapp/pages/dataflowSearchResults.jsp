<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search results">

	<stripes:layout-component name="contents">
	
        <h1>Search results</h1>
        
        <c:if test="${param.reload==null}">
	        <p>
	        	CR returns no more than 300 most relevant search results ordered descendingly by relevance.
	        	If you can't find what you're looking for, please narrow your search.
	        </p>
	    </c:if>
	    
	    <stripes:form action="/dataflowSearch.action" method="get" style="padding-bottom:20px">	
			<stripes:layout-render name="/pages/common/resourcesResultList.jsp" tableClass="sortable"/>
		</stripes:form>
				
	</stripes:layout-component>
</stripes:layout-render>
