<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Query">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	
	<stripes:layout-component name="contents">
	
		<h1>Perform Lucene query</h1>
		
		<stripes:form action="/luceneQuery.action" focus="query" style="padding-bottom:20px">
		
			<label for="query">Enter your Lucene query here:</label>
			<br/>
			<br/>
			<stripes:textarea name="query" id="query" cols="80" rows="4"/>
			<br/>
			
			<stripes:select name="analyzer" id="analyzerSelect" value="${actionBean.analyzer}">
	    		<c:forEach var="anl" items="${actionBean.analyzers}">
	    			<stripes:option value="${anl}" label="${anl}"/>
	    		</c:forEach>
	    	</stripes:select>
			
			<br/>
			<br/>
			<stripes:submit name="search" value="Query"/>
		</stripes:form>
		
		<c:if test="${not empty param.search}">
			<c:import url="/pages/queryHits.jsp"/>
		</c:if>
		
	</stripes:layout-component>
</stripes:layout-render>
