<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User Home">
<stripes:layout-component name="contents">

    <c:if test="${ actionBean.userAuthorized}">
    <h1>Compiled datasets</h1>
        <crfn:form id="datasetsForm" action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/compiledDatasets" method="post">

            <display:table name="${actionBean.compiledDatasets}" class="sortable"
                            pagesize="20" sort="list" id="uploadDTO" htmlId="uploads"
                            requestURI="${actionBean.parsedUrlBinding}" style="width:100%">

                <display:column title="" sortable="true" style="width:1em">
                    <input type="checkbox" value="${uploadDTO.subjectUri}" name="subjectUris" />
                </display:column>
                <display:column title="Title" sortable="true">
                    <stripes:link href="/factsheet.action">
                        ${uploadDTO.label}
                        <stripes:param name="uri" value="${uploadDTO.subjectUri}"/>
                    </stripes:link>
                </display:column>
                <display:column title="Modified" sortable="true" style="width:10em">
                    ${uploadDTO.dateModified}
                </display:column>

            </display:table>
            <div>
                <stripes:submit name="delete" value="Delete" title="Delete selecetd files"/>
                <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('datasetsForm');return false"/>
            </div>
        </crfn:form>
    </c:if>
    
</stripes:layout-component>
</stripes:layout-render>
