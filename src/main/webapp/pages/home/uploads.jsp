<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User Home">
<stripes:layout-component name="contents">
 
	<c:if test="${ actionBean.userAuthorized}">
	<ul id="dropdown-operations">
		<li><a href="#">Operations</a>
			<ul>
				<li>
					<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/uploads?add=">Upload file</stripes:link>
				</li>
				<li>
					<stripes:link class="link-plain" href="/factsheet.action?edit=&uri=${actionBean.user.homeUri}" title="Edit your home url properties">
					Edit folder
					</stripes:link>
				</li>
			</ul>
		</li>
	</ul>
	</c:if>
	<c:if test="${ actionBean.userAuthorized}">
	<h1>My files</h1>
		<crfn:form id="uploadsForm" action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/uploads" method="post">
		
			<display:table name="${actionBean.uploads}" class="sortable"
							pagesize="20" sort="list" id="uploadDTO" htmlId="uploads"
							requestURI="${actionBean.urlBinding}" style="width:100%">
							
				<display:column title="" sortable="false" style="width:1em">
					<input type="checkbox" value="${uploadDTO.subjectUri}" name="subjectUris" />
				</display:column>
				<display:column title="Title" sortable="false">
					<stripes:link href="/factsheet.action">
						${uploadDTO.label}
						<stripes:param name="uri" value="${uploadDTO.subjectUri}"/>
					</stripes:link>
				</display:column>
				<display:column title="Modified" sortable="false" style="width:10em">
					${uploadDTO.dateModified}
				</display:column>
							
			</display:table>
				<div>
					<stripes:submit name="delete" value="Delete" title="Delete selecetd files"/>
					<stripes:submit name="rename" value="Rename" title="Rename selecetd file"/>
					<input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('uploadsForm');return false"/>
				</div>
			
		</crfn:form>
	</c:if>
	
	<c:if test="${ !actionBean.userAuthorized}">
		<h1>${actionBean.attemptedUserName}'s files</h1>
		<display:table name="${actionBean.uploads}" class="sortable"
							pagesize="20" sort="list" id="uploadDTO" htmlId="uploads"
							requestURI="${actionBean.urlBinding}" style="width:100%">
			<display:column title="Title" sortable="false">
				<stripes:link href="/factsheet.action">
					${uploadDTO.label}
					<stripes:param name="uri" value="${uploadDTO.subjectUri}"/>
				</stripes:link>
			</display:column>
			<display:column title="Modified" sortable="false" style="width:10em">
				${uploadDTO.dateModified}
			</display:column>
		</display:table>
	</c:if>
	
</stripes:layout-component>
</stripes:layout-render>
