<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

    <stripes:layout-component name="contents">

        <h1>Google Earth network link</h1>
        <p>
        This page contains a link that opens Google Earth with layers of all the spatial objects found in CR.<br/>
        Each layer represents one distinct source where the objects were found from.<br/>
        The prerequisite is that you have to have Google Earth installed on your machine. If you don't then you can<br/>
        download it from the <a class="link-external" href="http://earth.google.com/download-earth.html">Google Earth website</a>.
        </p>
        <p style="text-align:center; margin-top:2em;">
            <a href="spatialSearch.action?kmlLinks=">Launch Google Earth</a>.
        </p>

    </stripes:layout-component>
</stripes:layout-render>
