<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Custom search">

	<stripes:layout-component name="contents">
	
        <h1>Custom search</h1>        
        <p>
        	sdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf<br/>
        	sdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf
        </p>
	    <c:choose>
		    <c:when test="${actionBean.unselectedFilters!=null && fn:length(actionBean.unselectedFilters)>0}">
		    	<div id="filterSelectionArea" style="margin-top:20px">
		    		<stripes:form action="/customSearch.action" method="get">
		    		
		    			<stripes:select name="filter" id="filterSelect">
		    				<stripes:option value="" label=""/>
		    				<c:forEach var="unselectedFilter" items="${actionBean.unselectedFilters}">
		    					<stripes:option value="${unselectedFilter.uri}" label="${unselectedFilter.title}" title="${unselectedFilter.uri}"/>
		    				</c:forEach>
		    			</stripes:select>&nbsp;
		    			<stripes:submit name="addFilter" value="Add filter"/>
		    			
		    			<ul style="margin-top:20px">
		    				<c:forEach var="selectedFilter" items="${actionBean.selectedFilters}">
		    					<li>${selectedFilter}</li>
		    				</c:forEach>
		    			</ul>
		    		</stripes:form>
			    </div>				    
		    </c:when>
		    <c:otherwise>
				<p>No filters found!</p> 
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>
