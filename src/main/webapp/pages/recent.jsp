<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Recent additions">

	<stripes:layout-component name="contents">
	
        <h1>Recent additions</h1>
        
        <p>This page displayes the 20 most recently added recources of the type denoted by the selected tab.<br/>
		If less than 20 are displayed, it means the addition time is known only for the resources displayed.<br/>
		If none are displayed, it means addition time is known for none.</p>
		
		<stripes:useActionBean beanclass="eionet.cr.web.action.RecentAdditionsActionBean" id="recentAdditionsActionBean"/>
	    
	    <div id="tabbedmenu">
		    <ul>
		    	<c:forEach items="${recentAdditionsActionBean.typeTitles}" var="title" varStatus="loop">
					<li><a href="recent.jsp?type=<c:out value='${title.key}'/>"><c:out value="${title.value}"/></a></li>
				</c:forEach>
		    </ul>
		</div>
		
	</stripes:layout-component>
</stripes:layout-render>
