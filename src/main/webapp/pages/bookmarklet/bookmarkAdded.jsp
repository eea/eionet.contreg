<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Bookmark installed">

	<stripes:layout-component name="contents">
	
	

	<script type="text/javascript">
	function goBackWithDelay() {
		setTimeout('goBackToPage()', 5000);
	}
	
	function goBackToPage() {
		document.location = '${actionBean.originalPageUrl}';
	}
	addEvent(window,'load',goBackWithDelay);
	</script>
	
	
	<h1> Thank you, ${actionBean.userName} </h1>
	<form method="get" action="${actionBean.originalPageUrl}">
	<p>
	URL was successfully added to the system.
	<input type="submit" value="OK"/>
	</p>
	<p>
	In few seconds you'll be redirected back to the page you came from.
	</p>
	</form>
	
	</stripes:layout-component>
</stripes:layout-render>
