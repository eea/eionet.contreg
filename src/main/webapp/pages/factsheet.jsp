<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">

        <c:choose>
		    <c:when test="${actionBean.context.eventName=='edit'}">
		    	<h1>Edit resource</h1>
			</c:when>
			<c:otherwise>
				<h1>Resource factsheet</h1>
			</c:otherwise>
		</c:choose>		    
        
        <c:choose>
		    <c:when test="${actionBean.subject!=null}">		    
		    	<c:set var="subjectUrl" value="${actionBean.subject.url}"/>
		    	<c:set var="subjectUri" value="${actionBean.subject.uri}"/>
		    	<div style="margin-top:20px">
		    	
		    		<c:choose>
		    			<c:when test="${subjectUrl!=null}">
		    				<p>Resource URL: <a class="link-external" href="${fn:escapeXml(subjectUrl)}"><c:out value="${subjectUrl}"/></a></p>
		    			</c:when>
		    			<c:when test="${subjectUri!=null}">
		    				<div class="advice-msg" title="${fn:escapeXml(subjectUri)}">This is an unresolvable resource!</div>
		    			</c:when>
		    			<c:otherwise>
		    				<div class="advice-msg">This is an anonymous resource!</div>
		    			</c:otherwise>
		    		</c:choose>
		    		
		    		<c:if test="${actionBean.context.eventName=='edit'}">
		    			<stripes:form action="/factsheet.action" method="post">
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
				    					<stripes:hidden name="uri" value="${subjectUri}"/>
										<stripes:hidden name="anonymous" value="${actionBean.subject.anonymous}"/>
				    				</td>
				    		</table>
			    		</stripes:form>
		    		</c:if>
		    		
					<stripes:layout-render name="/pages/common/factsheet_table.jsp" subjectUrl="${subjectUrl}" subjectUri="${subjectUri}"/>
					
				</div>				    
		    </c:when>
		    <c:otherwise>
		    	<div style="margin-top:20px" class="note-msg"><strong>Unknown resource</strong>
				<p>Nothing is known about ${actionBean.uri}</p>
			</div>
			</c:otherwise>
		</c:choose>
	    				
	</stripes:layout-component>
</stripes:layout-render>
