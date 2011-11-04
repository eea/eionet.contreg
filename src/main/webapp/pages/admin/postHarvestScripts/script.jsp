<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>

	<br/>
    <c:choose>
	    <c:when test="${actionBean.id>0}">
            <h4>Edit script:</h4>
	    </c:when>
	    <c:otherwise>
            <h4>Create script:</h4>
        </c:otherwise>
    </c:choose>

        <crfn:form action="${actionBean.urlBinding}" focus="first" method="post" style="padding-top:0.8em" onsubmit="formSubmit()">

            <table>
                <c:if test="${not empty actionBean.targetType}">
	                <tr>
	                    <td style="vertical-align:top;padding-right:0.3em;text-align:right">
	                        <label for="${actionBean.targetType=='SOURCE' ? 'harvestSource' : 'typeSelect'}" class="${empty actionBean.targetUrl ? 'required ' : ''}question">Target ${fn:toLowerCase(actionBean.targetType)}:</label>
		                </td>
		                <td>
							<c:choose>
	                            <c:when test="${not empty actionBean.targetUrl}">
	                                <stripes:link href="/factsheet.action">
	                                    <c:out value="${actionBean.targetUrl}"/>
	                                    <stripes:param name="uri" value="${actionBean.targetUrl}"/>
	                                </stripes:link>
	                            </c:when>
	                            <c:otherwise>
	                                <c:choose>
	                                    <c:when test="${actionBean.targetType=='SOURCE'}">
	                                        <stripes:text name="targetUrl" id="harvestSource" value="${empty actionBean.targetUrl ? 'Type at least 4 characters for suggestions ...' : actionBean.targetUrl}" size="80"/>
	                                    </c:when>
	                                    <c:otherwise>
	                                        <stripes:select name="targetUrl" id="typeSelect">
	                                            <stripes:option value="" label="-- select a type --"/>
	                                            <c:forEach items="${actionBean.typeUris}" var="typeUri">
	                                                <stripes:option value="${typeUri}" label="${typeUri}"/>
	                                            </c:forEach>
	                                        </stripes:select>
	                                    </c:otherwise>
	                                </c:choose>
	                            </c:otherwise>
	                        </c:choose>
		                </td>
		            </tr>
	            </c:if>
	            <tr>
                    <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                        <label for="titleText" class="required question">Title:</label>
                    </td>
                    <td>
                        <stripes:text name="title" id="titleText" size="80"/>
                    </td>
	            </tr>
                <tr>
                    <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                        <label for="scriptText" class="required question">Script:</label>
                    </td>
                    <td>
                        <stripes:textarea name="script" cols="80" rows="6"/>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                        <label for="activeCheckbox" class="question">Active:</label>
                    </td>
                    <td>
                        <stripes:checkbox name="active" id="activeCheckbox"/>
                    </td>
                </tr>
<%--
                <c:if test="${empty actionBean.targetType || actionBean.targetType=='TYPE'}">
                    <tr>
	                    <td style="vertical-align:top;padding-right:0.3em;text-align:right">
	                        <label for="harvestSource" class="question">Test source:</label>
	                    </td>
	                    <td>
	                        <stripes:text name="testSourceUrl" id="harvestSource" value="${empty actionBean.testSourceUrl ? 'Type at least 4 characters for suggestions ...' : actionBean.testSourceUrl}" size="80"/>
	                    </td>
                    </tr>
                </c:if>
--%>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <stripes:submit name="save" value="Save"/>
                        <stripes:submit name="save" value="Save & close"/>
<%--                        <stripes:submit name="test" value="Test"/> --%>
                        <stripes:submit name="cancel" value="Cancel"/>
                    </td>
                </tr>
            </table>

            <stripes:hidden name="id"/>
            <c:if test="${not empty actionBean.targetType}">
                <stripes:hidden name="targetType"/>
            </c:if>
            <c:if test="${not empty actionBean.targetUrl}">
                <stripes:hidden name="targetUrl"/>
            </c:if>
            <c:if test="${not empty actionBean.backToTargetUrl}">
            	<stripes:hidden name="backToTargetUrl"/>
            </c:if>
            <input type="hidden" name="ignoreMalformedSparql" value="${actionBean.ignoreMalformedSparql}"/>

        </crfn:form>

</stripes:layout-definition>
