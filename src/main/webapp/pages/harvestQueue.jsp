<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Recent uploads">

	<stripes:layout-component name="contents">
	
        <h1>Harvest queue</h1>
        
        <p>This page displays the harvest queue. There is a job that adds cron-scheduled harvests into this queue<br/>
        and there is another job that removes them and performs the actual harvest.<br/>
        It is possible that at some point the queue is empty and at some point it is quite heavily loaded.<br/>
        The queue is divided into two: <em>normal queue</em> and <em>urgent queue</em>.<br/>
        Normal queue contains cron-scheduled harvests and push harvests requested by other applications.<br/>
        Urgent queue is created by system administrators who can request an urgent harvest at any time.<br/>
        Harvests from the urgent queue are executed before harvests in the normal queue.</p>
        
        <c:if test="${not empty actionBean.currentlyHarvestedQueueItem}">
			<div class="advise-msg" style="margin-bottom:10px">
				<c:choose>
					<c:when test="${actionBean.currentlyHarvestedQueueItem.pushHarvest}">Currently push-harvesting:&nbsp;</c:when>
					<c:otherwise>Currently pull-harvesting:&nbsp;</c:otherwise>
				</c:choose><a href="javascript:doNothing()" title="${actionBean.currentlyHarvestedQueueItem.url}"><c:out value="${crfn:cutAtFirstLongToken(actionBean.currentlyHarvestedQueueItem.url,55)}"/></a>
			</div>
		</c:if>
		
	    <div id="tabbedmenu">
		    <ul>
		    	<c:forEach items="${actionBean.priorities}" var="loopPriority">
					<c:choose>
				  		<c:when test="${actionBean.priority==loopPriority.priority}" > 
							<li id="currenttab"><span><c:out value="${loopPriority.title}"/></span></li>
						</c:when>
						<c:otherwise>
							<li>
								<stripes:link href="/harvestQueue.action">
									<c:out value="${loopPriority.title}"/>
					                <stripes:param name="priority" value="${loopPriority.priority}"/>
					            </stripes:link>
				            </li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
		    </ul>
		</div>
		
		<br style="clear:left" />
		
		<div style="margin-top:20px">
		
			<c:choose>
				<c:when test="${not empty actionBean.list}">
			
					<display:table name="${actionBean.list}" class="sortable" pagesize="20" sort="list" id="queueItem" htmlId="queueItems" requestURI="${actionBean.urlBinding}" style="width:100%">
					
						<display:setProperty name="paging.banner.items_name" value="queue items"/>
						
						<display:column property="url" title="URL" sortable="true"/>
						<display:column property="timeAdded" title="Time added" sortable="true"/>
						<display:column title="Harvest type" sortable="true">
							<c:choose>
								<c:when test="${queueItem.pushHarvest}">push</c:when>
								<c:otherwise>pull</c:otherwise>
							</c:choose>
						</display:column>
						
					</display:table>
				</c:when>
				<c:otherwise>
					<p>No items found in this queue!</p>
				</c:otherwise>
			</c:choose>
		</div>                     
		
	</stripes:layout-component>
</stripes:layout-render>
