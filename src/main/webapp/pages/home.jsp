<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Home">

	<stripes:layout-component name="contents">
		<c:choose>
			<c:when test="${actionBean.userAuthorized}" >
		        <div id="tabbedmenu">
				    <ul>
				    	<c:forEach items="${actionBean.tabs}" var="tab">
							<c:choose>
						  		<c:when test="${actionBean.section == tab.tabType}" > 
									<li id="currenttab"><span><c:out value="${tab.title}"/></span></li>
								</c:when>
								<c:otherwise>
									<li>
										<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/${tab.tabType}">
											<c:out value="${tab.title}"/>
							            </stripes:link>
						            </li>
								</c:otherwise>
							</c:choose>
						</c:forEach>
				    </ul>
				</div>
				
				<br style="clear:left" />
				
				<div style="margin-top:20px">
					<c:if test="${actionBean.sectionBookmarks}">
						<c:choose>
							<c:when test="${not empty actionBean.bookmarks}">
						
								<display:table name="${actionBean.bookmarks}" class="sortable" pagesize="20" sort="list" id="bookmark" htmlId="bookmarks" requestURI="${actionBean.urlBinding}" style="width:100%">
									<display:column title="URL" sortable="false">
										<stripes:link href="/factsheet.action?uri=${bookmark.bookmarkUrl}">${bookmark.bookmarkUrl}</stripes:link>
									</display:column>
								</display:table>
							</c:when>
							<c:otherwise>
								<p>No bookmarks found for this user.</p>
							</c:otherwise>
						</c:choose>
					</c:if>
					<c:if test="${actionBean.sectionWorkspace}">
						<h1>User Home</h1>
		        		<p>This page is your home, ${ actionBean.attemptedUserName }</p>
					</c:if>
		
		
				</div> 
			</c:when>
			<c:otherwise>
					<div class="error-msg">
					${actionBean.authenticationMessage}
					</div>
			</c:otherwise>
		</c:choose>
	</stripes:layout-component>
</stripes:layout-render>
