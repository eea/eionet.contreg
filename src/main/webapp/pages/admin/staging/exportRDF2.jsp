<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>RDF export: step 2</h1>

        <div style="margin-top:20px">
            Your query has been compiled on the database side, and the following selected columns have been detected.<br/>
            For each column, please specify a mapping to the corresponding RDF property.<br/>
            Also, please specify the column that denotes the target dataset where the query's returned objects will go to.<br/>
            You will also need to specify the template for the returned objects' unique identifier.<br/>
            Defaults have been selected by the system where possible.<br/>
            Mandatory inputs are marked with <img src="http://www.eionet.europa.eu/styles/eionet2007/mandatory.gif"/>.
        </div>

        <%-- The form --%>

        <div style="padding-top:20px">
            <crfn:form id="form1" beanclass="${actionBean.class.name}" method="post">
                <fieldset>
                    <legend style="font-weight:bold">The query:</legend>
                    <pre style="font-size:0.75em;max-height:130px;overflow:auto"><c:out value="${actionBean.queryConf.query}" /></pre>
                </fieldset>
                <fieldset style="margin-top:20px">
                    <legend style="font-weight:bold">The mapping of columns to RDF properties:</legend>
                    <table>
                        <c:forEach items="${actionBean.queryConf.columnMappings}" var="colMapping">
                            <tr>
                                <td style="text-align:right">
                                    <label for="${colMapping.key}.propertySelect" class="required"><c:out value="${colMapping.key}"/>:</label>
                                </td>
                                <td>
                                    <stripes:select id="${colMapping.key}.propertySelect" name="${colMapping.key}.property" value="${colMapping.value.predicate}">
                                        <stripes:option value="" label=""/>
                                        <c:forEach items="${actionBean.typeProperties}" var="typeProperty">
                                            <stripes:option value="${typeProperty.predicate}" label="${typeProperty.label}"/>
                                        </c:forEach>
                                    </stripes:select>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </fieldset>
                <fieldset style="margin-top:20px">
                    <legend style="font-weight:bold">Other settings:</legend>
                    <table>
                        <tr>
                            <td style="text-align:right">
                                <label for="datasetColumnSelect" title="The column whose value denotes the target dataset where the query's returned objects will go to." class="required">Dataset column:</label>
                            </td>
                            <td>
                                <stripes:select id="datasetColumnSelect" name="queryConf.datasetColumn" value="${actionBean.queryConf.datasetColumn}">
                                    <stripes:option value="" label=""/>
                                    <c:forEach items="${actionBean.queryConf.columnMappings}" var="colMapping">
                                        <stripes:option value="${colMapping.key}" label="${colMapping.key}"/>
                                    </c:forEach>
                                </stripes:select>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right">
                                <label for="txtObjIdTemplate" title="Template for the returned objects' unique identifier." class="required">Identifier template:</label>
                            </td>
                            <td>
                                <stripes:text name="queryConf.objectIdTemplate" size="80" id="txtObjIdTemplate"/>
                            </td>
                        </tr>
                    </table>
                </fieldset>
                <div style="margin-top:20px">
                    <stripes:submit name="backToStep1" value="< Back"/>&nbsp;
                    <stripes:submit name="step2" value="Run"/>&nbsp;
                    <stripes:submit name="cancel" value="Cancel"/>
                </div>
            </crfn:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
