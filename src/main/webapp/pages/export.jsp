<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

		<stripes:layout-component name="head">
			<script type="text/javascript">
				// <![CDATA[
				    ( function($) {
				    	$(document).ready(function(){
	                        // Open dialog
	                        $("#exportSubmit").click(function() {
	                        	if ($('input[name=exportType]:checked').val() == "HOMESPACE") {
		                            $('#export_dialog').dialog('open');
		                            return false;
	                        	} else {
	                        		return true;
	                        	}
	                        });

	                        // Dialog setup
	                        $('#export_dialog').dialog({
	                            autoOpen: false,
	                            width: 500
	                        });

	                        // Close dialog
	                        $("#export_form_submit").click(function() {
	                            $('#export_dialog').dialog("close");
	                            return true;
	                        });
	                    });
					} ) ( jQuery );
					// ]]>
			</script>
	</stripes:layout-component>

    <stripes:layout-component name="contents">

        <h1>Export triples</h1>
        <p>Source URL: ${actionBean.harvestSource.url}</p>

        <crfn:form action="/source.action" method="get" id="exportForm">

            <stripes:hidden name="harvestSource.url"/>

            <table class="formtable">
                <tr>
                    <td>
                        <stripes:label for="toFile">To file</stripes:label>
                        <stripes:radio name="exportType" value="FILE" checked="FILE" title="To file" id="toFile"/>
                    </td>
                </tr>
                <c:if test="${actionBean.userLoggedIn}">
	                <tr>
	                    <td>
	                        <stripes:label for="toHomespace">To homespace</stripes:label>
	                        <stripes:radio name="exportType" value="HOMESPACE" id="toHomespace"/>
	                    </td>
	                </tr>
                </c:if>
                <tr>
                    <td>
                        <stripes:submit name="export" value="Export" id="exportSubmit"/>
                    </td>
                </tr>
            </table>
        </crfn:form>
        <c:if test="${actionBean.userLoggedIn}">
	        <div id="export_dialog" title="Export triples to homespace">
	            <crfn:form action="/source.action" method="post">
	                <stripes:hidden name="harvestSource.url"/>
	                <stripes:hidden name="exportType" value="HOMESPACE"/>
	                <fieldset style="border: 0px;">
	                    <label for="datasetName" style="width: 200px; float: left;">Dataset name</label><br/>
	                    <stripes:text name="datasetName" id="datasetName" size="40"/>
	                	<br/>
	                	<label for="folder" style="width: 200px; float: left;">Folder</label><br/>
	                	<stripes:select name="folder" id="folder" style="width: 200px; float: left;">
							<c:forEach items="${actionBean.folders}" var="f" varStatus="loop">
								<stripes:option value="${f}" label="${crfn:extractFolder(f)}" />
							</c:forEach>
						</stripes:select>
						<br/><br/>
	                    <stripes:checkbox name="overwriteDataset" id="overwriteDataset"/>
						<stripes:label for="overwriteDataset">Overwrite if file/dataset already exists</stripes:label>
	                </fieldset>
	                <stripes:submit name="export" value="Export" id="export_form_submit" style="float: right;"/>
	            </crfn:form>
        	</div>
        </c:if>

    </stripes:layout-component>

</stripes:layout-render>
