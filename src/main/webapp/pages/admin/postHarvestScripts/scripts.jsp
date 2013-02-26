<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>

    <ul id="dropdown-operations">
        <li><a href="#">Operations</a>
            <ul>
            <li>
	            <stripes:link href="/admin/postHarvestScripts" event="search" title="Search scripts">
	                <c:out value="Search scripts"/>
	            </stripes:link>
            </li>
            <c:if test="${empty actionBean.targetType}">
                <li>
                     <stripes:link href="/admin/postHarvestScript" title="Create all-source script">
                         <c:out value="Create script"/>
                     </stripes:link>
                </li>
             </c:if>
             <c:if test="${not empty actionBean.targetType}">
                 <c:if test="${empty actionBean.targetUrl}">
                     <li>
                         <stripes:link href="/admin/postHarvestScript" title="Create ${fn:toLowerCase(actionBean.targetType)}-specific script">
                             <c:out value="Create script"/>
                             <stripes:param name="targetType" value="${actionBean.targetType}"/>
                         </stripes:link>
                     </li>
                 </c:if>
                 <c:if test="${not empty actionBean.targetUrl}">
                     <li>
                        <stripes:link href="/admin/postHarvestScript" title="Create script for this ${fn:toLowerCase(actionBean.targetType)}">
                            <c:out value="Create script for this ${fn:toLowerCase(actionBean.targetType)}"/>
                            <stripes:param name="targetType" value="${actionBean.targetType}"/>
                            <stripes:param name="targetUrl" value="${actionBean.targetUrl}"/>
                        </stripes:link>
                        <stripes:link href="/admin/postHarvestScript" title="Create script for another ${fn:toLowerCase(actionBean.targetType)}">
                            <c:out value="Create script for another ${fn:toLowerCase(actionBean.targetType)}"/>
                            <stripes:param name="targetType" value="${actionBean.targetType}"/>
                            <stripes:param name="backToTargetUrl" value="${actionBean.targetUrl}"/>
                        </stripes:link>
                        <c:if test="${actionBean.targetType == 'SOURCE'}">
                            <stripes:link href="/sourceView.action" title="Schedule urgent harvest" event="scheduleUrgentHarvest">
                                <c:out value="Schedule urgent harvest" />
                                <stripes:param name="uri" value="${actionBean.targetUrl}"/>
                            </stripes:link>
                        </c:if>
                     </li>
                 </c:if>
             </c:if>
            </ul>
        </li>
    </ul>

    <c:if test="${not empty actionBean.targetType}">
        <c:if test="${not empty actionBean.targets}">
            <div style="margin-top:3em">
                <crfn:form id="targetsForm" action="/admin/postHarvestScripts" method="get">
                 <div>Displaying scripts of:</div>
         <div>
                 <stripes:select name="targetUrl" id="targetSelect" value="${actionBean.targetUrl}" onchange="this.form.submit();" size="10">
<!--
                      <c:if test="${empty actionBean.targetUrl}">
                         <stripes:option value="" label="-- select a ${fn:toLowerCase(actionBean.targetType)} --"/>
                     </c:if>
-->
                     <c:forEach items="${actionBean.targets}" var="target">
                         <stripes:option value="${target.left}" label="${target.left}"/>
                     </c:forEach>
                 </stripes:select>
                  <stripes:hidden name="targetType"/>
          </div>
                </crfn:form>
            </div>
        </c:if>
        <c:if test="${empty actionBean.targets}">
            <div style="margin-top:3em">No ${fn:toLowerCase(actionBean.targetType)}-specific scripts found! Use operations menu to create one.</div>
        </c:if>
    </c:if>

    <c:if test="${not empty actionBean.scripts}">

        <c:if test="${fn:length(actionBean.scripts)>1}">
            <div class="advice-msg" style="clear:both;width:100%;margin-top:1em;">
                Note: scripts will be executed in the below order. Use "Move up/down" buttons to change that.
            </div>
        </c:if>

        <div style="float:left;width:100%;padding-top:1.2em;">

            <crfn:form id="scriptsForm" action="/admin/postHarvestScripts" method="post">

                <display:table name="${actionBean.scripts}" class="datatable" id="script" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

                    <display:setProperty name="paging.banner.item_name" value="script"/>
                    <display:setProperty name="paging.banner.items_name" value="scripts"/>
                    <display:setProperty name="paging.banner.all_items_found" value='<div class="pagebanner">{0} {1} found.</div>'/>
                    <display:setProperty name="paging.banner.onepage" value=""/>

                     <display:column style="width:2em;text-align:center">
                         <input type="checkbox" name="selectedIds" value="${script.id}" title="Select this script"/>
                     </display:column>
                     <display:column title='<span title="Title assigned to the script">Title</span>'>
                      <stripes:link href="/admin/postHarvestScript" title="View, edit and test this script">
                          <c:out value="${script.title}"/>
                          <stripes:param name="id" value="${script.id}"/>
                      </stripes:link>
                     </display:column>
                     <display:column style="width:3em;text-align:center" title='<span title="Indicates whether the script is currently turned off">Active</span>'>
                        <c:out value="${script.active ? 'Yes' : 'No'}"/>
                     </display:column>
                     <display:column style="width:12em;text-align:center" title='<span title="Script last modification time">Last modified</span>'>
                        <c:out value="${script.lastModified}"/>
                     </display:column>

                </display:table>

                <c:if test="${not empty actionBean.scripts && fn:length(actionBean.scripts)>0}">
                    <div>
                         <stripes:submit name="delete" value="Delete" title="Delete selected scripts"/>
                         <stripes:submit name="activateDeactivate" value="Activate/deactivate" title="Activate/deactivate (i.e. turn on/off) selected scripts"/>
                         <c:if test="${not empty actionBean.scripts && fn:length(actionBean.scripts)>1}">
                            <stripes:submit name="moveUp" value="Move up" title="Move selected scripts up"/>
                            <stripes:submit name="moveDown" value="Move down" title="Move selected scripts down"/>
                         </c:if>
                         <input type="button" onclick="toggleSelectAll('scriptsForm');return false" value="Select all" name="selectAll"/>
                         <stripes:submit name="cut" value="Cut" title="Cut selected scripts"/>
                         <stripes:submit name="copy" value="Copy" title="Copy selected scripts"/>
                         <c:if test="${actionBean.pastePossible}">
                            <stripes:submit name="paste" value="Paste" title="Paste selected ${fn:length(actionBean.clipBoardScripts)} script(s): ${actionBean.clipBoardScripts}"/>
                        </c:if>
                    </div>
                </c:if>
        <div>
                <c:if test="${not empty actionBean.targetType}">
                    <stripes:hidden name="targetType"/>
                </c:if>
                <c:if test="${not empty actionBean.targetUrl}">
                    <stripes:hidden name="targetUrl"/>
                </c:if>
        </div>
            </crfn:form>
        </div>
    </c:if>

    <c:if test="${empty actionBean.scripts && (empty actionBean.targetType || (not empty actionBean.targetUrl && not empty actionBean.targets))}">
        <div style="margin-top:3em">No scripts found! Use operations menu to create one.</div>
    </c:if>

</stripes:layout-definition>
