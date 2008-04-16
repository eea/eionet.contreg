<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	
        <h1>Resource properties</h1>
        
        <div style="margin-top:20px">
		    <c:choose>
			    <c:when test="${actionBean.resource!=null}">
			    	<table class="datatable" width="100%" cellspacing="0" summary="The table displays the resource's metadata elements in label, value columns">
			    		<tbody>
					    	<c:forEach var="resourceProperty" items="${actionBean.resourceProperties}">
					    		<tr>
					    			<th scope="row" class="scope-row metalabel" xml:lang="en">
					    				<c:out value="${resourceProperty.propertyLabel}"/>
					    			</th>
					    			<td><c:out value="${resourceProperty.valueLabel}"/></td>
					    		</tr>
					    	</c:forEach>
					    </tbody>
			    	</table>
			    </c:when>
			    <c:otherwise>
					No such resource found! 
				</c:otherwise>
			</c:choose>
		</div>
				
	</stripes:layout-component>
</stripes:layout-render>
