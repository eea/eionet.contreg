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

        <div class="caution-msg">
            <strong>Caution ...</strong>
            <p>Work in progress!</p>
        </div>

        <%-- The page's heading --%>

        <h1>Monitor source deletions</h1>

        <div style="margin-top:20px">
                <p>
                    Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.<br/>
                    Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.<br/>
                    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.<br/>
                    Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<br/>
                </p>
        </div>

        <%-- The section that displays the source deletion queue. --%>

        <c:if test="${not empty actionBean.deletionQueue}">

            <div style="width:75%;padding-top:10px">

                <stripes:form id="deletionQueueForm" method="post" beanclass="${actionBean.class.name}">

                    <display:table name="${actionBean.deletionQueue}" id="queueItem" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:100%">
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

                    <stripes:submit name="cancel" value="Cancel" title="Cancel the deletion of selected URLs"/>
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
