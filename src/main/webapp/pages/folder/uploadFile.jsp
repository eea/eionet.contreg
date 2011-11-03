<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <div style="margin-top:20px">

        <div id="wait_container">
            <h1>Upload file</h1>

            <crfn:form action="/folder.action" method="post">
                <stripes:hidden name="uri" value="${actionBean.uri}"/>
                <table>
                    <col style="width:10em"/>
                    <col style="width:100%"/>
                    <tr>
                        <td><label for="txtTitle">Title</label></td>
                        <td><stripes:text id="txtTitle" name="title" size="80"/></td>
                    </tr>
                    <tr>
                        <td><label class="question required" for="fileToUpload">File to upload</label></td>
                        <td><stripes:file name="uploadedFile" id="fileToUpload" size="80"/></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <label for="chkReplaceExisting">Replace existing file of the same name</label>
                            <input type="checkbox" name="replaceExisting" id="chkReplaceExisting"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <stripes:submit name="upload" value="Upload"
                                onclick="javascript:showWait('The file is being uploaded, and its harvest is attempted. Please wait ...', '${pageContext.request.contextPath}');return true;"/>
                            <stripes:submit name="view" value="Cancel"/>
                        </td>
                    </tr>
                </table>
            </crfn:form>

        </div>

        </div>

    </stripes:layout-component>

</stripes:layout-render>
