<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest queue">

    <stripes:layout-component name="contents">

        <h1>Harvest queue</h1>

        <p>This page displays the harvest queue. It is divided into two: <em>urgent queue</em> and <em>batch queue</em>.
        The batch queue contains the sources that are being harvested at the current run of the batch harvester. If the
        batch harvester is not executing at the moment, then the batch queue displays the sources that will be harvested
        next time the batch harvester executes. If the batch queue is empty then either there is nothing to harvest or
        batch harvesting has been turned off.
        The urgent queue is created by system administrators who can request an urgent harvest at any time.
        Harvests in the urgent queue have always a higher priority over those in the batch queue.</p>

        <c:if test="${empty actionBean.batchHarvestingHours}">
            <div class="tip-msg" style="margin-bottom:10px">
                According to the current configuration, batch harvesting is turned off!
            </div>
        </c:if>

        <c:if test="${not empty actionBean.batchHarvestingHours}">
            <div class="tip-msg" style="margin-bottom:10px">
                Batch harvesting has been configured to run only at these hours: <c:out value="${actionBean.batchHarvestingHours}"/>
            </div>
        </c:if>

        <c:if test="${not empty actionBean.currentQueuedHarvest}">
            <div class="advise-msg" style="margin-bottom:10px">
                <c:choose>
                    <c:when test="${fn:endsWith(currentHarvest.class.name, 'PushHarvest')}">Currently push-harvesting:</c:when>
                    <c:otherwise>Currently pull-harvesting:</c:otherwise>
                </c:choose><stripes:link href="/source.action" title="${actionBean.currentQueuedHarvest.contextUrl}">
<c:out value="${crfn:cutAtFirstLongToken(actionBean.currentQueuedHarvest.contextUrl,55)}"/>
<stripes:param name="view" value=""/>
<stripes:param name="harvestSource.url" value="${actionBean.currentQueuedHarvest.contextUrl}"/>
</stripes:link>
            </div>
        </c:if>

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

                        <crfn:form id="resultSetForm" beanclass="${actionBean.class.name}" method="post">

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

            <c:if test="${actionBean.typeDemand}">
                <c:choose>
                    <c:when test="${not empty actionBean.onDemandHarvestEntries}">

                        <display:table name="${actionBean.onDemandHarvestEntries}" class="sortable" pagesize="20" sort="list" id="entry" htmlId="queueItems" requestURI="${actionBean.urlBinding}" style="width:100%">

                            <display:setProperty name="paging.banner.items_name" value="sources"/>

                            <display:column title="URL" sortable="true" sortProperty="key">
                                <stripes:link beanclass="eionet.cr.web.action.factsheet.FactsheetActionBean">
                                    <stripes:param name="uri" value="${entry.key}"/>
                                    <c:out value="${entry.key}"/>
                                </stripes:link>
                            </display:column>
                            <display:column property="value" title="User" sortable="true"/>

                        </display:table>

                    </c:when>
                    <c:otherwise>
                        <p>No items found in this list.</p>
                    </c:otherwise>
                </c:choose>
            </c:if>

        </div>

    </stripes:layout-component>
</stripes:layout-render>
