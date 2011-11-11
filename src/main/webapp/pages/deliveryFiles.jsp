<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Save files into dataset">

   	<stripes:layout-component name="head">
		<script type="text/javascript">
// <![CDATA[
			( function($) {
				$(document).ready(function(){
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
			<c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
		        <h1>Save files into dataset</h1>
				<p class="documentDescription">
					Select multiple files and store them into one compiled dataset under your home-folder.
					The list will only show files that contain at least one triple. If the file you are
					looking for isn't in the list then it is probably not harvested yet or it isn't XML or RDF file.
				</p>

				<c:if test="${not empty actionBean.deliveryFiles}">
					<stripes:form action="/saveFiles.action" method="post">
			        	<table border="0" width="100%" class="datatable">
				        	<c:forEach items="${actionBean.deliveryFiles}" var="delivery" varStatus="cnt">
				        		<tr>
				        			<td colspan="2">
				        				<b><c:out value="${delivery.uri}"/></b>
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
				        					<stripes:checkbox name="selectedFiles" value="${file.uri}"/>
				        				</td>
				        				<td>
				        					<c:out value="${file.title}"/>
				        				</td>
				        				<td>
				        					<c:if test="${file.triplesCnt > 0}">
				        						<c:out value="${file.triplesCnt}"/>
				        					</c:if>
				        				</td>
				        			</tr>
				        		</c:forEach>
				        	</c:forEach>
			        	</table>
						<table border="0" width="550">
							<tr>
								<td width="155">
									<stripes:label for="selDataset" class="question">Existing datasets</stripes:label>
								</td>
								<td>
									<stripes:select name="dataset" style="width: 383px;" id="selDataset">
										<stripes:option value="new_dataset" label="- new dataset -" />
										<c:forEach items="${actionBean.existingDatasets}" var="ds" varStatus="loop">
											<stripes:option value="${ds}" label="${ds}" />
										</c:forEach>
									</stripes:select>
								</td>
							</tr>
						</table>
						 <div id="newFile">
							<table border="0" width="550">
								<tr>
									<td width="160">
										<stripes:label for="fileName" class="required question">Dataset name</stripes:label>
									</td>
									<td>
										<stripes:text name="fileName" id="fileName" size="58"/>
									</td>
								</tr>
								<tr>
									<td width="160">
										<stripes:label for="folder" class="required question">Folder</stripes:label>
									</td>
									<td>
										<stripes:select name="folder" id="folder">
											<c:forEach items="${actionBean.folders}" var="f" varStatus="loop">
												<stripes:option value="${f}" label="${crfn:removeHomeUri(f)}" />
											</c:forEach>
										</stripes:select>
									</td>
								</tr>
								<tr>
									<td></td>
									<td>
										<stripes:checkbox name="overwrite" id="overwrite"/>
										<stripes:label for="overwrite">Overwrite if file/dataset already exists</stripes:label>
									</td>
								</tr>
							</table>
						</div>
						<table border="0" width="550">
							<tr>
								<td align="right">
									<stripes:hidden name="selectedDeliveries" value="${actionBean.selectedDeliveries}"/>
									<stripes:submit name="save" value="Save dataset"/>
								</td>
							</tr>
						</table>
		        	</stripes:form>
		        </c:if>
        	</c:when>
			<c:otherwise>
				<div class="note-msg">You are not logged in!</div>
			</c:otherwise>
		</c:choose>

    </stripes:layout-component>
</stripes:layout-render>
