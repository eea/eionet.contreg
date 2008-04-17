<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Recent additions">

	<stripes:layout-component name="contents">
	
        <h1>Recent additions</h1>
        
        <p>This page displayes the 20 most recently added recources of the type denoted by the selected tab.<br/>
		If less than 20 are displayed, it means the addition time is known only for the resources displayed.<br/>
		If none are displayed, it means addition time is known for none.</p>
		
	    <div id="tabbedmenu">
		    <ul>
		    	<c:forEach items="${actionBean.typeTitles}" var="title" varStatus="loop">
					<c:choose>
				  		<c:when test="${actionBean.type == title.key}" > 
							<li id="currenttab"><span>${title.value}</span></li>
						</c:when>
						<c:otherwise> 	
							<li>
								<stripes:link href="/recentAdditions.action">
									${title.value}
					                <stripes:param name="type" value="${title.key}"/>
					            </stripes:link>
				            </li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
		    </ul>
		</div>
		
	</stripes:layout-component>
</stripes:layout-render>
