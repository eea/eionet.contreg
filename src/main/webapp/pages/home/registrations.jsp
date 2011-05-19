<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User History">
<stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.userAuthorized}" >
            <h1>My registrations</h1>
        </c:when>
        <c:otherwise>
            <h1>${actionBean.attemptedUserName}'s registrations</h1>
        </c:otherwise>
    </c:choose>
        <c:choose>
        <c:when test="${not empty actionBean.registrations}">
            <display:table name="${actionBean.registrations}" class="datatable"
                pagesize="20" sort="list" id="registrations" htmlId="registratioinslist"
                requestURI="${actionBean.urlBinding}" style="width:100%">
                    <display:column title="Subject" sortable="false">
                        <stripes:link href="/factsheet.action">${registrations.subjectUri}
                            <stripes:param name="uri" value="${registrations.subjectUri}" />
                        </stripes:link>
                    </display:column>
                    <display:column title="Predicate" sortable="false">
                        <stripes:link href="/factsheet.action">${registrations.predicateUri}
                            <stripes:param name="uri" value="${registrations.predicateUri}" />
                        </stripes:link>
                    </display:column>
                    <display:column title="Object" sortable="false">
                        ${registrations.object}
                    </display:column>
            </display:table>
        </c:when>
        <c:otherwise>
            <p>No registrations found.</p>
        </c:otherwise>
    </c:choose>
</stripes:layout-component>
</stripes:layout-render>
