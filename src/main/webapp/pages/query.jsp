<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.util.Util"%>

<%@page import="eionet.cr.index.Searcher"%>

<%!
static final String[] fieldsOrder = {
	"ID",
	"http://purl.org/dc/elements/1.1/title",
	"http://purl.org/dc/elements/1.1/coverage",
	"http://purl.org/dc/elements/1.1/date",
	"http://purl.org/dc/elements/1.1/language"
};

static final String[] analyzers = {
	"org.apache.lucene.analysis.standard.StandardAnalyzer",
	"org.apache.lucene.analysis.WhitespaceAnalyzer",
	"org.apache.lucene.analysis.SimpleAnalyzer",
	"org.apache.lucene.analysis.StopAnalyzer"
};
%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Query">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
		<jsp:useBean id="queryBean" scope="page"
                     class="eionet.cr.web.action.QueryActionBean"/>
	
		<h1>Query</h1>
		<stripes:form action="/query.action" focus="query">
			<label for="query">Enter your Lucene query here:</label>
			<br/>
			<stripes:textarea name="query" cols="100"/>
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
		<%
		List hits = (List)request.getAttribute("hits");
		if (hits!=null && hits.size()>0){
			%>
			<div>
				<table>
					<%
					for (int i=0; i<hits.size(); i++){
						%>
						<tr><td colspan="2"><strong><%=i+1%></strong></td></tr>
						<%
						Hashtable hash = (Hashtable)hits.get(i);
						if (hash.size()==0){
							%>
							<tr><td colspan="2">Some document with no stored fields in it...</td></tr><%
						}
						else{
							HashSet displayed = new HashSet();
							for (int j=0; j<fieldsOrder.length; j++){
								String fieldName = fieldsOrder[j];
								String[] fieldValues = (String[])hash.get(fieldName);
								for (int k=0; fieldValues!=null && k<fieldValues.length; k++){
									String fieldNameDisplay = k>0 ? "" : fieldName;
									%>
									<tr>
										<td style="background-color:#CCFFFF"><%=fieldNameDisplay%></td>
										<%
										if (Util.isURL(fieldValues[k])){
											%>
											<td><a target="_blank" href="<%=fieldValues[k]%>"><%=fieldValues[k]%></a></td><%
										}
										else{
											%>
											<td><%=fieldValues[k]%></td><%
										}
										%>
									</tr>
									<%
								}
								displayed.add(fieldName);
							}
							
							Enumeration keys = hash.keys();
							while (keys!=null && keys.hasMoreElements()){
								String fieldName =  (String)keys.nextElement();
								if (!displayed.contains(fieldName)){
									String[] fieldValues = (String[])hash.get(fieldName);
									for (int k=0; fieldValues!=null && k<fieldValues.length; k++){
										String fieldNameDisplay = k>0 ? "" : fieldName;
										%>
										<tr>
											<td style="background-color:#CCFFFF"><%=fieldNameDisplay%></td>
											<%
											if (Util.isURL(fieldValues[k])){
												%>
												<td><a target="_blank" href="<%=fieldValues[k]%>"><%=fieldValues[k]%></a></td><%
											}
											else{
												%>
												<td><%=fieldValues[k]%></td><%
											}
											%>
										</tr>
										<%
									}
								}
							}
						}
						%>
						<tr><td colspan="2">------------------------------------------------------------------------------------------------------------</td></tr><%
					}
					%>
				</table>
			</div>
			<%
		}
		%>
	</stripes:layout-component>
</stripes:layout-render>
