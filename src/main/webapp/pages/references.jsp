<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">

		<h1>References</h1>
		<p>
		Below is the list of all references to
		<c:if test="${not empty actionBean.searchExpression}">
			<c:choose>
				<c:when test="${actionBean.searchExpression.uri}">
					<stripes:link href="/factsheet.action">${actionBean.object}
						<stripes:param name="uri" value="${actionBean.object}"/>			
					</stripes:link>
				</c:when>
				<c:when test="${actionBean.searchExpression.hash}">
					hash code&nbsp;<stripes:link href="/factsheet.action">${actionBean.object}
						<stripes:param name="uriHash" value="${actionBean.object}"/>
					</stripes:link>
				</c:when>
				<c:otherwise>
					<em><c:out value="${actionBean.object}"/></em>
				</c:otherwise>
			</c:choose>
		</c:if>
		</p>
	
		<c:if test="${param.search!=null}">			
			<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
		</c:if>
		
	</stripes:layout-component>
</stripes:layout-render>
