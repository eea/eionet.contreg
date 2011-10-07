<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Show on Map">
    <stripes:layout-component name="head">
        <meta http-equiv="X-UA-Compatible" content="IE=7" />
        <link rel="stylesheet" type="text/css" href="http://serverapi.arcgisonline.com/jsapi/arcgis/1.6/js/dojo/dijit/themes/tundra/tundra.css"/>
        <script type="text/javascript" src="http://serverapi.arcgisonline.com/jsapi/arcgis/?v=1.6"></script>
        <script type="text/javascript">
            loadMap();
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <div style="margin-top:20px">
            <div id="map" style="width:900px; height:400px; border:1px solid #000;"></div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
