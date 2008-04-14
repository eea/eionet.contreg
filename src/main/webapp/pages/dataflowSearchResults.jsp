<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search results">

	<stripes:layout-component name="contents">
	
        <h1>Search results</h1>
        
        <p>
	        Maybe some text here Maybe some text here Maybe some text here Maybe some text here
	        maybe some text here Maybe some text here Maybe some text here Maybe some text here
        </p>
	    
	    <stripes:form action="/dataflowSearch.action" method="get" style="padding-bottom:20px">
			
		    <stripes:useActionBean beanclass="eionet.cr.web.action.DataflowSearchActionBean" id="dataflowSearchActionBean"/>
	                     
		    <display:table name="${dataflowSearchActionBean.resultList}" class="sortable" pagesize="20" sort="list" requestURI="/dataflowSearch.action">
				<c:forEach var="col" items="${dataflowSearchActionBean.columns}">
					<display:column property="${col.property}" title="${col.title}" sortable="${col.sortable}"/>
				</c:forEach>
			</display:table>

		</stripes:form>
				
	</stripes:layout-component>
</stripes:layout-render>
