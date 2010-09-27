<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	<h1>Export triples</h1>
	<p>Url: ${ actionBean.url }</p>

	
	<crfn:form action="/download.action" method="get">
	<stripes:hidden name="exportSource">${ actionBean.url }</stripes:hidden>
		<table class="formtable">
			<tr>
				<td>
					<stripes:radio id="toFile" name="exportSelect" value="toFile" checked="toFile" title="To File"/>
					<stripes:label for="toFile">To File</stripes:label>
				</td>
			</tr>
			<tr>
				<td>
					<stripes:radio id="toHomespace" name="exportSelect" value="toHomespace" disabled="true"/>
					<stripes:label for="toHomespace">To Homespace</stripes:label>
				</td>
			</tr>
			
			<tr>
				<td>
					<stripes:submit name="export" value="Export" id="exportButton"/>
				</td>
			</tr>
		</table>
	</crfn:form>
	</stripes:layout-component>
	
</stripes:layout-render>