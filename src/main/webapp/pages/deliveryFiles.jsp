<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Save files into dataset">

       <stripes:layout-component name="head">
        <script type="text/javascript">
// <![CDATA[
            ( function($) {
                $(document).ready(function(){

                    $("#saveButton").click(function() {
                        var checkedVals = [];
                        $("#filesList :checked").each(function() {
                            checkedVals.push($(this).val());
                        });
                        if (checkedVals.length == 0) {
                            alert("No files are selected for merging");
                            return false;
                        }
                        return true;
                    });

                    var ds_val = $('select#selDataset').val();
                    //Hide div w/id extra
                    if(ds_val != "new_dataset") {
                        $("#newFile").css("display","none");
                    }

                    // Add onclick handler to checkbox w/id checkme
                    $("#selDataset").change(function(){
                        if($(this).val() != "new_dataset") {
                            $("#newFile").hide();
                        } else {
                            $("#newFile").show();
                        }
                    });
                });
            } ) ( jQuery );
// ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/mergedeliveries", "v")}'>
                <h1>Merge deliveries</h1>

                <c:if test="${not empty actionBean.deliveryFiles}">

                    <p class="documentDescription">
                        Below is the list of XML files in your selected deliveries.
                        Select the ones you wish to merge and proceed to the merging.
                        This will compile the selected files into a dataset within
                        your home folder.
                    </p>

                    <stripes:form action="/saveFiles.action" method="post" id="deliveryFilesForm">
                        <stripes:hidden name="searchCriteria" />
                        <div id="filesList">
                        <table border="0" width="100%" class="datatable">
                            <c:forEach items="${actionBean.deliveryFiles}" var="delivery" varStatus="cnt">
                                <tr>
                                    <td colspan="2">
                                        <b>
                                            <stripes:link href="/factsheet.action">
                                                <stripes:param name="uri" value="${delivery.uri}" />
                                                <c:out value="${delivery.title}"/>
                                            </stripes:link>
                                        </b>
                                        (<c:out value="${delivery.uri}"/>)
                                    </td>
                                    <td>
                                        <c:if test="${cnt.index == 0}">
                                            <b>Statements</b>
                                        </c:if>
                                    </td>
                                </tr>
                                <c:forEach items="${delivery.files}" var="file" varStatus="loop">
                                    <tr class="${loop.index % 2 == 0 ? 'odd' : 'even'}">
                                        <td width="20">
                                            <stripes:checkbox name="selectedFiles" value="${file.uri}" id="selFiles"/>
                                        </td>
                                        <td>
                                            <c:out value="${file.title}"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${file.triplesCnt > 0}">
                                                    <c:out value="${file.triplesCnt}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <span title="Number of statements is not available"><c:out value="N/A"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:forEach>
                            <tr>
                                <td colspan="3" align="left">
                                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAllForField('deliveryFilesForm','selectedFiles');return false"/>
                                </td>
                            </tr>
                        </table>
                        </div>
                        <table border="0" width="550">
                            <tr>
                                <td width="155">
                                    <stripes:label for="selDataset" class="question">Existing datasets</stripes:label>
                                </td>
                                <td>
                                    <stripes:select name="dataset" style="width: 383px;" id="selDataset">
                                        <stripes:option value="new_dataset" label="- new dataset -" />
                                        <optgroup label="Recently compiled datasets">
                                            <c:forEach items="${actionBean.newestExistingDatasets}" var="ds" varStatus="loop">
                                                <stripes:option value="${ds.uri}" label="${crfn:removeHomeUri(ds.uri)} (${ds.label})" />
                                            </c:forEach>
                                        </optgroup>
                                        <optgroup label="Rest of the datasets">
                                            <c:forEach items="${actionBean.existingDatasets}" var="ds" varStatus="loop">
                                                <stripes:option value="${ds.uri}" label="${crfn:removeHomeUri(ds.uri)} (${ds.label})" />
                                            </c:forEach>
                                        </optgroup>
                                    </stripes:select>
                                </td>
                            </tr>
                        </table>
                         <div id="newFile">
                            <table border="0" width="550">
                                <tr>
                                    <td width="160">
                                        <stripes:label for="datasetId" class="required question">Dataset ID</stripes:label>
                                    </td>
                                    <td>
                                        <stripes:text name="datasetId" id="datasetId" size="58" title="Valid URI characters"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="160">
                                        <stripes:label for="datasetTitle">Dataset title</stripes:label>
                                    </td>
                                    <td>
                                        <stripes:text name="datasetTitle" id="datasetTitle" size="58"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="160">
                                        <stripes:label for="folder" class="required question">Folder</stripes:label>
                                    </td>
                                    <td>
                                        <stripes:select name="folder" id="folder">
                                            <c:forEach items="${actionBean.folders}" var="f" varStatus="loop">
                                                <stripes:option value="${f}" label="${crfn:extractFolder(f)}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td>
                                        <stripes:checkbox name="overwrite" id="overwrite"/>
                                        <stripes:label for="overwrite" title="The existing dataset will be cleared and then reconstructed">Overwrite if file/dataset already exists</stripes:label>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <table border="0" width="550">
                            <tr>
                                <td align="right">
                                    <stripes:hidden name="selectedDeliveries" value="${actionBean.selectedDeliveries}"/>
                                    <stripes:submit id="saveButton" name="save" value="Save dataset"/>
                                </td>
                            </tr>
                        </table>
                    </stripes:form>
                </c:if>
                <c:if test="${empty actionBean.deliveryFiles}">
                    <p class="important-msg">
                        Found no XML files files in your selected deliveries!
                        Only XML files are currently supported for this operation.
                    </p>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="note-msg">You are not logged in or you do not have enough privileges!</div>
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
