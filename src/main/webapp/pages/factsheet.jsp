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
							<c:when test="${not empty actionBean.subject && not empty actionBean.subject.uri && !actionBean.subject.anonymous}">
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="uri" value="${actionBean.subject.uri}"/>
								</stripes:link>
							</c:when>
							<c:when test="${not empty actionBean.uri}">
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="uri" value="${actionBean.uri}"/>
								</stripes:link>
							</c:when>
							<c:otherwise>
								<stripes:link href="/references.action" event="search">Resource references
									<stripes:param name="anonHash" value="${actionBean.uriHash}"/>
								</stripes:link>
							</c:otherwise>
						</c:choose>
			        </li>
			        <li>
						<c:choose>
							<c:when test="${not empty actionBean.subject && not empty actionBean.subject.uri && !actionBean.subject.anonymous}">
								<stripes:link href="/objectsInSource.action" event="search">Objects in Source
									<stripes:param name="uri" value="${actionBean.subject.uri}"/>
								</stripes:link>
							</c:when>
							<c:when test="${not empty actionBean.uri}">
								<stripes:link href="/objectsInSource.action" event="search">Objects in Source
									<stripes:param name="uri" value="${actionBean.uri}"/>
								</stripes:link>
							</c:when>
							<c:otherwise>
								<stripes:link href="/objectsInSource.action" event="search">Objects in Source
									<stripes:param name="anonHash" value="${actionBean.uriHash}"/>
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
				    
				    	<c:if test='${actionBean.adminLoggedIn}'>
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
										<c:if test="${subjectUrl!=null}">
											<li>
												<stripes:url value="${actionBean.urlBinding}" event="harvestAjax"  var="url">
														<stripes:param name="uri" value="${actionBean.uri}"/>
												</stripes:url>
												<stripes:url value="${actionBean.urlBinding}" event="harvest"  var="oldUrl">
														<stripes:param name="uri" value="${actionBean.uri}"/>
												</stripes:url>
												<a id="wait_link" href="${oldUrl }" onclick="javascript:showWait('${pageContext.request.contextPath}', '${url }'); return false;">Harvest</a>

											</li>
										</c:if>
										
										
										
										
						    			<c:if test="${actionBean.urlFoundInHarvestSource}">
						    				<li>
						    				<stripes:link class="link-plain" href="/source.action?view=&harvestSource.url=${ subjectUrl }">Source  details</stripes:link>
						    				</li>
						    			</c:if>
						    			
						    			
				    					<c:if test="${ !actionBean.urlUserBookmark }">
				    						<li>
						    				<stripes:link class="link-plain" href="/factsheet.action">Add bookmark
						    				<stripes:param name="addbookmark" value="" />
						    				<stripes:param name="uri" value="${ subjectUrl }" />
						    				</stripes:link>
						    				</li>
				    					</c:if>
				    					
				    					<c:if test="${ actionBean.urlUserBookmark }">
				    						<li>
						    				<stripes:link class="link-plain" href="/factsheet.action">Remove bookmark
						    				<stripes:param name="removebookmark" value="" />
						    				<stripes:param name="uri" value="${ subjectUrl }" />
						    				</stripes:link>
						    				
						    				</li>
				    					</c:if>
				    					
				    					<c:if test="${subjectUrl!=null}">
											<li>
						    					<stripes:link class="link-plain" href="/home/${actionBean.userName}/reviews?add=Add&addUrl=${ subjectUrl }">Add review</stripes:link>
						    				</li>
						    			</c:if>
				    					
										
									</ul>
								</li>
							</ul>
						</c:if>
				    	<div style="margin-top:20px" id="wait_container">
				    
				    		<c:choose>
				    			<c:when test="${actionBean.subject.anonymous}">
				    				<div class="advice-msg">This is an anonymous resource!</div>
				    			</c:when>
				    			<c:when test="${subjectUrl!=null}">
				    				<div><p>Resource URL: <a class="link-external" href="${fn:escapeXml(subjectUrl)}"><c:out value="${subjectUrl}"/></a>
				    				<c:choose>
				    					<c:when	test ="${ actionBean.urlUserBookmark }"> (Bookmarked)</c:when>
				    				</c:choose></p></div>
				    				
				    				
				    			</c:when>
				    			<c:otherwise>
				    				<div class="advice-msg" title="${fn:escapeXml(subjectUri)}">This is an unresolvable resource!</div>
				    			</c:otherwise>
				    		</c:choose>
			    		
				    		<crfn:form action="/factsheet.action" method="post">
				    
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

				    		</crfn:form>
						</div>				    
				    </c:when>
				    <c:otherwise>
				    	<c:choose>
			    			<c:when test="${not empty actionBean.uri}">
			    				<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
				    				<c:if test="${not empty actionBean.url}">
					    				<ul id="dropdown-operations">
										<li><a href="#">Operations</a>
											<ul>
												<li>
													<stripes:link href="${actionBean.urlBinding}" event="harvest">Harvest
															<stripes:param name="uri" value="${actionBean.uri}"/>
													</stripes:link>
												</li>
											</ul>
										</li>
									</ul>
								</c:if>
							</c:if>
			    				<div style="margin-top:20px" class="note-msg">
								<strong>Unknown</strong>
								<p>Nothing is known about
			    					<c:choose>
			    						<c:when test="${not empty actionBean.url}">
			    							<a class="link-external" href="${actionBean.url}">${actionBean.url}</a>
			    						</c:when>
			    						<c:otherwise>
			    							${actionBean.uri}
			    						</c:otherwise>
			    					</c:choose>
								in Content Registry</p>
			    				</div>
			    			</c:when>
			    			<c:when test="${actionBean.uriHash!=0}">
			    				<div style="margin-top:20px" class="note-msg">
								<strong>Unknown</strong>
			    					<p>Nothing is known about this anonymous resource in Content Registry</p>
			    				</div>
			    			</c:when>
			    			<c:otherwise>
			    				<div class="error-msg">
			    					Resource identifier not specified!
			    				</div>
			    			</c:otherwise>
			    		</c:choose>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<div>&nbsp;</div>
			</c:otherwise>
		</c:choose>

	</stripes:layout-component>
</stripes:layout-render>
