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
								<crfn:form id="bookmarkForm" action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/bookmark?deletebookmarks=1" method="post">
									<display:table name="${actionBean.bookmarks}" class="sortable" pagesize="20" sort="list" id="bookmark" htmlId="bookmarks" requestURI="${actionBean.urlBinding}" style="width:100%">
										<display:column title="" sortable="false"  style="width:50px;">
											<input type="checkbox" value="${ bookmark.bookmarkUrlHtmlFormatted }"  name='bookmarkUrl'></input>  
										</display:column>
										<display:column title="URL" sortable="false">
											<stripes:link href="/factsheet.action">${bookmark.bookmarkUrl}
												<stripes:param name="uri" value="${bookmark.bookmarkUrl}" />										
											</stripes:link>
										</display:column>
									</display:table>
									<div>
										<stripes:submit name="delete" value="Delete Bookmarks" title="Delete seleceted bookmarks"/>
										<input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkForm');return false"/>
									</div>
								</crfn:form>
							</c:when>
							<c:otherwise>
								<p>No bookmarks found.</p>
							</c:otherwise>
						</c:choose>
					</c:if>
					
					<c:if test="${actionBean.sectionHistory}">
						<c:choose>
							<c:when test="${not empty actionBean.history}">
						
								<display:table name="${actionBean.history}" class="sortable" pagesize="20" sort="list" id="history" htmlId="historylist" requestURI="${actionBean.urlBinding}" style="width:100%">
									<display:column title="Date" sortable="false" style="width:150px;">${history.lastOperation}
									</display:column>
									<display:column title="URL" sortable="false">
										<stripes:link href="/factsheet.action">${history.url}
											<stripes:param name="uri" value="${history.url}" />										
										</stripes:link>
									</display:column>
								</display:table>
							</c:when>
							<c:otherwise>
								<p>No history found.</p>
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
