<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View Harvesting Source">

	<stripes:layout-component name="contents">

		<c:choose>
			<c:when test="${actionBean.harvestSource!=null}">
			
				<c:if test="${not empty actionBean.currentHarvest && (actionBean.currentHarvest.sourceUrlString==actionBean.harvestSource.url)}">
					<div class="important-msg" style="margin-bottom:10px">This source is being harvested right now!</div>
				</c:if>
				
				<h1>View source</h1>
				<br/>
			    <stripes:form action="/source.action" focus="">
			    	<stripes:hidden name="harvestSource.sourceId"/>
			        <table>
			            <tr>
			                <td>URL:</td>
			                <td><a class="link-external" href="${fn:escapeXml(actionBean.harvestSource.url)}"><c:out value="${actionBean.harvestSource.url}"/></a></td>
			            </tr>
			            <tr>
			                <td>E-mails:</td>
			                <td><c:out value="${actionBean.harvestSource.emails}"/></td>
			            </tr>
			            <tr>
			                <td>Date created:</td>
			                <td>
			                	<c:out value="${actionBean.harvestSource.timeCreated}"/>
			                </td>
			            </tr>
			            <tr>
			                <td>Number of resources:</td>
			                <td>
			                	<c:out value="${actionBean.noOfResources}"/>
			                </td>
			            </tr>
			            <tr>
			                <td>Harvest interval:</td>
			                <td>
			                	<c:out value="${actionBean.intervalMinutesDisplay}"/>
			                </td>
			            </tr>
			            <tr>
			                <td>Last harvest:</td>
			                <td>
			                	<c:out value="${actionBean.harvestSource.lastHarvest}"/>
			                </td>
			            </tr>
		           		<c:if test="${actionBean.harvestSource.unavailable}">
		           			<tr>
		           				<td colspan="2" class="warning-msg" style="color:#E6E6E6">The source has been unavailable for too many times!</td>
		           			</tr>
		           		</c:if>
		           		
			            <tr>
			                <td colspan="2" style="padding-top:10px">
			                	<stripes:submit name="goToEdit" value="Edit" title="Edit this harvest source"/>
			                    <stripes:submit name="scheduleUrgentHarvest" value="Schedule urgent harvest"/>
			                </td>
			            </tr>
			        </table>
			        <br/>
			        <c:if test="${not empty actionBean.sampleTriples}">			        	
				        <table id="sampletriples" class="datatable">
				        	<caption>Sample triples:</caption>
									<col style="width:30%"/>
									<col style="width:30%"/>
									<col style="width:40%"/>
				        	<thead>
					        	<tr>
					        		<th scope="col">Subject</th>
					        		<th scope="col">Predicate</th>
					        		<th scope="col">Object</th>
					        	</tr>
				        	</thead>
				        	<tbody>
				        		<c:forEach items="${actionBean.sampleTriples}" var="sampleTriple" varStatus="loop">
				        			<tr <c:if test="${not empty sampleTriple.objectDerivSource}">class="derived"</c:if>>
				        				<td>
				        					<c:choose>
				        						<c:when test="${sampleTriple.subject!=null}">
				        							<c:out value="${crfn:cutAtFirstLongToken(sampleTriple.subject, 100)}"/>
				        						</c:when>
				        						<c:otherwise>
				        							Anonymous resource
				        						</c:otherwise>
				        					</c:choose>
				        				</td>
				        				<td><c:out value="${crfn:cutAtFirstLongToken(sampleTriple.predicate, 100)}"/></td>
				        				<td><c:out value="${crfn:cutAtFirstLongToken(sampleTriple.object, 100)}"/></td>
				        			</tr>
				        		</c:forEach>
				        	</tbody>
				        </table>
				    </c:if>
			        <table class="datatable">
			        	<caption style="text-align:left;color:black">Last 10 harvests:</caption>
			        	<thead>
				        	<tr>
				        		<th scope="col">Type</th>
				        		<th scope="col">User</th>
				        		<th scope="col">Started</th>
				        		<th scope="col">Finished</th>
				        		<th scope="col">Triples</th>
				        		<th scope="col">Subjects</th>
				        		<th scope="col"></th>
				        	</tr>
			        	</thead>
			        	<tbody>
			        		<c:forEach items="${actionBean.harvests}" var="harv" varStatus="loop">
			        			<tr>
			        				<td><c:out value="${harv.harvestType}"/></td>
			        				<td><c:out value="${harv.user}"/></td>
			        				<td><fmt:formatDate value="${harv.datetimeStarted}" pattern="dd-MM-yy HH:mm:ss"/></td>
			        				<td><fmt:formatDate value="${harv.datetimeFinished}" pattern="dd-MM-yy HH:mm:ss"/></td>		        				
			        				<td><c:out value="${harv.totalStatements}"/></td>
			        				<td><c:out value="${harv.totalResources}"/></td>
									<td>
				        				<stripes:link href="/harvest.action">
				        					<img src="${pageContext.request.contextPath}/images/view2.gif" title="View" alt="View"/>
											<c:if test="${(!(empty harv.hasFatals) && harv.hasFatals) || (!(empty harv.hasErrors) && harv.hasErrors)}">
												<img src="${pageContext.request.contextPath}/images/error.png" title="Errors" alt="Errors"/>
											</c:if>
											<c:if test="${!(empty harv.hasWarnings) && harv.hasWarnings}">
												<img src="${pageContext.request.contextPath}/images/warning.png" title="Warnings" alt="Warnings"/>
											</c:if>	                                
			                                <stripes:param name="harvestDTO.harvestId" value="${harv.harvestId}"/>
			                            </stripes:link>
				        			</td>	        				
			        			</tr>
			        		</c:forEach>
			        	</tbody>
			        </table>
			    </stripes:form>
			</c:when>
			<c:otherwise>
				<div class="error-msg">No such harvest source found!</div>
			</c:otherwise>			
		</c:choose>			  
		    
	</stripes:layout-component>
</stripes:layout-render>
