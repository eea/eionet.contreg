<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Documentation">
    <stripes:layout-component name="contents">
    	<h1>${actionBean.title}</h1>
		${actionBean.content}
	</stripes:layout-component>
</stripes:layout-render>
