<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Upload content file">

	<stripes:layout-component name="contents">
	
			<h1>Upload content file</h1>
			
		    <crfn:form action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/uploads" method="post">
				<table>
					<col style="width:10em"/>
					<col style="width:100%"/>
					<tr>
						<td><label class="question required" for="txtTitle">Title</label></td>
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
							<stripes:submit name="add" value="Upload"/>
							<input type="button" name="cancel" value="Cancel" onclick="history.go(-1);" />
						</td>       
					</tr>
				</table>
			</crfn:form>
	
	</stripes:layout-component>
	
</stripes:layout-render>