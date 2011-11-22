<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Registrations">
    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />
        <c:choose>
            <c:when test="${actionBean.usersRegistrations}" >
                <h1>My registrations</h1>
            </c:when>
            <c:otherwise>
                <h1>${actionBean.ownerName}'s registrations</h1>
            </c:otherwise>
        </c:choose>
        <c:choose>
        <c:when test="${not empty actionBean.registrations}">
            <display:table name="${actionBean.registrations}" class="datatable"
                pagesize="20" sort="list" id="registrations" htmlId="registratioinslist"
                requestURI="${actionBean.urlBinding}" style="width:100%">
                    <display:column title="Subject" sortable="true">
                        <stripes:link href="/factsheet.action">${registrations.subjectUri}
                            <stripes:param name="uri" value="${registrations.subjectUri}" />
                        </stripes:link>
                    </display:column>
                    <display:column title="Predicate" sortable="true">
                        <stripes:link href="/factsheet.action">${registrations.predicateUri}
                            <stripes:param name="uri" value="${registrations.predicateUri}" />
                        </stripes:link>
                    </display:column>
                    <display:column title="Object" sortable="true">
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
