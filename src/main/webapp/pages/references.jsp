<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">

		<c:choose>
			<c:when test="${not empty actionBean.searchExpression}">
				
				<div id="tabbedmenu">
				    <ul>
						<li>
							<c:choose>
								<c:when test="${actionBean.searchExpression.uri}">
									<stripes:link href="/factsheet.action">Resource properties
										<stripes:param name="uri" value="${actionBean.object}"/>			
									</stripes:link>
								</c:when>
								<c:otherwise>
									<stripes:link href="/factsheet.action">Resource properties
										<stripes:param name="uriHash" value="${actionBean.object}"/>
									</stripes:link>
								</c:otherwise>
							</c:choose>
				        </li>
						<li id="currenttab">
							<span>Resource references</span>
						</li>
				    </ul>
				</div>
				
				<div>	
					<p>
						<br/><br/>
						Below is the list of references to
						<c:choose>
							<c:when test="${actionBean.searchExpression.uri}">
								<stripes:link href="/factsheet.action">${actionBean.object}
									<stripes:param name="uri" value="${actionBean.object}"/>			
								</stripes:link>
							</c:when>
							<c:otherwise>
								resource with hash code&nbsp;<stripes:link href="/factsheet.action">${actionBean.object}
									<stripes:param name="uriHash" value="${actionBean.object}"/>
								</stripes:link>
							</c:otherwise>
						</c:choose>
					</p>
				</div>
				
				<div style="padding-top:20px;">
					<c:if test="${param.search!=null}">			
						<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
					</c:if>
				</div>
	
			</c:when>
			<c:otherwise>
				<div>&nbsp;</div>
			</c:otherwise>
		</c:choose>
		
	</stripes:layout-component>
</stripes:layout-render>
