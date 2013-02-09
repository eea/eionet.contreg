<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        // Open the tables and columns popup
                        $("#openTableAndColumnsPopup").click(function() {
                            $('#tableAndColumnsPopup').dialog('open');
                            return false;
                        });

                        // Setup the tables and columns popup
                        $('#tableAndColumnsPopup').dialog({
                            autoOpen: false,
                            height: 500,
                            width: 700,
                            maxHeight: 800,
                            maxWidth: 800
                        });

                        // Close the tables and columns popup
                        $("#closeTableAndColumnsPopup").click(function() {
                            $('#tableAndColumnsPopup').dialog("close");
                            return true;
                        });

                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <%-- The page's heading --%>

        <h1>RDF export: step 1</h1>

        <div style="margin-top:20px">
            You have chosen to run an RDF export from database <stripes:link beanclass="${actionBean.databaseActionBeanClass.name}"><c:out value="${actionBean.dbName}"/><stripes:param name="dbName" value="${actionBean.dbName}"/></stripes:link>.<br/>
            As the first step, please type the SQL query whose results will be exported, and select the type of objects that the query returns.<br/>
            Also please give this RDF export a descriptive name that will help you to distinguish it from others later.<br/>
            Mandatory inputs are marked with <img src="http://www.eionet.europa.eu/styles/eionet2007/mandatory.gif"/>.
        </div>

        <%-- The form --%>

        <div style="padding-top:10px">
            <crfn:form id="form1" beanclass="${actionBean.class.name}" method="post">
                <table>
                    <tr>
                        <td style="text-align:right;vertical-align:top">
                            <stripes:label for="txtName" class="question required">Name:</stripes:label>
                        </td>
                        <td>
                            <stripes:text id="txtName" name="exportName" size="80"/>&nbsp;&nbsp;<a href="#" id="openTableAndColumnsPopup">View tables and columns &#187;</a>
                        </td>
                    </tr>
                    <tr>
                         <td style="text-align:right;vertical-align:top">
                             <stripes:label for="txtQuery" class="question required">Query:</stripes:label>
                         </td>
                         <td>
                             <stripes:textarea id="txtQuery" name="queryConf.query" cols="80" rows="15"/>
                         </td>
                     </tr>
                    <tr>
                        <td style="text-align:right;vertical-align:top">
                            <stripes:label for="selObjectsType" class="question required">Objects type:</stripes:label>
                        </td>
                        <td>
                            <stripes:select name="queryConf.objectTypeUri" id="selObjectType">
                                <c:forEach items="${actionBean.objectTypes}" var="objectType">
                                    <stripes:option value="${objectType.uri}" label="${objectType.label}"/>
                                </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <stripes:submit name="step1" value="Next >"/>
                            <stripes:submit name="cancel" value="Cancel"/>
                        </td>
                    </tr>
                </table>
            </crfn:form>
        </div>

        <div id="tableAndColumnsPopup" title="Tables and columns in this database">
            <div style="height:90%;width:90%;background-color:#F0F0F0;">
                <div style="padding-top:10%;padding-left:20%;width:50%;height:100px">
                    <p>To be implemented:</p>
                    here the tables and columns of this database can be explored.
                </div>
            </div>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
