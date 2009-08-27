<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Type search">

	<stripes:layout-component name="contents">

       <h1>Type search</h1>
       <p>
       	This page enables to find content by type. Below is the list of types known to CR.
       	This does not mean that CR has content for every listed type.<br/>
       	The search will return the list of all resources having the type you selected.
       	To view a resource's factsheet, click the relevant action icon next to it.   
       </p>

	    <stripes:form action="/typeSearch.action" method="get" id="typesearch">

				<stripes:select name="type" onchange="hideElement('searchColumns')">
					<c:forEach var="picklistItem" items="${actionBean.availableTypes}">
    					<stripes:option value="${picklistItem.id}" label="${picklistItem.value} (${picklistItem.id})"/>
    				</c:forEach>
				</stripes:select>&nbsp;
				<stripes:submit name="search" value="Search"/>
		</stripes:form>
		<stripes:form action="/typeSearch.action" id="typesearch">
		<stripes:hidden name="type" value="${actionBean.type }"/>				
				<c:if test="${!empty actionBean.availableColumns}">
					<div id="searchColumns">
						<label for="searchColumnsSelect"> Choose which columns to display in a search result</label> <br/>
						<stripes:select 
								name="selectedColumns" 
								multiple="multiple" 
								size="5" 
								style="max-width:300px; margin-top:5px" 
								id="searchColumnsSelect" value="${actionBean.selectedColumns}" >
							<c:forEach items="${actionBean.availableColumns}" var="typeColumn">
								<stripes:option value="${typeColumn.id}" label="${typeColumn.value }"/>
							</c:forEach>
						</stripes:select>
						<stripes:submit name="setSearchColumns" value="Set search columns" id="searchColumnsButton"/>
					</div>
				</c:if>
				<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
					&nbsp;<stripes:submit name="introspect" value="Introspect"/>
				</c:if>		

				<c:if test="${not empty param.search or not empty param.setSearchColumns}">
					<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
				</c:if>

			</stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
