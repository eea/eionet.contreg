<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View Harvesting Source">

	<stripes:layout-component name="contents">

		<c:choose>
			<c:when test="${actionBean.harvestSource!=null}">
			
				<c:if test="${not empty actionBean.currentHarvest && (actionBean.currentHarvest.sourceUrlString==actionBean.harvestSource.url)}">
					<div class="important-msg" style="margin-bottom:10px">This source is being harvested right now!</div>
				</c:if>
				<div id="tabbedmenu">
			    <ul>
			    	<c:forEach items="${actionBean.tabs}" var="loopItem">
						<c:choose>
					  		<c:when test="${actionBean.selectedTab == loopItem.id}" > 
								<li id="currenttab"><span><c:out value="${loopItem.value}"/></span></li>
							</c:when>
							<c:otherwise>
								<li>
									<stripes:link href="${actionBean.urlBinding}" event="${loopItem.id}">
										<c:out value="${loopItem.value}"/>
						                <stripes:param name="harvestSource.url" value="${actionBean.harvestSource.url }"/>
						            </stripes:link>
					            </li>
							</c:otherwise>
						</c:choose>
					</c:forEach>
			    </ul>
			</div>
			<br style="clear:left" />
			<div style="margin-top:20px;margin-bottom:5px">	
				<stripes:layout-render name="/pages/viewsource/${actionBean.selectedTab}.jsp"/>
			</div>
			</c:when>
			<c:otherwise>
				<div class="error-msg">No such harvest source found!</div>
			</c:otherwise>			
		</c:choose>			  
		    
	</stripes:layout-component>
</stripes:layout-render>
