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
        <c:choose>
            <c:when test='${sessionScope.crUser != null}'>
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
                        <c:choose>
                            <c:when test='${actionBean.fileUploaded == false}'>
                                <crfn:form action="/uploadCSV.action" method="post">
                                    <table width="360" border="0" cellpadding="3">
                                        <tr>
                                            <td><label class="question" for="type">File type</label></td>
                                            <td>
                                                <stripes:select name="type" style="width: 280px;">
                                                    <stripes:option value="csv" label="CSV - Comma separated values" />
                                                    <stripes:option value="tsv" label="TSV - Tab separated values" />
                                                </stripes:select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label class="question required" for="file">File</label></td>
                                            <td><stripes:file name="file" id="file" size="30" /></td>
                                        </tr>
                                        <tr>
                                            <td colspan="2" align="right"><stripes:submit name="upload" onclick="javascript:uploadFile();" value="Upload" /></td>
                                        </tr>
                                    </table>
                                </crfn:form>
                            </c:when>
                            <c:otherwise>
                                <crfn:form action="/uploadCSV.action" method="post">
                                    <table width="360" border="0" cellpadding="3">
                                        <tr>
                                            <td><label class="question">File type</label></td>
                                            <td>${actionBean.type}</td>
                                        </tr>
                                        <tr>
                                            <td><label class="question">File</label></td>
                                            <td>${actionBean.fileName}</td>
                                        </tr>
                                        <tr>
                                            <td><label class="question required" for="object_type">Object type</label></td>
                                            <td><input type="text" name="objectType" value="${actionBean.objectType}" id="object_type" size="34" /></td>
                                        </tr>
                                        <tr>
                                            <td><label class="question" for="label">Label column</label></td>
                                            <td>
                                                <option value="-1"></option>
                                                <stripes:select name="labelColumn" style="width: 230px;">
                                                    <c:forEach var="col" items="${actionBean.columns}" varStatus="loop">
                                                        <stripes:option value="${loop.index}" label="${col}" />
                                                    </c:forEach>
                                                </stripes:select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><label class="question required" for="unique">Unique column</label></td>
                                            <td>
                                                <stripes:select name="uniqueColumns" multiple="true" size="6" style="width: 230px;">
                                                    <c:forEach var="col" items="${actionBean.columns}" varStatus="loop">
                                                        <stripes:option value="${loop.index}" label="${col}" />
                                                    </c:forEach>
                                                </stripes:select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2" align="right">
                                                <stripes:hidden name="filePath"/>
                                                <stripes:hidden name="fileName"/>
                                                <stripes:hidden name="columns"/>
                                                <stripes:hidden name="type"/>
                                                <stripes:submit name="insert" value="Insert"/>
                                            </td>
                                        </tr>
                                    </table>
                                </crfn:form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="note-msg">You are not logged in!</div>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
