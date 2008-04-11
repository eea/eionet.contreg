<%@page contentType="text/html;charset=UTF-8"%>

<%@page import="eionet.cr.search.Searcher"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Query">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
		<jsp:useBean id="queryBean" scope="page"
                     class="eionet.cr.web.action.QueryActionBean"/>
	
		<h1>Query</h1>
		
		<stripes:form action="/luceneQuery.action" focus="query" style="padding-bottom:20px">
			<label for="query">Enter your Lucene query here:</label>
			<br/>
			<br/>
			<stripes:textarea name="query" cols="100"/>
			<br/>
			<%
			String[] analyzers = Searcher.listAvailableAnalyzers();
			if (analyzers!=null && analyzers.length>0){
				%>
				<select name="analyzer">
					<%
					String requestedAnalyzer = (String)request.getAttribute("analyzer");
					for (int i=0; i<analyzers.length; i++){
						String selected = requestedAnalyzer!=null && requestedAnalyzer.equals(analyzers[i]) ? "selected=\"selected\"" : "";
						%>
						<option value="<%=analyzers[i]%>" <%=selected%>><%=analyzers[i]%></option><%
					}
					%>
				</select>
				<%
			}
			%>
			<br/>
			<br/>
			<stripes:submit name="search" value="Query"/>
		</stripes:form>
		
		<c:import url="/pages/genericHits.jsp"/>
		
	</stripes:layout-component>
</stripes:layout-render>
