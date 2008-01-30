<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Kalkulaator">
	<stripes:layout-component name="contents">
		<h1>Calculator</h1>
		<style type="text/css">
		    input.error { background-color: yellow; }
		</style>
	    <stripes:form action="/calculator.action" focus="">
	    	<stripes:errors/>
	        <table>
	            <tr>
	                <td>Number 1:</td>
	                <td><stripes:text name="numberOne"/></td>
	            </tr>
	            <tr>
	                <td>Number 2:</td>
	                <td><stripes:text name="numberTwo"/></td>
	            </tr>
	            <tr>
	                <td colspan="2">
	                    <stripes:submit name="addition" value="Liida"/>       
	                    <stripes:submit name="division" value="Lahuta"/>             
	                </td>
	            </tr>
	            <tr>
	                <td>Result:</td>
	                <td>${actionBean.result}</td>
	            </tr>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
