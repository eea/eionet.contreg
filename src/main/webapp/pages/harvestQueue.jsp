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
		
	    <div id="tabbedmenu">
		    <ul>
		    	<c:forEach items="${actionBean.priorities}" var="loopPriority">
					<c:choose>
				  		<c:when test="${actionBean.priority==loopPriority.priority}" > 
							<li id="currenttab"><span>${fn:escapeXml(loopPriority.title)}</span></li>
						</c:when>
						<c:otherwise>
							<li>
								<stripes:link href="/harvestQueue.action">
									${fn:escapeXml(loopPriority.title)}
					                <stripes:param name="priority" value="${loopPriority.priority}"/>
					            </stripes:link>
				            </li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
		    </ul>
		</div>
		<br style="clear:left" />
				
	</stripes:layout-component>
</stripes:layout-render>
