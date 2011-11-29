<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Bulk add/delete harvest sources">

    <stripes:layout-component name="contents">
    <c:choose>
        <c:when test="${actionBean.adminLoggedIn}">
            <h1>Bulk add/delete harvest sources</h1>
            <div style="margin-top:15px">
                <stripes:form action="/admin/bulkharvest" method="post">
                    <div>
                        <label for="sourceUrlsTextArea" class="question">Harvest sources to add/delete:</label>
                        <stripes:textarea name="sourceUrlsString" id="sourceUrlsTextArea" rows="20" cols="80" style="display:block; width:100%"/>
                    </div>
                    <div style="position: relative; margin-bottom:30px">
                        <div style="position: absolute; top:5px; right:0px;">
                            <stripes:submit name="add" value="Add" id="addButton"/>
                            <stripes:submit name="delete" value="Delete" id="deleteButton"/>
                        </div>
                    </div>
                </stripes:form>
            </div>
        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
