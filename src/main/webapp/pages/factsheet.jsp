<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	
        <h1>Resource properties</h1>
        
	    <c:choose>
		    <c:when test="${actionBean.resource!=null}">
		    	<div style="margin-top:20px">
		    		<c:choose>
		    			<c:when test="${actionBean.url!=null}">
		    				<p>Click <a href="${actionBean.url}">here</a> to go to the resource's original location.</p>
		    			</c:when>
		    			<c:otherwise>
		    				<p>Link to the resource's original location was not found.</p>
		    			</c:otherwise>
		    		</c:choose>
			    	<table class="datatable" width="100%" cellspacing="0" summary="The table displays the resource's metadata elements in label, value columns">
			    		<tbody>
					    	<c:forEach var="resourceProperty" items="${actionBean.resourceProperties}">
					    		<tr>
					    			<th scope="row" class="scope-row metalabel" xml:lang="en">
					    				<c:out value="${resourceProperty.label}"/>
					    			</th>
					    			<td>
					    				<ul class="menu">
						    				<c:forEach var="resourcePropertyValue" items="${resourceProperty.values}">
						    					<li><c:out value="${resourcePropertyValue.label}"/></li>
						    				</c:forEach>
					    				</ul>
					    			</td>
					    		</tr>
					    	</c:forEach>
					    </tbody>
			    	</table>
			    </div>				    
		    </c:when>
		    <c:otherwise>
				No such resource found! 
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>
