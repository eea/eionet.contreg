<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="contents">

        <h1>STEP1, ctx: <c:out value="${actionBean.ctx}"/></h1>

        <crfn:form id="newDbForm" beanclass="${actionBean.class.name}" method="post">
            <table>
                    <tr>
                        <td>step1Value:</td>
                        <td>
                           <stripes:text name="step1Value"/>
                        </td>
                    </tr>
                    <tr>
                        <td>step2Value:</td>
                        <td>
                           <c:out value="${actionBean.step2Value}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>step3Value:</td>
                        <td>
                           <c:out value="${actionBean.step3Value}"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <stripes:submit name="step2" value="Next &gt;"/>
                        </td>
                    </tr>
                </table>
        </crfn:form>

    </stripes:layout-component>
</stripes:layout-render>