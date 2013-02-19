<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        $("select[id$=propertySelect]").change(function() {
                            $(this).attr("title", $("option:selected",this).attr('title'));
                            return true;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>RDF export: step 2</h1>

        <div style="margin-top:20px">
            Your query has been compiled on the database side, and the following selected columns have been detected.<br/>
            For each column, please specify a mapping to the corresponding RDF property.<br/>
            If none of the selected columns is mapped to the "Indicator (code)" property, please also select an indicator from picklist.<br/>
            It is also mandatory to select the dataset where the query's returned objects will go into.<br/>
            Defaults have been selected by the system where possible.<br/>
            Mandatory inputs are marked with <img src="http://www.eionet.europa.eu/styles/eionet2007/mandatory.gif"/>. Conditional inputs are marked with <img src="${pageContext.request.contextPath}/images/conditional.gif"/>.
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
                                    <stripes:select name="${colMapping.key}.property" value="${colMapping.value.predicate}" title="${colMapping.value.hint}" id="${colMapping.key}.propertySelect">
                                        <stripes:option value="" label=""/>
                                        <c:forEach items="${actionBean.typeProperties}" var="typeProperty">
                                            <stripes:option value="${typeProperty.predicate}" label="${typeProperty.label}" title="${typeProperty.hint}"/>
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
                            <td style="text-align:right;vertical-align:top">
                                <label for="selIndicator" title="Indicator of the selected observations." style="padding-right: 12px;background: url(${pageContext.request.contextPath}/images/conditional.gif) center right no-repeat;">Indicator:</label>
                            </td>
                            <td>
                                <stripes:select id="selIndicator" name="queryConf.indicator" value="${actionBean.queryConf.indicator}">
                                    <stripes:option value="" label=""/>
                                    <c:forEach items="${actionBean.indicators}" var="indicator">
                                        <stripes:option value="${indicator.right}" label="${indicator.right}"/>
                                    </c:forEach>
                                </stripes:select>&nbsp;<span style="font-size:0.8em">(must be selected, unless indicator has been mapped to one of the selected columns above)</span>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right">
                                <label for="selDataset" title="The dataset where the selected observations will go into." class="required">Dataset:</label>
                            </td>
                            <td>
                                <stripes:select id="selDataset" name="queryConf.dataset">
                                    <stripes:option value="http://semantic.digital-agenda-data.eu/dataset/scoreboard" label="Unit C4 - Economic and statistical analysis"/>
                                </stripes:select>
                            </td>
                        </tr>
                    </table>
                </fieldset>
                <div style="margin-top:20px">
                    <stripes:submit name="backToStep1" value="< Back"/>&nbsp;
                    <stripes:submit name="step2" id="runButton" value="Run"/>&nbsp;
                    <stripes:submit name="cancel" value="Cancel"/>
                </div>
            </crfn:form>
        </div>



    </stripes:layout-component>
</stripes:layout-render>
