<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>Add a new staging database</h1>

        <%-- The form. --%>

        <div style="padding-top:20px">
	        <crfn:form id="newDbForm" beanclass="${actionBean.class.name}" method="post">
	            <table>
	                <tr>
	                    <td style="text-align:right;vertical-align:top">
	                       <stripes:label for="fileInput" class="question required">File:</stripes:label>
	                    </td>
	                    <td>
	                       <stripes:file name="dbFile" id="fileInput" size="80"/>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="text-align:right;vertical-align:top">
	                       <stripes:label for="txtName" class="question required">Name:</stripes:label>
	                    </td>
	                    <td>
	                       <stripes:text name="dbName" id="txtName" size="80"/>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="text-align:right;vertical-align:top">
	                       <stripes:label for="txtDescription" class="question">Description:</stripes:label>
	                    </td>
	                    <td>
	                       <stripes:textarea name="dbDescription" cols="80" rows="5"/>
	                    </td>
	                </tr>
	                <tr>
	                    <td>&nbsp;</td>
	                    <td>
	                        <stripes:submit name="add" value="Submit"/>
	                        <stripes:submit name="backToDbList" value="Cancel"/>
	                    </td>
	                </tr>
	            </table>
	        </crfn:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
