<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add documentation">
    <stripes:layout-component name="contents">
        <h1>Add documentation</h1>
        <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
            <stripes:form action="/documentation" method="post">
                <table border="0" cellpadding="3">
                    <tr>
                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
                        <td>
                            <stripes:text id="page_id" name="pid" size="66"/>
                        </td>
                    </tr>
                    <tr>
                        <td><stripes:label class="question" for="page_title">Page title</stripes:label></td>
                        <td>
                            <stripes:text id="page_title" name="title" size="66"/>
                        </td>
                    </tr>
                    <tr>
                        <td><stripes:label class="question" for="content_type">Content type</stripes:label></td>
                        <td>
                            <stripes:text id="content_type" name="contentType" size="66"/>
                        </td>
                    </tr>
                    <tr>
                        <td><stripes:label class="question" for="file">File</stripes:label></td>
                        <td><stripes:file name="file" id="file" size="54" /></td>
                    </tr>
                    <tr>
                    	<td></td>
                        <td>
                            <stripes:checkbox name="overwrite" id="overwrite"/>
                            <stripes:label for="overwrite">Overwrite if file with the same name already exists</stripes:label>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="right">
                            <stripes:submit name="addContent" value="Add" />
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
