<%@ page contentType="application/vnd.google-earth.kml+xml; charset=UTF-8" %><%--
--%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%--
--%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%><%--
--%><%@ taglib prefix="crfn" uri="http://cr.eionet.europa.eu/jstl/functions" %><%--
--%><?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.0">
<Document>

	<name>CR spatial objects</name>
    <description><![CDATA[Shows layers of spatial objects found in CR]]></description>
    <visibility>1</visibility>
    <open>1</open>
    <LookAt>
      <longitude>15.0</longitude>
      <latitude>51.0</latitude>
      <altitude>0</altitude>
      <range>3500000</range>
      <tilt>0</tilt>
      <heading>0.0</heading>
    </LookAt>
    
	<c:if test="${not empty actionBean.sources}">
	
		<c:forEach items="${actionBean.sources}" var="currentSource" varStatus="sourcesStatus">
		
				<NetworkLink>
					<name><c:out value="${currentSource}"/></name>					
					<description><![CDATA[Spatial objects found from ${currentSource}]]></description>
					<Url>
						<href>${actionBean.contextUrl}${actionBean.urlBinding}?source=${currentSource}</href>
        				<viewRefreshMode>onStop</viewRefreshMode>
        				<viewRefreshTime>3</viewRefreshTime>					
					</Url>
				</NetworkLink>
			
		</c:forEach>
	</c:if>
	
</Document>
</kml>