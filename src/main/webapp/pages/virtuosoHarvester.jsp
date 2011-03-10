<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest">

    <stripes:layout-component name="contents">

        <h1>Virtuoso Harvest</h1>
        <stripes:form action="/virtuosoHarvester.action" method="post" name="f">
            <stripes:label for="source">Source URL:</stripes:label>
            <stripes:text name="sourceUrl" id="source" size="60"/>

            <stripes:submit name="harvest" value="Harvest" id="harvestButton"/>
        </stripes:form>


    </stripes:layout-component>
</stripes:layout-render>
