<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Post-harvest scripts">

<stripes:layout-component name="contents">

    <h1 style="padding-bottom:10px">Post-harvest script</h1>

    <c:if test="${empty sessionScope.crUser || !sessionScope.crUser.administrator}">
        <div class="error-msg">Access not allowed!</div>
    </c:if>

    <c:if test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">

        <crfn:form action="${actionBean.urlBinding}" focus="" method="post">

            <div style="padding-bottom:10px">
		        ${crfn:conditional(actionBean.targetType=="HARVEST_SOURCE", "Target harvest source:", "Target type:")}
		        <c:choose>
			        <c:when test="${empty actionBean.uri}">
			           <stripes:text name="uri" id="harvestSource" size="100" value="Type at least 4 characters for suggestions ..."/>
			        </c:when>
			        <c:otherwise>
			           <stripes:link href="/source.action" event="view">
			               <c:out value="${actionBean.uri}"/>
			               <stripes:param name="harvestSource.url" value="${actionBean.uri}"/>
			           </stripes:link>
			        </c:otherwise>
			    </c:choose>
		    </div>

	        <div id="tabbedmenu">
	            <ul>
	             <li id="currenttab"><span>Queries</span></li>
	             <li>
	                 <stripes:link href="${actionBean.urlBinding}">
	                     <c:out value="Test"/>
	                     <stripes:param name="targetType" value="RDF_TYPE"/>
	                     <stripes:param name="uri" value="${actionBean.uri}"/>
	                 </stripes:link>
	             </li>
	            </ul>
	        </div>

            <div style="padding-top:10px">
                <stripes:submit name="save" value="Save"/>
            </div>

            <c:choose>
                <c:when test="${empty actionBean.uri || fn:length(actionBean.queries)==0}">
                    <textarea name="query1" id="query1Text" rows="8" cols="80" style="clear:right; display: block; width: 100%"></textarea>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${actionBean.queries}" var="query" varStatus="queryLoopStatus">
                        <textarea name="query${queryLoopStatus.index+1}" id="query${queryLoopStatus.index+1}Text" rows="8" cols="80" style="clear:right; display: block; width: 100%"><c:out value="${query}"/></textarea>
                    </c:forEach>
                </c:otherwise>
            </c:choose>

            <stripes:hidden name="targetType" value="${actionBean.targetType}" />

	    </crfn:form>

    </c:if>

</stripes:layout-component>

</stripes:layout-render>
