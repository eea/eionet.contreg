<%@page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<%@page import="eionet.cr.util.Util"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error">
    <stripes:layout-component name="contents">
        <h1>Error</h1>
        <%
        Throwable exception = (Throwable)request.getAttribute("exception");
        if(exception != null){
            %>
            <h4>message:</h4>
            <p><%=exception.toString()%></p>
            <h4>stack trace:</h4>
            <p><%=Util.getStackTraceForHTML(exception)%></p>
            <%
        }
        else{
            %>
            <p>But no error message found!</p><%
        }
        %>
    </stripes:layout-component>
</stripes:layout-render>
