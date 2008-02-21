<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,java.lang.*,eionet.cr.dao.DAOFactory"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

 <%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql" %>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error">
	<stripes:layout-component name="contents">
		<h1>Error</h1>
    	<%
    	Throwable exception = (Throwable)request.getAttribute("exception");
    	if(exception != null){
	    	StackTraceElement[] errors = exception.getStackTrace();
	    	for(int i = 0; i<errors.length; i++){
		    	String error = errors[i].toString();%>
		    	<%=error%><br/>
	    <%
	    	}
		}
    	%>
	</stripes:layout-component>
</stripes:layout-render>