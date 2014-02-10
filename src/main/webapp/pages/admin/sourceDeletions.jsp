<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Monitor source deletions">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){
                        // Add JQuery here.
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>Monitor source deletions</h1>

        <div style="margin-top:15px">
                <p>
                    On this page you can monitor the queue of sources that have been marked for background deletion.
                    They are listed in the ascending order of timestamp when the deletion was requested. If several sources
                    have the same timestamp, they are ordered by source URL ascending.<br/>
                    You can filter the list by entering URL portion in the below filter box.<br/>
                    For canceling the deletion of selected sources, use the given checkboxes and Cancel button.
                </p>
        </div>

        <%-- The section that displays the source deletion queue. --%>

        <c:if test="${not empty actionBean.deletionQueue}">

            <div style="width:100%;padding-top:10px">

                <stripes:form id="deletionQueueForm" method="post" beanclass="${actionBean.class.name}">

                    <div style="margin-bottom:10px">
	                    <stripes:label for="filterText" class="question">URL filter:</stripes:label>
	                    <stripes:text id="filterText" name="filter" size="60"/>
	                    <stripes:submit name="view" value="Filter"/>
                    </div>

                    <display:table name="${actionBean.deletionQueuePaginated}" id="queueItem" class="sortable" sort="external" pagesize="${actionBean.resultListPageSize}" requestURI="${actionBean.urlBinding}" style="width:100%">
                        <display:column style="width:5%">
                            <stripes:checkbox name="cancelUrls" value="${queueItem.left}" />
                        </display:column>
                        <display:column title="URL" style="width:75%">
                            <stripes:link beanclass="${actionBean.sourceFactsheetBeanClass.name}" title="${fn:escapeXml(queueItem.left)}">
                                <c:out value="${crfn:cutAtFirstLongToken(queueItem.left, 80)}"/>
                            </stripes:link>
                        </display:column>
                        <display:column title="Timestamp" style="width:20%">
                            <fmt:formatDate value="${queueItem.right}" pattern="yyyy-MM-dd HH:mm:ss" />
                        </display:column>
                    </display:table>

                    <stripes:submit name="cancel" value="Cancel selected" title="Cancel the deletion of selected URLs"/>
                    <input type="button" onclick="toggleSelectAll('deletionQueueForm');return false" value="Select all" name="selectAll">

                </stripes:form>
            </div>

        </c:if>

        <%-- Message if deletion queue is empty. --%>

        <c:if test="${empty actionBean.deletionQueue}">
            <div class="system-msg">No sources found in deletion queue!</div>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
