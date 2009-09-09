<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">

		<c:choose>
			<c:when test="${!actionBean.noCriteria}">
				
				<div id="tabbedmenu">
				    <ul>
						<li>
							<c:choose>
								<c:when test="${not empty actionBean.uri}">
									<stripes:link href="/factsheet.action">Resource properties
										<stripes:param name="uri" value="${actionBean.uri}"/>			
									</stripes:link>
								</c:when>
								<c:otherwise>
									<stripes:link href="/factsheet.action">Resource properties
										<stripes:param name="uriHash" value="${actionBean.anonHash}"/>
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
							<c:when test="${actionBean.anonHash!=0}">
								<stripes:link href="/factsheet.action">this anonymous resource
									<stripes:param name="uriHash" value="${actionBean.anonHash}"/>
								</stripes:link>
							</c:when>
							<c:when test="${actionBean.uriResolvable}">
								<stripes:link href="/factsheet.action">${actionBean.uri}
									<stripes:param name="uri" value="${actionBean.uri}"/>			
								</stripes:link>
							</c:when>
							<c:otherwise>
								<stripes:link href="/factsheet.action">this unresolvable resource
									<stripes:param name="uri" value="${actionBean.uri}"/>			
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
