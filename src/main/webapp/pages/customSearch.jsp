<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Custom search">

	<stripes:layout-component name="contents">
	
		<script lang="JavaScript">
			<!--
						
			function textSet(idEdit, text, sel)
			{
				document.getElementById(idEdit).value = text;
				document.getElementById(sel).value = '';
				document.getElementById(idEdit).focus();
				
			}
			
			function submitGetList(selectedKey)
			{
				document.forms["customSearchForm"].elements["listKey"].value = selectedKey;
				document.forms["customSearchForm"].submit();
				
			}
			
			//-->
		</script>

	
        <h1>Custom search</h1>        
        <p>
        	sdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf<br/>
        	sdhgf sdhgf sajkgfashkdf askjgf askf skdhf asjdhgf asjkdgfsajdhgf
        </p>
        
    	<div id="filterSelectionArea" style="margin-top:20px">
    		<stripes:form action="/customSearch.action" method="get" id="customSearchForm">
    		
    			<stripes:select name="filterKey" id="filterSelect">
    				<stripes:option value="" label=""/>
    				<c:if test="${actionBean.unselectedFilters!=null && fn:length(actionBean.unselectedFilters)>0}">
	    				<c:forEach var="unselectedFilter" items="${actionBean.unselectedFilters}">
	    					<stripes:option value="${unselectedFilter.key}" label="${unselectedFilter.title}" title="${unselectedFilter.uri}"/>
	    				</c:forEach>
	    			</c:if>
    			</stripes:select>&nbsp;
    			<stripes:submit name="addFilter" value="Add filter"/>
    			
    			<c:if test="${actionBean.selectedFilters!=null && fn:length(actionBean.selectedFilters)>0}">
	    			<table style="margin-top:20px;margin-bottom:20px">
	    				<c:forEach var="selectedFilter" items="${actionBean.selectedFilters}">
	    					<tr>
	    						<td style="padding-right:12px">
		    						<stripes:link href="/customSearch.action" event="removeFilter">
										<img src="${pageContext.request.contextPath}/images/delete.gif" title="Remove filter" alt="Remove filter"/>
										<stripes:param name="filterKey" value="${selectedFilter.key}"/>
									</stripes:link>
	    						</td>
	    						<td style="text-align:right">${selectedFilter.title}:</td>
	    						<td>
	    							<input type="text" id="val_${selectedFilter.key}" name="val_${selectedFilter.key}" size="10">
	    						</td>
	    						<td>
	    							<c:choose>
			                        	<c:when test="${actionBean.getListKey == selectedFilter.key}">    
			                        		<select id="sel_${selectedFilter.key}" onchange="JavaScript:textSet('val_${selectedFilter.key}',this.value,'sel_${selectedFilter.key}');">
												<option value=""></option>
			                        			<option value="ARGENTINA">ARGENTINA</option>
										        <option value="VIETNAM">VIETNAM</option>
										        <option value="XXX">XXX</option>
											</select>
								    	</c:when>
				                        <c:otherwise>
		    								<img src="${pageContext.request.contextPath}/images/select.gif" title="Show list" alt="Show list" onclick="javascript:submitGetList(${selectedFilter.key});"/>
				                        </c:otherwise>
				                    </c:choose>
	    						</td>
	    					</tr>
	    				</c:forEach>
	    				<input type="hidden" name="listKey" value=""/>
	    				<input type="hidden" name="list" value=""/>
	    			</table>
	    			<stripes:submit name="search" value="Search"/>
	    		</c:if>
	    		
	    		<c:if test="${actionBean.selected!=null && fn:length(actionBean.selected)>0}">
	    			<c:forEach var="selected" items="${actionBean.selected}">
	    				<input type="hidden" name="selected" value="${selected}"/>
	    			</c:forEach>
	    		</c:if>
	    		
    		</stripes:form>
	    </div>				    
				
	</stripes:layout-component>
</stripes:layout-render>
