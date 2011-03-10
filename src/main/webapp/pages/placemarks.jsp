<%@ page contentType="application/vnd.google-earth.kml+xml; charset=UTF-8" %><%--
--%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%--
--%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%><%--
--%><%@ taglib prefix="crfn" uri="http://cr.eionet.europa.eu/jstl/functions" %><%--
--%><?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.0">
<Document>

    <c:choose>
        <c:when test="${empty actionBean.source}">
    <name>All spatial things</name>
    <description>All spatial things found in CR</description>
        </c:when>
        <c:otherwise>
    <name>Spatial things from ${actionBean.source}</name>
    <description>Spatial things that CR has from ${actionBean.source}</description>
        </c:otherwise>
    </c:choose>

    <c:set var="colorForSource" value="${crfn:colorForSource(actionBean.source)}"/>

    <Style id="placemarksStyle1">
        <IconStyle>
            <color>${colorForSource}</color>
            <colorMode>normal</colorMode>
             <scale>1.0</scale>
             <Icon>
                <href>root://icons/palette-4.png</href>
            </Icon>
         </IconStyle>
        <LabelStyle>
            <color>${colorForSource}</color>
            <colorMode>normal</colorMode>
             <scale>1.0</scale>
        </LabelStyle>
    </Style>

    <c:if test="${not empty actionBean.resultList}">

        <c:set var="rdfType" value="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"/>
        <c:set var="rdfsLabel" value="http://www.w3.org/2000/01/rdf-schema#label"/>
        <c:set var="wgsLatitude" value="http://www.w3.org/2003/01/geo/wgs84_pos#lat"/>
        <c:set var="wgsLongitude" value="http://www.w3.org/2003/01/geo/wgs84_pos#long"/>

        <c:forEach items="${actionBean.resultList}" var="subject" varStatus="resultListStatus">

            <c:set var="subjectLatitude" value="${crfn:getObjectLiteral(subject, wgsLatitude)}"/>
            <c:set var="subjectLongitude" value="${crfn:getObjectLiteral(subject, wgsLongitude)}"/>

            <c:if test="${not empty subjectLatitude && not empty subjectLongitude}">

                <c:set var="subjectLabel" value="${subject.label}"/>
                <c:set var="subjectType" value="${crfn:getObjectLiteral(subject, rdfType)}"/>
                <c:set var="factsheetUrl" value="${actionBean.contextUrl}/factsheet.action?uri=${crfn:urlEncode(subject.uri)}"/>

                <Placemark id="${subject.uriHash}">
                    <c:choose>
                        <c:when test="${not empty subjectLabel}">
                            <name><c:out value="${subjectLabel}"/></name>
                        </c:when>
                        <c:otherwise>
                            <name><c:out value="${subject.uri}"/></name>
                        </c:otherwise>
                    </c:choose>
                    <styleUrl>#placemarksStyle1</styleUrl>
                    <description>
                        <![CDATA[
                        <div>
                            <c:if test="${not empty subjectType}">
                                <p><strong>Type:</strong>&nbsp;<c:out value="${subjectType}"/></p>
                            </c:if>
                            <p><strong>Factsheet:</strong>&nbsp;<a href="${factsheetUrl}">${factsheetUrl}</a></p>
                        </div>
                        ]]>
                    </description>
                    <open>0</open>
                    <Point>
                        <coordinates>${subjectLongitude},${subjectLatitude}</coordinates>
                    </Point>
                </Placemark>
            </c:if>

        </c:forEach>
    </c:if>

</Document>
</kml>
