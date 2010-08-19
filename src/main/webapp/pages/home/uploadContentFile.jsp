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
						<td><label class="question required" for="title">Title</label></td>
						<td><stripes:text id="contentFileTitle" name="title" size="80"/></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><stripes:text id="objecturl" name="review.objectUrl" size="80">${ param.addUrl }</stripes:text></td>
					</tr>
					<tr>
						<td><label class="question" for="reviewcontent">Review content</label></td>
						<td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10"></stripes:textarea></td>
					</tr>
					<tr>
						<td colspan="2">
							<stripes:submit name="addSave" value="Add review"/>       
						</td>
					</tr>
				</table>
			</crfn:form>
	
	</stripes:layout-component>
	
</stripes:layout-render>