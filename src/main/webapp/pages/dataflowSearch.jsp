<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Dataflow search">

	<stripes:useActionBean beanclass="eionet.cr.web.action.DataflowSearchActionBean" id="dataflowSearchActionBean"/>
	
	<stripes:layout-component name="contents">
	
        <h1>Dataflow search</h1>
        
        <p>Here we could have some sort of explanatory text ...</p>
	    
	    <stripes:form action="/dataflowSearch.action" method="get">
			
	    	<label for="dataflowSelect" style="font-weight:bold;margin-bottom:3px">Dataflow:</label>
	    	<stripes:select name="dataflow" id="dataflowSelect" size="14" style="width:100%">
	    		<c:forEach var="instr" items="${dataflowSearchActionBean.instrumentsObligations}">
	    			<optgroup label="${instr.label}">
	    				<c:forEach var="oblig" items="${instr.obligations}">
	    					<stripes:option value="${oblig.id}" label="${oblig.label}"/>
	    				</c:forEach>		
	    			</optgroup>
	    		</c:forEach>
	    	</stripes:select>
			<br/>
			<br/>
			
			<label for="localitySelect" style="font-weight:bold">Locality:</label>
	    	<stripes:select name="locality" id="localitySelect">
	    		<stripes:option value="" label="-- All --"/>
	    		<c:forEach var="loclty" items="${dataflowSearchActionBean.localities}">
	    			<stripes:option value="${loclty}" label="${loclty}"/>
	    		</c:forEach>
	    	</stripes:select>&nbsp;&nbsp;&nbsp;<label for="yearSelect" style="font-weight:bold">Delivery year:</label>
	    	<stripes:select name="year" id="yearSelect">
	    		<stripes:option value="" label="-- All --"/>
	    		<c:forEach var="y" items="${dataflowSearchActionBean.years}">
	    			<stripes:option value="${y}" label="${y}"/>
	    		</c:forEach>
	    	</stripes:select>
	    	<br/>
	    	<br/>
	    	<stripes:submit name="search" value="Search" id="searchButton"/>
	    	
	    </stripes:form>
		
	</stripes:layout-component>
</stripes:layout-render>
