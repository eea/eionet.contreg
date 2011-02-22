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
		<div id="tabbedmenu">
	    <ul>
	    <li>
			<c:choose>
				<c:when test="${not empty actionBean.uri}">
					<stripes:link href="/factsheet.action">Resource properties
						<stripes:param name="uri" value="${actionBean.uri}"/>
					</stripes:link>
				</c:when>
				<c:otherwise>
					<stripes:link href="/factsheet.action">Resource properties
						<stripes:param name="uriHash" value="${actionBean.anonHash}"/>
					</stripes:link>
				</c:otherwise>
			</c:choose>
		</li>
		<li>
			<c:choose>
				<c:when test="${not empty actionBean.subject && not empty actionBean.subject.uri && !actionBean.subject.anonymous}">
					<stripes:link href="/references.action" event="search">Resource references
						<stripes:param name="uri" value="${actionBean.subject.uri}"/>
					</stripes:link>
				</c:when>
				<c:when test="${not empty actionBean.uri}">
					<stripes:link href="/references.action" event="search">Resource references
						<stripes:param name="uri" value="${actionBean.uri}"/>
					</stripes:link>
				</c:when>
				<c:otherwise>
					<stripes:link href="/references.action" event="search">Resource references
						<stripes:param name="anonHash" value="${actionBean.uriHash}"/>
					</stripes:link>
				</c:otherwise>
			</c:choose>
        </li>
		<li id="currenttab">
				<span>Show on Map</span>
		</li>
	    </ul>
	</div>
	<br style="clear:left" />
	<div style="margin-top:20px">
		<div id="map" style="width:900px; height:400px; border:1px solid #000;"></div>
	</div>
	</stripes:layout-component>
</stripes:layout-render>
