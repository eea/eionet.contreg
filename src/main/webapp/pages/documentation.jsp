<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Documentation">
    <stripes:layout-component name="contents">

            <c:if test='${crfn:userHasPermission(pageContext.session, "/documentation", "u")}'>
    			<div id="tabbedmenu">
	                <ul>
	                	<c:choose>
	                        <c:when test="${actionBean.event == 'edit' || actionBean.event == 'add' || actionBean.pageId == 'contents'}">
	                        	<li><stripes:link href="/documentation">View</stripes:link></li>
	                            <li id="currenttab"><stripes:link href="/documentation/contents">Contents</stripes:link></li>
	                        </c:when>
	                        <c:otherwise>
	                        	<li id="currenttab"><stripes:link href="/documentation">View</stripes:link></li>
	                        	<li><stripes:link href="/documentation/contents">Contents</stripes:link></li>
	                        </c:otherwise>
	                    </c:choose>
	                </ul>
            	</div>
            	<br style="clear:left" />
				<br style="clear:left" />
            </c:if>
			<c:choose>
           		<c:when test='${actionBean.event == "edit" && crfn:userHasPermission(pageContext.session, "/documentation", "u")}'>
		            <stripes:form action="/documentation" method="post">
		                <table border="0" cellpadding="3">
		                    <tr>
		                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
		                        <td>
		                            ${actionBean.pageId}
		                            <stripes:hidden name="pid" value="${actionBean.pageId}"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="page_title">Page title</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_title" name="title" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="content_type">Content type</stripes:label></td>
		                        <td>
		                            <stripes:text id="content_type" name="contentType" size="66"/>
		                        </td>
		                    </tr>
		                    <c:if test='${actionBean.editableContent}'>
			                    <tr>
			                        <td valign="top"><stripes:label class="question" for="content">Content</stripes:label></td>
			                        <td>
			                            <stripes:textarea id="content" name="content" cols="50" rows="10"/>
			                        </td>
			                    </tr>
		                    </c:if>
		                    <tr>
		                        <td><stripes:label class="question" for="file">File</stripes:label></td>
		                        <td><stripes:file name="file" id="file" size="54" /></td>
		                    </tr>
		                    <tr>
		                        <td colspan="2" align="right">
		                            <stripes:submit name="editContent" value="Save" />
		                        </td>
		                    </tr>
		                </table>
		            </stripes:form>
           		</c:when>
           		<c:when test='${actionBean.event == "add" && crfn:userHasPermission(pageContext.session, "/documentation", "u")}'>
		            <stripes:form action="/documentation" method="post">
		                <table border="0" cellpadding="3">
		                    <tr>
		                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_id" name="pid" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="page_title">Page title</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_title" name="title" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="content_type">Content type</stripes:label></td>
		                        <td>
		                            <stripes:text id="content_type" name="contentType" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="file">File</stripes:label></td>
		                        <td><stripes:file name="file" id="file" size="54" /></td>
		                    </tr>
		                    <tr>
		                    	<td></td>
		                        <td>
		                            <stripes:checkbox name="overwrite" id="overwrite"/>
		                            <stripes:label for="overwrite">Overwrite if file with the same name already exists</stripes:label>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td colspan="2" align="right">
		                            <stripes:submit name="addContent" value="Add" />
		                        </td>
		                    </tr>
		                </table>
		            </stripes:form>
           		</c:when>
           		<c:when test='${actionBean.pageId == "contents" && crfn:userHasPermission(pageContext.session, "/documentation", "u")}'>
           			<div id="operations">
	                    <ul>
	                        <li>
	                            <stripes:link href="/documentation">
	                            	Add new file
	                            	<stripes:param name="event" value="add"/>
	                            </stripes:link>
	                        </li>
	                    </ul>
	                </div>
           			<stripes:form action="/documentation" method="post">
	           			<table>
							<col/>
							<col style="width: 10em"/>
							<c:forEach var="doc" items="${actionBean.docs}">
								<tr>
									<td>
										<stripes:checkbox name="docIds" value="${doc.pageId}"/>
									</td>
									<td>
										<c:set var="doctitle" value="${doc.title}"/>
										<c:if test="${empty doctitle}">
											<c:set var="doctitle" value="${doc.pageId}"/>
										</c:if>
										<stripes:link href="/documentation/${doc.pageId}/edit">${doctitle}</stripes:link>
									</td>
								</tr>
							</c:forEach>
						</table>
						<div style="padding-top: 1em">
							<stripes:submit name="delete" value="Delete" />
						</div>
					</stripes:form>
           		</c:when>
   				<c:otherwise>
   					<c:choose>
						<c:when test="${not empty actionBean.pageId}">
							<h1>${actionBean.title}</h1>
							${actionBean.content}
						</c:when>
                        <c:otherwise>
                        	<h1>Documentation</h1>
		   					<ul>
			   					<c:forEach var="doc" items="${actionBean.docs}">
									<li>
										<c:set var="doctitle" value="${doc.title}"/>
										<c:if test="${empty doctitle}">
											<c:set var="doctitle" value="${doc.pageId}"/>
										</c:if>
										<stripes:link href="/documentation/${doc.pageId}">${doctitle}</stripes:link>
									</li>
								</c:forEach>
							</ul>
                        </c:otherwise>
                    </c:choose>
   				</c:otherwise>
   			</c:choose>
    </stripes:layout-component>
</stripes:layout-render>
