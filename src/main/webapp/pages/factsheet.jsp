<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">

		<c:choose>
			<c:when test="${!actionBean.noCriteria}">
			
				<div id="tabbedmenu">
				    <ul>
					<li id="currenttab"><span>Resource properties</span></li>
					<li>
						<c:choose>
							<c:when test="${not empty actionBean.uri}">
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="object" value="${actionBean.uri}"/>
								</stripes:link>
							</c:when>
							<c:when test="${actionBean.uriHash!=0}">
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="object" value="${actionBean.uriHash}"/>
								</stripes:link>
							</c:when>
							<c:otherwise>
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="object" value=""/>
								</stripes:link>
							</c:otherwise>
						</c:choose>
			        </li>
				    </ul>
				</div>
				<br style="clear:left" />
		
		        <c:choose>
				    <c:when test="${actionBean.subject!=null}">		    
								    
				    	<c:set var="subjectUrl" value="${actionBean.subject.url}"/>
				    	<c:set var="subjectUri" value="${actionBean.subject.uri}"/>
				    	<c:set var="allowEdit" value="false"/>
				    	
				    	<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
				    		<c:set var="displayOperations" value="true"/>
					    	<c:if test="${!subject.anonymous}">
		    					<c:set var="allowEdit" value="true"/>
		    				</c:if>
		    			</c:if>
		    			
				    	<c:if test="${displayOperations}">
				    		<ul id="dropdown-operations">
								<li><a href="#">Operations</a>
									<ul>
										<c:if test="${allowEdit}">
											<li>
												<stripes:link class="link-plain" href="/factsheet.action" event="${actionBean.context.eventName=='edit' ? 'view' : 'edit'}">${actionBean.context.eventName=='edit' ? 'View' : 'Edit'}
													<stripes:param name="uri" value="${subjectUri}"/>
												</stripes:link>
											</li>
										</c:if>
										<li>
											<stripes:link href="${actionBean.urlBinding}" event="harvest">Schedule harvest
													<stripes:link-param name="uri" value="${actionBean.uri}"/>
											</stripes:link>
										</li>
									</ul>
								</li>
							</ul>
						</c:if>
				
				    	<div style="margin-top:20px">
				    	
				    		<c:choose>
				    			<c:when test="${subjectUrl!=null}">
				    				<div><p>Resource URL: <a class="link-external" href="${fn:escapeXml(subjectUrl)}"><c:out value="${subjectUrl}"/></a></p></div>
				    			</c:when>
				    			<c:when test="${subjectUri!=null}">
				    				<div class="advice-msg" title="${fn:escapeXml(subjectUri)}">This is an unresolvable resource!</div>
				    			</c:when>
				    			<c:otherwise>
				    				<div class="advice-msg">This is an anonymous resource!</div>
				    			</c:otherwise>
				    		</c:choose>
				    		
				    		<stripes:form action="/factsheet.action" method="post">
				    		
						    	<c:if test="${actionBean.context.eventName=='edit' && allowEdit}">
					    				<table>
					    					<tr>
					    						<td><stripes:label for="propertySelect">Property:</stripes:label></td>
					    						<td>
					    							<stripes:select name="propertyUri" id="propertySelect">
									    				<c:forEach var="prop" items="${actionBean.addibleProperties}">
									    					<stripes:option value="${prop.uri}" label="${prop.label} (${prop.uri})"/>
											    		</c:forEach>
									    			</stripes:select>
					    						</td>
					    					</tr>
					    					<tr>
								    			<td><stripes:label for="propertyText">Value:</stripes:label></td>
								    			<td><stripes:textarea name="propertyValue" id="propertyText" cols="100" rows="2"/></td>
								    		</tr>
								    		<tr>
								    			<td>&nbsp;</td>
							    				<td>
							    					<stripes:submit name="save" value="Save" id="saveButton"/>
							    					<stripes:submit name="delete" value="Delete selected" id="deleteButton"/>
							    					<stripes:hidden name="uri" value="${subjectUri}"/>
													<stripes:hidden name="anonymous" value="${actionBean.subject.anonymous}"/>
							    				</td>
							    		</table>
						    	</c:if>
				    		
								<stripes:layout-render name="/pages/common/factsheet_table.jsp"
											subjectUrl="${subjectUrl}" subjectUri="${subjectUri}" displayCheckboxes="${actionBean.context.eventName=='edit' && allowEdit}"/>
		
				    		</stripes:form>
						</div>				    
				    </c:when>
				    <c:otherwise>
						<div style="margin-top:20px" class="note-msg">
				    		<c:choose>
				    			<c:when test="${not empty actionBean.uri}">
				    				<strong>Nothing is known about ${actionBean.uri}</strong>
				    			</c:when>
				    			<c:when test="${actionBean.uriHash!=0}">
				    				<strong>Nothing is known about resource with hash code ${actionBean.uriHash}</strong>
				    			</c:when>
				    			<c:otherwise>
				    				<strong>Resource identifier not specified!</strong>
				    			</c:otherwise>
				    		</c:choose>		    		
				    	</div>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<div>&nbsp;</div>
			</c:otherwise>
		</c:choose>
	    				
	</stripes:layout-component>
</stripes:layout-render>
