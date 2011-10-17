<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Post-harvest scripts">

<stripes:layout-component name="head">

    <script type="text/javascript">

            ( function($) {
                $(document).ready(function(){

                    if ($("#harvestSource").length > 0){

                        $('#harvestSource').click(function(){
                            if (this.value='Type at least 4 characters for suggestions ...'){
                                this.value='';
                            }
                        });
                        $('#harvestSource').focus(function(){
                            if (this.value='Type at least 4 characters for suggestions ...'){
                                this.value='';
                            }
                        });
                    }

                });
            } ) ( jQuery );

    </script>

</stripes:layout-component>

<stripes:layout-component name="contents">

    <c:if test="${empty sessionScope.crUser || !sessionScope.crUser.administrator}">
        <div class="error-msg">Access not allowed!</div>
    </c:if>

    <c:if test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">

    <c:choose>
	    <c:when test="${actionBean.id>0}">
            <h1>Edit post-harvest script</h1>
	    </c:when>
	    <c:otherwise>
            <h1>Add post-harvest script</h1>
        </c:otherwise>
    </c:choose>


            <div class="advice-msg" style="margin-top: 0.3em;margin-bottom: 0.3em;">
                <c:if test="${empty actionBean.targetType}">
                    Note: you're ${actionBean.id>0 ? 'editing' : 'adding'} an all-source script.
                </c:if>
                <c:if test="${not empty actionBean.targetType}">
                    Note: you're ${actionBean.id>0 ? 'editing' : 'adding'} a ${fn:toLowerCase(actionBean.targetType)}-specific script.
                </c:if>

            </div>

        <crfn:form action="${actionBean.urlBinding}" focus="first" method="post" style="padding-top:0.8em">

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
            <input type="hidden" name="ignoreMalformedSparql" value="${actionBean.ignoreMalformedSparql}"/>

        </crfn:form>

    </c:if>

</stripes:layout-component>

</stripes:layout-render>
