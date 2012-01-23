<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />
        <br />
        <br />
        <h1>Sample triples</h1>

        <c:choose>
        <c:when test="${not empty actionBean.sampleTriples}">
            <table id="sampletriples" class="datatable">
                <col style="width: 30%" />
                <col style="width: 30%" />
                <col style="width: 40%" />
                <thead>
                    <tr>
                        <th scope="col">Subject</th>
                        <th scope="col">Predicate</th>
                        <th scope="col">Object</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${actionBean.sampleTriples}" var="sampleTriple"
                        varStatus="loop">
                        <tr
                            <c:if test="${sampleTriple.objectDerivSourceHash > 0}">class="derived"</c:if>>
                            <td><c:choose>
                                <c:when test="${not empty sampleTriple.subjectUri}">
                                    <c:out
                                        value="${crfn:cutAtFirstLongToken(sampleTriple.subjectUri, 100)}" />
                                </c:when>
                                <c:otherwise>
                                    No URI found
                                </c:otherwise>
                            </c:choose></td>
                            <td><c:out
                                value="${crfn:cutAtFirstLongToken(sampleTriple.predicateUri, 100)}" /></td>
                            <td><c:out
                                value="${crfn:cutAtFirstLongToken(sampleTriple.object, 100)}" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div class="important-msg">No sample triples found!</div>
        </c:otherwise>
    </c:choose>

    </stripes:layout-component>
</stripes:layout-render>