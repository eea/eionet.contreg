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

            <div>

                <div id="wizard" class="csvWizard">

                    <c:if test="${param.displayWizard==null}">
                        <crfn:form action="/uploadCSV.action" method="post">
                            <stripes:hidden name="folderUri"/>
                            <table width="360" border="0" cellpadding="3">
                                <tr>
                                    <td><label class="question required">File type</label></td>
                                    <td>
                                        <stripes:radio value="CSV" name="fileType" id="fileTypeCSV" checked="CSV" /><label for="fileTypeCSV">CSV - Comma separated values</label><br/>
                                        <stripes:radio value="TSV" name="fileType" id="fileTypeTSV"/><label for="fileTypeTSV">TSV - Tab separated values"</label>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label class="question required" for="fileInput">File</label></td>
                                    <td><stripes:file name="fileBean" id="fileInput" size="30" /></td>
                                </tr>
                                <tr>
                                    <td><label class="question">Overwrite</label></td>
                                    <td><stripes:checkbox name="overwrite" id="overwriteCheck" /><label for="overwriteCheck">Yes</label></td>
                                </tr>
                                <tr>
                                    <td colspan="2" align="right">
                                        <stripes:submit name="upload" onclick="javascript:uploadFile();" value="Upload" />
                                    </td>
                                </tr>
                            </table>
                        </crfn:form>
                        <div class="csvWizardDesc">
                            <h2>How to prepare your CSV/TSV file</h2>
                            <p>The import wizard will only work if you have <em>one</em> header line, which have simple column names without spaces.
                            Column names are case insensitive.
                            All values will be stored as <em>strings</em> unless you tell the wizard otherwise. You do this by appending a colon (:) and
                            a data type to the column name.
                            </p>
                            <p>
                            List of known data types:
                            </p>
                            <ul>
                                <li><b>number</b> - will be stored as xsd:int, xsd:long or xsd:double</li>
                                <li><b>boolean</b> - will be stored as xsd:boolean</li>
                                <li><b>date</b> - will be stored as xsd:date. Expects format: yyyy-MM-dd</li>
                                <li><b>datetime</b> - will be stored as xsd:dateTime. Expects format: yyyy-MM-dd'T'HH:mm:ss</li>
                                <li><b>url or uri</b> - will be stored as resource.</li>
                            </ul>
                            <p>Values that are the empty string will not be stored.
                            </p>
                        </div>
                    </c:if>

                    <c:if test="${param.displayWizard!=null}">
                        <crfn:form action="/uploadCSV.action" method="post">
                            <stripes:hidden name="overwrite" />
                            <table border="0" cellpadding="3">
                                <tr>
                                    <td><label class="question">File type</label></td>
                                    <td><c:out value="${actionBean.fileType}"/></td>
                                </tr>
                                <tr>
                                    <td><label class="question">File name</label></td>
                                    <td><c:out value="${actionBean.fileName}"/></td>
                                </tr>
                                <tr>
                                    <td><label class="question">Table title</label></td>
                                    <td><stripes:text name="fileLabel" size="80" /></td>
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
                                            <stripes:option value="" label="No label" />
                                            <c:forEach var="col" items="${actionBean.columns}">
                                                <stripes:option value="${col}" label="${col}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top"><label class="question required" for="uniqueColumnsSelect">Column(s) constituting a unique key</label></td>
                                    <td>
                                        <stripes:select name="uniqueColumns" id="uniqueColumnsSelect" value="${actionBean.uniqueColumns}" multiple="true" size="6" style="min-width: 230px;">
                                            <c:forEach var="col" items="${actionBean.columns}">
                                                <stripes:option value="${col}" label="${col}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label class="question required">Original publisher</label></td>
                                    <td><stripes:text name="publisher" size="80" /></td>
                                </tr>
                                <tr>
                                    <td><label class="question required" for="licenseSelect">Owner's license</label></td>
                                    <td><stripes:select name="license" id="licenseSelect">
                                            <stripes:option value="All rights reserved" label="© All rights reserved" />
                                            <stripes:option value="http://creativecommons.org/licenses/by-sa/3.0/" label="CC Attribution-ShareAlike 3.0 - Unported" />
                                            <stripes:option value="http://www.opendatacommons.org/licenses/by/" label="Open Data Commons Attribution (ODC-By)" />
                                            <stripes:option value="http://www.opendatacommons.org/licenses/odbl/" label="Open Database License (ODC-ODbL)" />
                                            <stripes:option value="other" label="Other open license" />
                                        </stripes:select><br/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label class="question required">Copyright attribution</label></td>
                                    <td><stripes:textarea name="attribution" cols="80" rows="3"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label class="question required">Source</label></td>
                                    <td><stripes:text name="source" size="80" /></td>
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

                        <div class="csvWizardDesc">
                            <h2>How to fill out the form</h2>
                            <ul>
                                <li>The Object type is an identifier for the table's type of content. Like a table name in a relational database.</li>
                                <li>The Label column is a representative field of the row, which will be seen a the link label when other objects links to the row.</li>
                            </ul>
                        </div>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${empty sessionScope.crUser}">
            <div class="note-msg">You are not authorized for this operation!</div>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
