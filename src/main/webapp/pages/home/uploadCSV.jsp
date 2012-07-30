<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Upload CSV/TSV file">

    <stripes:layout-component name="contents">

        <script type="text/javascript" xml:space="preserve">
            function uploadFile() {
                var event = "upload";
                var form = document.f;
                if (!form.onsubmit) {
                    form.onsubmit = function() {
                        return false
                    }
                }
                ;
                var params = Form.serialize(form, {
                    submit : event
                });
                new Ajax.Updater('wizard', form.action, {
                    method : 'post',
                    parameters : params
                });
            }
        </script>

        <h1>Upload CSV/TSV file</h1>

        <c:if test="${not empty sessionScope.crUser}">

            <div style="position: relative;">

                <div class="csvWizardDesc">
                    Import wizard recognizes some dataTypes by column names.<br />
                    List of known column names and their matching dataType:
                    <ul>
                        <li><b>Number</b> - will be stored as xsd:int, xsd:long or xsd:double</li>
                        <li><b>Boolean</b> - will be stored as xsd:boolean</li>
                        <li><b>Date</b> - will be stored as xsd:date. Expects format: yyyy-MM-dd</li>
                        <li><b>DateTime</b> - will be stored as xsd:dateTime. Expects format: yyyy-MM-dd'T'HH:mm:ss</li>
                        <li><b>URL or URI</b> - will be stored as resource.</li>
                    </ul>
                    All column names are case insensitive.<br />
                    All other column names are stored as literals without dataType.<br />
                </div>

                <div id="wizard" class="csvWizard">

                    <c:if test="${param.displayWizard==null}">
                        <crfn:form action="/uploadCSV.action" method="post">
                            <stripes:hidden name="folderUri"/>
                            <table width="360" border="0" cellpadding="3">
                                <tr>
                                    <td><label class="question" for="fileTypeSelect">File type</label></td>
                                    <td>
                                        <stripes:select name="fileType" id="fileTypeSelect" style="width: 280px;">
                                            <stripes:option value="CSV" label="CSV - Comma separated values" />
                                            <stripes:option value="TSV" label="TSV - Tab separated values" />
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label class="question required" for="fileInput">File</label></td>
                                    <td><stripes:file name="fileBean" id="fileInput" size="30" /></td>
                                </tr>
                                <tr>
                                    <td><label for="overwriteCheck">Overwrite</label></td>
                                    <td><stripes:checkbox name="overwrite" id="overwriteCheck" /></td>
                                </tr>
                                <tr>
                                    <td colspan="2" align="right">
                                        <stripes:submit name="upload" onclick="javascript:uploadFile();" value="Upload" />
                                    </td>
                                </tr>
                            </table>
                        </crfn:form>
                    </c:if>

                    <c:if test="${param.displayWizard!=null}">
                        <crfn:form action="/uploadCSV.action" method="post">
                            <stripes:hidden name="overwrite" />
                            <table width="360" border="0" cellpadding="3">
                                <tr>
                                    <td><label class="question">File type</label></td>
                                    <td><c:out value="${actionBean.fileType}"/></td>
                                </tr>
                                <tr>
                                    <td><label class="question">File</label></td>
                                    <td><c:out value="${actionBean.fileName}"/></td>
                                </tr>
                                <tr>
                                    <td><label class="question">File title</label></td>
                                    <td><stripes:text name="fileLabel" size="34" /></td>
                                </tr>
                                <tr>
                                    <td>
                                        <label class="question required" for="objectsTypeInput">Object type</label>
                                    </td>
                                    <td><stripes:text name="objectsType" id="objectsTypeInput" size="34" /></td>
                                </tr>
                                <tr>
                                    <td><label class="question" for="labelSelect">Label column</label></td>
                                    <td>
                                        <stripes:select name="labelColumn" id="labelSelect" value="${actionBean.labelColumn}" style="width: 230px;">
                                            <c:forEach var="col" items="${actionBean.columns}">
                                                <stripes:option value="${col}" label="${col}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top"><label class="question required" for="uniqueColumnsSelect">Column(s) constituting a unique key</label></td>
                                    <td>
                                        <stripes:select name="uniqueColumns" id="uniqueColumnsSelect" value="${actionBean.uniqueColumns}" multiple="true" size="6" style="width: 230px;">
                                            <c:forEach var="col" items="${actionBean.columns}">
                                                <stripes:option value="${col}" label="${col}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2" align="right">
                                        <stripes:hidden name="folderUri"/>
                                        <stripes:hidden name="relativeFilePath"/>
                                        <stripes:hidden name="fileType"/>
                                        <stripes:hidden name="fileName"/>
                                        <stripes:submit name="save" value="Save"/>
                                    </td>
                                </tr>
                            </table>
                        </crfn:form>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${empty sessionScope.crUser}">
            <div class="note-msg">You are not authorized for this operation!</div>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
