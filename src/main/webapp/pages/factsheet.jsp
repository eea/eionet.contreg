<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	
        <h1>Resource factsheet</h1>        
	    <stripes:layout-render name="/pages/common/factsheet_layout.jsp" tableClass="sortable"/>
	    				
	</stripes:layout-component>
</stripes:layout-render>
