<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Dataflow search">

	<stripes:useActionBean beanclass="eionet.cr.web.action.DataflowSearchActionBean" id="dataflowSearchActionBean"/>
	
	<stripes:layout-component name="contents">
	
        <h1>Dataflow search</h1>
        
        <div style="margin-top:15px">
		    <crfn:form action="/dataflowSearch.action" method="get">
				
		    	<label for="dataflowSelect" style="font-weight:bold;margin-bottom:3px">Dataflow:</label>
		    	<stripes:select name="dataflow" id="dataflowSelect" size="9" style="width:100%">
		    		<c:forEach var="instr" items="${dataflowSearchActionBean.instrumentsObligations}">
		    			<optgroup label="${instr.key}">
		    				<c:forEach var="oblig" items="${instr.value}">
		    					<stripes:option value="${oblig.uri}" label="${oblig.label}"/>
		    				</c:forEach>		
		    			</optgroup>
		    		</c:forEach>
		    	</stripes:select>
				<br/>
				<br/>
				
				<label for="localitySelect" style="font-weight:bold">Locality:</label>
		    	<stripes:select name="locality" id="localitySelect" style="max-width:200px">
		    		<stripes:option value="" label="-- All --"/>
		    		<c:forEach var="loclty" items="${dataflowSearchActionBean.localities}">
		    			<stripes:option value="${crfn:addQuotesIfWhitespaceInside(loclty)}" label="${loclty}"/>
		    		</c:forEach>
		    	</stripes:select>
		    	<label for="yearSelect" style="font-weight:bold;display:inline;margin-left:20px">Delivery year:</label>
		    	<stripes:select name="year" id="yearSelect">
		    		<stripes:option value="" label="-- All --"/>
		    		<c:forEach var="y" items="${dataflowSearchActionBean.years}">
		    			<stripes:option value="${y}" label="${y}"/>
		    		</c:forEach>
		    	</stripes:select><stripes:submit name="search" value="Search" id="searchButton" style="display:inline;margin-left:60px"/>
		    	
		    </crfn:form>
		</div>			  
		
		<c:if test="${not empty param.search}">
    		<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
    	</c:if>
				    	
	</stripes:layout-component>
</stripes:layout-render>
