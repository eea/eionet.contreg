<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Upload content file">

    <stripes:layout-component name="contents">

        <h1>Rename uploaded files</h1>

        <crfn:form action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/uploads" method="post">

            <table>
                <c:forEach var="subjectUri" items="${actionBean.subjectUris}">
                    <tr>
                        <td style="padding-right:20px">${fn:replace(fn:substringAfter(subjectUri, actionBean.uriPrefix), "%20", " ")}</td>
                        <td>to:&nbsp;<stripes:text name="${actionBean.fileNameParamPrefix}${crfn:spoHash(subjectUri)}" size="40"/></td>
                    </tr>
                </c:forEach>
            </table>

            <div>
                <c:forEach var="subjectUri" items="${actionBean.subjectUris}">
                    <input type="hidden" name="subjectUris" value="${subjectUri}"/>
                </c:forEach>
            </div>

            <div>
                <stripes:submit name="rename" value="OK"/>
                <stripes:submit name="view" value="Cancel"/>
            </div>
        </crfn:form>

    </stripes:layout-component>

</stripes:layout-render>
