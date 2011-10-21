<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit documentation">
    <stripes:layout-component name="contents">
        <h1>Edit documentation</h1>
        <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
            <stripes:form action="/documentation" method="post">
                <table border="0" cellpadding="3">
                    <tr>
                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
                        <td>
                            ${actionBean.pageId}
                            <stripes:hidden name="pid" value="${actionBean.pageId}"/>
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
                    <c:if test='${actionBean.editableContent}'>
	                    <tr>
	                        <td valign="top"><stripes:label class="question" for="content">Content</stripes:label></td>
	                        <td>
	                            <stripes:textarea id="content" name="content" cols="50" rows="10"/>
	                        </td>
	                    </tr>
                    </c:if>
                    <tr>
                        <td><stripes:label class="question" for="file">File</stripes:label></td>
                        <td><stripes:file name="file" id="file" size="54" /></td>
                    </tr>
                    <tr>
                        <td colspan="2" align="right">
                            <stripes:submit name="editContent" value="Save" />
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
