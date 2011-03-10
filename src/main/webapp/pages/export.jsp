<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <h1>Export triples</h1>
        <p>Source URL: ${harvestSource.url}</p>

        <crfn:form action="/source.action" method="get">

            <stripes:hidden name="harvestSource.url"/>

            <table class="formtable">
                <tr>
                    <td>
                        <stripes:label for="toFile">To file</stripes:label>
                        <stripes:radio name="exportType" value="FILE" checked="FILE" title="To file" id="toFile"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <stripes:label for="toHomespace">To homespace</stripes:label>
                        <stripes:radio name="exportType" value="HOMESPACE" disabled="true" id="toHomespace"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <stripes:submit name="export" value="Export" id="exportSubmit"/>
                    </td>
                </tr>
            </table>

        </crfn:form>

    </stripes:layout-component>

</stripes:layout-render>
