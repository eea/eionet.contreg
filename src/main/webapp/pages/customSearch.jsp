<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Custom search">

	<stripes:layout-component name="contents">
	
        <h1>Custom search</h1>
        <p>
        	aaaaaaaaaaaasdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf<br/>
        	sdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf
        </p>
        
        <p>
        	${actionBean.selectedFilters}
        </p>
        

		<c:choose>        
        	<c:when test="${actionBean.availableFilters!=null && fn:length(actionBean.availableFilters)>0}">
        
		    	<div id="filterSelectionArea" style="margin-top:20px">
		    	
		    		<stripes:form action="/customSearch.action" method="get" id="customSearchForm">
		    		
		    			<stripes:select name="addedFilter" id="filterSelect">
		    				<stripes:option value="" label=""/>
		    				<c:forEach var="availableFilter" items="${actionBean.availableFilters}">
		    					<c:if test="${actionBean.selectedFilters[availableFilter.key]==null}">
		    						<stripes:option value="${availableFilter.key}" label="${availableFilter.value.title}" title="${availableFilter.value.uri}"/>
		    					</c:if>
		    				</c:forEach>
		    			</stripes:select>&nbsp;
		    			<stripes:submit name="addFilter" value="Add filter"/>
		    			
		    			<c:if test="${actionBean.selectedFilters!=null && fn:length(actionBean.selectedFilters)>0}">
			    			<table style="margin-top:20px;margin-bottom:20px">
			    				<c:forEach var="availableFilter" items="${actionBean.availableFilters}">
			    					<c:if test="${actionBean.selectedFilters[availableFilter.key]!=null}">
				    					<tr>
				    						<td style="padding-right:12px">
					    						<stripes:link href="/customSearch.action" event="removeFilter">
													<img src="${pageContext.request.contextPath}/images/delete.gif" title="Remove filter" alt="Remove filter"/>
													<stripes:param name="removedFilter" value="${availableFilter.key}"/>
													<c:forEach var="selectedFilter" items="${actionBean.selectedFilters}">
														<stripes:param name="value_${selectedFilter.key}" value="${selectedFilter.value}"/>
													</c:forEach>													
												</stripes:link>
				    						</td>
				    						<td style="text-align:right">${availableFilter.value.title}:</td>
				    						<td>
				    							<c:if test="${param.showPicklist==null || actionBean.picklistFilter!=availableFilter.key || (param.showPicklist!=null && actionBean.picklistFilter==availableFilter.key && (actionBean.picklist==null || fn:length(actionBean.picklist)==0))}">
				    								<input type="text" name="value_${availableFilter.key}" value="${fn:escapeXml(actionBean.selectedFilters[availableFilter.key])}" size="30"/>
				    							</c:if>
				    							<c:if test="${param.showPicklist!=null && actionBean.picklistFilter==availableFilter.key && actionBean.picklist!=null && fn:length(actionBean.picklist)>0}">
													<stripes:select name="value_${availableFilter.key}" style="max-width:400px">
					                        			<stripes:option value="" label=""/>
					                        			<c:if test="${actionBean.picklist!=null}">
						                        			<c:forEach var="picklistItem" items="${actionBean.picklist}">
						                        				<stripes:option value='"${picklistItem}"' label="${picklistItem}"/>
						                        			</c:forEach>
						                        		</c:if>
													</stripes:select>
				    							</c:if>
				    							<c:if test="${param.showPicklist==null || actionBean.picklistFilter!=availableFilter.key}">
				    								<stripes:link href="/customSearch.action" event="showPicklist" style="position:absolute">
														<img style="padding-top: 1px;" src="${pageContext.request.contextPath}/images/list.gif" title="Get existing values" alt="Get existing values"/>
														<stripes:param name="picklistFilter" value="${availableFilter.key}"/>
														<c:forEach var="selectedFilter" items="${actionBean.selectedFilters}">
															<stripes:param name="value_${selectedFilter.key}" value="${selectedFilter.value}"/>
														</c:forEach>
													</stripes:link>
				    							</c:if>
				    							<c:if test="${param.showPicklist!=null && actionBean.picklistFilter==availableFilter.key && (actionBean.picklist==null || fn:length(actionBean.picklist)==0)}">
				    								No picklist found!
				    							</c:if>
				    						</td>
				    					</tr>
				    				</c:if>
			    				</c:forEach>
			    			</table>
			    			<stripes:submit name="search" value="Search"/>
		    			</c:if>
	    				
		    		</stripes:form>
			    </div>
			    
			</c:when>
			<c:otherwise>
				No available filters found!
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>
