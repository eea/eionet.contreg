<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User Home">
<stripes:layout-component name="contents">
	<p>This page is your home, ${ actionBean.attemptedUserName }</p>
</stripes:layout-component>
</stripes:layout-render>