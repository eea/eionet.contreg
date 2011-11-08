<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <div style="margin-top:20px">

        <h1>Rename files / folders</h1>

        <crfn:form action="/folder.action" method="post">
            <stripes:hidden name="uri" value="${actionBean.uri}"/>
            <table>
                <c:forEach var="item" items="${actionBean.renameItems}" varStatus="loop">
                    <stripes:hidden name="renameItems[${loop.index}].uri" value="${item.uri}"/>
                    <stripes:hidden name="renameItems[${loop.index}].type" value="${item.type}"/>
                    <stripes:hidden name="renameItems[${loop.index}].name" value="${item.name}"/>
                    <tr>
                        <td style="padding-right:20px">${item.name}</td>
                        <td>to:&nbsp;<stripes:text name="renameItems[${loop.index}].newName"  value="${item.newName}"/></td>
                    </tr>
                </c:forEach>
            </table>

            <div>
                <stripes:submit name="rename" value="OK"/>
                <stripes:submit name="view" value="Cancel"/>
            </div>
        </crfn:form>

        </div>

    </stripes:layout-component>

</stripes:layout-render>
