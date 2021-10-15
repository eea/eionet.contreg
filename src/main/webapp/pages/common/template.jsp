<%@ page import="eionet.cr.config.GeneralConfig" %>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>
	<c:choose>
		<c:when test="${actionBean.eeaTemplate or GeneralConfig.getProperty('useEeaTemplate')}">
    		<stripes:layout-render name="/pages/common/templateEea.jsp" pageTitle="${pageTitle}"/>
	    </c:when>
    	<c:otherwise>
			<stripes:layout-render name="/pages/common/templateEionet.jsp" pageTitle="${pageTitle}"/>
		</c:otherwise>
	</c:choose>
</stripes:layout-definition>