<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest queue">

    <stripes:layout-component name="contents">

        <h1>Harvest queue</h1>

        <p>
            This page displays the queues of urgent and batch harvests, and the list of on-going on-demand harvests.
            <p>
                An <b>urgent</b> harvest is urgent, but not immediate. An <b>on-demand</b> harvest is immediate. A <b>batch</b>
                harvest is executed according to time schedule.
            </p>
            <p>
                At most one harvest is picked from the urgent and batch queues at a time, and no batch harvest is executed before
                the urgent queue is completed. The queues are checked every ${actionBean.queueCheckingInterval} seconds.
                On-demand harvests can be executed several in parallel, and also at the same time with a harvest from the urgent
                or batch queue. However, no source is executed by the on-demand harvester and urgent/batch queue at the same time.
            </p>
        </p>

        <div class="tip-msg">
            <strong>Information</strong>
            <c:if test="${actionBean.typeBatch and empty actionBean.batchHarvestingHours}">
                <p>According to the current configuration, batch harvesting is turned off!</p>
            </c:if>
            <c:if test="${actionBean.typeBatch and not empty actionBean.batchHarvestingHours}">
                <p>Batch harvesting has been configured to run only at these hours: <c:out value="${actionBean.batchHarvestingHours}"/></p>
            </c:if>
            <c:if test="${empty actionBean.currentQueuedHarvest and empty actionBean.onDemandHarvestEntries}">
                <p>No harvest being executed at the moment!</p>
            </c:if>
            <c:if test="${not empty actionBean.currentQueuedHarvest or not empty actionBean.onDemandHarvestEntries}">
                <c:if test="${not empty actionBean.currentQueuedHarvest}">
                    <p>
                        Currently executing from the urgent or batch queue:<br/>
                        <stripes:link beanclass="${actionBean.harvestSourceActionBeanClass}" title="${actionBean.currentQueuedHarvest.contextUrl}">
                            <c:out value="${crfn:cutAtFirstLongToken(actionBean.currentQueuedHarvest.contextUrl,110)}"/>
                                <stripes:param name="harvestSource.url" value="${actionBean.currentQueuedHarvest.contextUrl}"/>
                        </stripes:link>
                    </p>
                </c:if>
                <c:if test="${not empty actionBean.onDemandHarvestEntries}">
                    <p>
                        Currently on-going on-demand harvests:
                        <c:forEach items="${actionBean.onDemandHarvestEntries}" var="onDemandHarvest">
                            <br/>
                            <stripes:link beanclass="${actionBean.harvestSourceActionBeanClass}" title="${onDemandHarvest.key}">
                                <c:out value="${crfn:cutAtFirstLongToken(onDemandHarvest.key, 110)}"/>
                                    <stripes:param name="harvestSource.url" value="${onDemandHarvest.key}"/>
                            </stripes:link>
                        </c:forEach>
                    </p>
                </c:if>
            </c:if>

        </div>

        <div id="tabbedmenu">
            <ul>
                <c:forEach items="${actionBean.queueTypes}" var="loopQueueType">
                    <c:choose>
                          <c:when test="${actionBean.queueType==loopQueueType.queueType}" >
                            <li id="currenttab"><span><c:out value="${loopQueueType.title}"/></span></li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <stripes:link href="/harvestQueue.action">
                                    <c:out value="${loopQueueType.title}"/>
                                    <stripes:param name="queueType" value="${loopQueueType.queueType}"/>
                                </stripes:link>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ul>
        </div>

        <br style="clear:left" />

        <div style="margin-top:20px">

            <c:if test="${actionBean.typeUrgent}">
                <c:choose>
                    <c:when test="${not empty actionBean.urgentQueue}">

                        <c:forEach items="${actionBean.urgentQueue}" var="queueItem">
                            <c:if test="${actionBean.user.administrator || (not empty actionBean.user.userName && actionBean.user.userName eq queueItem.userName)}">
                                <c:set var="isRemovables" value="true"/>
                            </c:if>
                        </c:forEach>

                        <crfn:form id="resultSetForm" beanclass="${actionBean['class'].name}" method="post">

                            <display:table name="${actionBean.urgentQueue}" class="sortable" pagesize="20" sort="list" id="queueItem" htmlId="queueItems" requestURI="${actionBean.urlBinding}" style="width:100%">

                                <display:setProperty name="paging.banner.items_name" value="queue items"/>

                                <c:if test="${isRemovables}">
                                    <display:column style="width:10px">
                                        <c:choose>
                                            <c:when test="${actionBean.user.administrator || (not empty actionBean.user.userName && actionBean.user.userName eq queueItem.userName)}">
                                                <stripes:checkbox name="selectedItems" value="${queueItem.itemId}" />
                                            </c:when>
                                            <c:otherwise>&nbsp;</c:otherwise>
                                        </c:choose>
                                    </display:column>
                                </c:if>
                                <display:column property="url" sortable="true" style="width:60%" title='<span title="URL to be harvested">URL</span>'/>
                                <display:column property="timeAdded" sortable="true" style="max-width:20%" title='<span title="Time when the item was added to the queue">Time added</span>'/>
                                <display:column property="userName" sortable="true" style="max-width:20%" title='<span title="The user who added the item to the queue">User</span>'/>

                            </display:table>
                            <c:if test="${isRemovables}">
                                <div>
                                    <stripes:submit name="remove" value="Remove" title="Remove selected URLs"/>
                                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAllForField('resultSetForm', 'selectedItems');return false;"/>
                                </div>
                            </c:if>

                        </crfn:form>
                    </c:when>
                    <c:otherwise>
                        <p>No items found in this queue.</p>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <c:if test="${actionBean.typeBatch}">
                <c:choose>
                    <c:when test="${not empty actionBean.batchQueue}">

                        <display:table name="${actionBean.batchQueue}" class="sortable" pagesize="20" sort="list" id="harvestSource" htmlId="queueItems" requestURI="${actionBean.urlBinding}" style="width:100%">

                            <display:setProperty name="paging.banner.items_name" value="sources"/>

                            <display:column property="url" title="URL" sortable="true"/>
                            <display:column property="lastHarvest" title="Last harvest" sortable="true"/>

                        </display:table>

                    </c:when>
                    <c:otherwise>
                        <p>No items found in this queue.</p>
                    </c:otherwise>
                </c:choose>
            </c:if>

        </div>

    </stripes:layout-component>
</stripes:layout-render>
