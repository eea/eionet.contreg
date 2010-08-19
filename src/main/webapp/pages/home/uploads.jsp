<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User Home">
<stripes:layout-component name="contents">
 
	<c:if test="${ actionBean.userAuthorized}">
	<ul id="dropdown-operations">
		<li><a href="#">Operations</a>
			<ul>
				<li>
					<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/uploads?uploadContentFile=">Upload content file</stripes:link>
				</li>
				<li>
					<stripes:link class="link-plain" href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}">
					Edit folder
					</stripes:link>
				</li>
			</ul>
		</li>
	</ul>
	</c:if>
	<h1>My files</h1>
	<form action="#" method="post">
	<table class="sortable" style="width:100%;">
		<col style="width:1em"/>
		<col/>
		<col style="width:10em"/>
		<tr><th></th><th>Title</th><th>Modified</th></tr>
		<tr class="zebraodd">
			<td><input type="checkbox" name="uris" value="1"/></td>
			<td><a href="#">File number 1 (rdfs:label shown)</a></td>
			<td>2010-08-17 09:24:17</a></td>
		</tr>
		<tr class="zebraeven">
			<td><input type="checkbox" name="uris" value="2"/></td>
			<td><a href="#">File_number_2.rdf</a></td>
			<td>2010-08-17 09:24:17</a></td>
		</tr>
	</table>
	<div>
	<input type="submit" name="delete" value="Delete"/>
	<input type="submit" name="rename" value="Rename"/>
	<input type="submit" name="rename" value="Edit"/>
	</div>
	</form>
	
</stripes:layout-component>
</stripes:layout-render>
