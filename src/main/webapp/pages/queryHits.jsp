<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.util.Util"%>

<%@page import="java.util.*" %>
<%@page import="java.io.*" %>
<%@page import="eionet.cr.util.URLUtil" %>
<%@page import="eionet.cr.search.Searcher" %>
<%@page import="eionet.cr.index.EncodingSchemes" %>
<%@page import="eionet.cr.common.Identifiers" %>

<%!static final String[] fieldsOrder = {
	Identifiers.DOC_ID,
	"http://purl.org/dc/elements/1.1/title",
	"http://purl.org/dc/elements/1.1/coverage",
	"http://purl.org/dc/elements/1.1/date",
	"http://purl.org/dc/elements/1.1/language"
};
%>

<%@ include file="/pages/common/taglibs.jsp"%>	

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
						Map hash = (Map)hits.get(i);
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
									String fieldNameDisplay = k>0 ? "" : EncodingSchemes.getLabel(fieldName, true);
									%>
									<tr>
										<td style="background-color:#CCFFFF"><%=fieldNameDisplay%></td>
										<%
										if (URLUtil.isURL(fieldValues[k])){
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
							
							Iterator keys = hash.keySet().iterator();
							while (keys!=null && keys.hasNext()){
								String fieldName =  (String)keys.next();
								if (!displayed.contains(fieldName)){
									String[] fieldValues = (String[])hash.get(fieldName);
									for (int k=0; fieldValues!=null && k<fieldValues.length; k++){
										String fieldNameDisplay = k>0 ? "" : EncodingSchemes.getLabel(fieldName, true);
										%>
										<tr>
											<td style="background-color:#CCFFFF"><%=fieldNameDisplay%></td>
											<%
											if (URLUtil.isURL(fieldValues[k])){
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