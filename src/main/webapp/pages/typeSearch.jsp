
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

				<stripes:select name="type">
					<c:forEach var="picklistItem" items="${actionBean.picklist}">
    					<stripes:option value="${picklistItem.uri}" label="${picklistItem.label} (${picklistItem.uri})"/>
    				</c:forEach>
				</stripes:select>&nbsp;
				<stripes:submit name="search" value="Search"/>
				<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
					&nbsp;<stripes:submit name="introspect" value="Introspect"/>
				</c:if>		

				<c:if test="${not empty param.search}">
					<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
				</c:if>

			</stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
