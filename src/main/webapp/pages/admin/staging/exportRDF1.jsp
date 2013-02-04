<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>RDF export: step 1</h1>

        <div style="margin-top:20px">
            You have chosen to run an RDF export from database <stripes:link beanclass="${actionBean.databaseActionBeanClass.name}"><c:out value="${actionBean.dbName}"/><stripes:param name="dbName" value="${actionBean.dbName}"/></stripes:link>.<br/>
            As the first step, please type the SQL query whose results will be exported, and the type of objects that the query returns<br/>.
            Mandatory inputs are marked with <img src="http://www.eionet.europa.eu/styles/eionet2007/mandatory.gif"/>.
        </div>

        <%-- The form --%>

        <div style="padding-top:10px">
            <crfn:form id="form1" beanclass="${actionBean.class.name}" method="post">
                <table>
                   <tr>
                        <td style="text-align:right;vertical-align:top">
                            <stripes:label for="txtQuery" class="question required">Query:</stripes:label>
                        </td>
                        <td>
                            <stripes:textarea id="" name="queryConf.query" cols="80" rows="15"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:right;vertical-align:top">
                            <stripes:label for="selObjectsType" class="question required">Objects type:</stripes:label>
                        </td>
                        <td>
                            <stripes:select name="queryConf.objectTypeUri" id="selObjectType">
                                <c:forEach items="${actionBean.objectTypes}" var="objectType">
                                    <stripes:option value="${objectType.uri}" label="${objectType.label}"/>
                                </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <stripes:submit name="step1" value="Next >"/>
                            <stripes:submit name="cancel" value="Cancel"/>
                        </td>
                    </tr>
                </table>
            </crfn:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
