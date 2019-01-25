<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Bulk add/delete/check harvest sources">

    <stripes:layout-component name="contents">

        <h1>Bulk add/delete/check harvest sources</h1>

        <div style="margin-top:15px">
            <stripes:form beanclass="${actionBean['class'].name}" method="post">
                <div>
                    <label for="sourceUrlsTextArea" class="question">Harvest sources to add/delete/check:</label>
                    <textarea name="sourceUrlsString" id="sourceUrlsTextArea" rows="20" cols="80" style="display:block; width:100%"><c:out value="${actionBean.sourceUrlsString}"/></textarea>
                </div>
                <div style="position: relative; margin-bottom:30px">
                    <div style="position: absolute; top:5px; right:0px;">
                        <stripes:submit name="add" value="Add/harvest" id="addButton" title="Harvests the given sources, creates them first if they don't exist yet"/>
                        <stripes:submit name="delete" value="Delete" id="deleteButton" title="Deletes the given sources. Non-exisiting sources ignored."/>
                        <stripes:submit name="check" value="Check" id="deleteButton" title="Checks if the given sources are up-to-date. Detailed feedback will be displayed."/>
                    </div>
                </div>
                <c:if test="${actionBean.context.eventName eq 'check' && not empty actionBean.checkRemarks}">
                    <div>
                        <h5>Specific remarks about some of the above URLs:</h5>
                        <ul style="list-style:none;margin:0;padding:0;font-size:0.8em">
                            <c:forEach items="${actionBean.checkRemarks}" var="remark">
                                <li><c:out value="${remark.key}"/><span style="color:#7A7A7A;padding-left:20px">(<c:out value="${remark.value}"/>)</span></li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
