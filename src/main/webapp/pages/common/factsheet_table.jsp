<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>

<c:if test="${actionBean.subject.predicates!=null && fn:length(actionBean.subject.predicates)>0}">

    <c:set var="isRaw" value="${param.raw!=null}"/>

    <table class="datatable" width="100%" cellspacing="0" summary="">

        <c:if test="${displayCheckboxes}">
            <col/>
        </c:if>
        <col style="width:25%;"/>
        <col/>
        <col/>
        <col style="width:4em;"/>

        <thead>
            <c:if test="${displayCheckboxes}">
                <th scope="col" class="scope-col">&nbsp;</th>
            </c:if>
            <th scope="col" class="scope-col">Property</th>
            <th scope="col" class="scope-col">&nbsp;</th>
            <th scope="col" class="scope-col">Value</th>
            <th scope="col" class="scope-col">Source</th>
        </thead>
        <tbody>
            <c:forEach var="predicate" items="${actionBean.subject.sortedPredicates}" varStatus="predLoop">

                <c:set var="predicateLabelDisplayed" value="${false}"/>
                <c:set var="predicateObjectsCount" value="${actionBean.subject.predicateObjectCounts[predicate.key]}"/>

                <c:forEach items="${predicate.value}" var="object" varStatus="objLoop">

                    <tr>
                        <c:if test="${displayCheckboxes}">
                            <c:choose>
                                <c:when test="${sessionScope.crUser.registrationsUri==object.sourceSmart}">
                                    <th>
                                        <input type="checkbox" name="rowId" value="${crfn:spoHash(predicate.key)}_${object.id}"/>
                                        <stripes:hidden name="pred_${crfn:spoHash(predicate.key)}" value="${predicate.key}" />
                                        <stripes:hidden name="obj_${object.id}" value="${object.value}"/>
                                        <stripes:hidden name="source_${object.id}" value="${object.sourceUri}"/>
                                    </th>
                                </c:when>
                                <c:otherwise><th>&nbsp;</th></c:otherwise>
                            </c:choose>
                        </c:if>
                        <th scope="row" class="scope-row" style="white-space:nowrap">
                            <c:choose>
                                <c:when test="${not predicateLabelDisplayed}">
                                    <c:out value="${actionBean.subject.predicateLabels[predicate.key]}"/>
                                    <c:set var="predicateLabelDisplayed" value="${true}"/>
                                    <stripes:link  href="/factsheet.action" title="${predicate.key}">
                                        <stripes:param name="uri" value="${predicate.key}"/>
                                        <img src="${pageContext.request.contextPath}/images/view2.gif" alt="Definition"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </th>
                        <td>
                            <c:choose>
                                <c:when test="${objLoop.index==0 && predicateObjectsCount>1}">
                                    <c:choose>
                                        <c:when test="${empty actionBean.predicatePageNumbers[predicate.key]}">
                                               <stripes:link href="${crfn:predicateExpandLink(actionBean,predicate.key,1)}">
                                       <img src="${pageContext.request.contextPath}/images/expand.png" title="${predicateObjectsCount} values" alt="Browse ${predicateObjectsCount} values"/>
                                      </stripes:link>
                                        </c:when>
                                        <c:otherwise>
                                               <stripes:link href="${crfn:predicateCollapseLink(actionBean,predicate.key)}">
                                             <img src="${pageContext.request.contextPath}/images/collapse.png" title="Collapse" alt="Collapse"/>
                                               </stripes:link>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!object.literal}">
                                    <c:choose>
                                        <c:when test="${!object.anonymous}">
                                            <stripes:link class="infolink" href="/factsheet.action" title="${object.displayValue==object.value ? object.displayValue : object.value}"><c:out value="${object.displayValue}"/>
                                                <stripes:param name="uri" value="${object.value}"/>
                                            </stripes:link>
                                        </c:when>
                                        <c:otherwise>
                                            <stripes:link class="infolink" href="/factsheet.action"><c:out value="${object.displayValue}"/>
                                                <stripes:param name="uri" value="${object.value}"/>
                                            </stripes:link>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <span title="[Datatype: ${object.dataTypeLabel}]" style="white-space:pre-wrap"><c:out value="${object.value}"/></span><c:if test="${object.objectMD5!=null}">&nbsp;<stripes:link id="predObjValueLink_${predLoop.index+objLoop.index}" href="${actionBean.urlBinding}" event="openPredObjValue" title="Open full text of this value">
                                            <strong>[...]</strong>
                                            <stripes:param name="uri" value="${actionBean.uri}"/>
                                            <stripes:param name="predicateUri" value="${predicate.key}"/>
                                            <stripes:param name="objectMD5" value="${object.objectMD5}"/>
                                            <stripes:param name="graphUri" value="${object.sourceSmart}"/>
                                        </stripes:link></c:if>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty object.language}">
                                <span class="langcode" title="Language code of this text is '${object.language}'"><c:out value="${object.language}"/></span>
                            </c:if>
                        </td>
                        <td class="center">
                            <c:choose>
                                <c:when test="${object.sourceSmart!=null}">
                                    <stripes:link href="/factsheet.action">
                                        <img src="${pageContext.request.contextPath}/images/harvest_source.png" title="${fn:escapeXml(object.sourceSmart)}" alt="${fn:escapeXml(object.sourceSmart)}"/>
                                        <stripes:param name="uri" value="${object.sourceSmart}"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>

                    <c:set var="predicateNumberOfPages" value="${crfn:numberOfPages(predicateObjectsCount, actionBean.predicatePageSize)}"/>

                    <c:if test="${predicateNumberOfPages>1 && fn:length(predicate.value)>1 && objLoop.index==fn:length(predicate.value)-1}">
                        <c:set var="predicatePageNumber" value="${actionBean.predicatePageNumbers[predicate.key]}"/>
                        <tr>
                            <c:if test="${displayCheckboxes}">
                                <th>&nbsp;</th>
                            </c:if>
                            <th>&nbsp;</th>
                            <td>&nbsp;</td>
                            <td class="factsheetValueBrowse">
                                <c:if test="${predicatePageNumber>1}">
                                    <c:choose>
                                        <c:when test="${predicatePageNumber==2}">
                                            <stripes:link href="${crfn:predicateExpandLink(actionBean,predicate.key,1)}" class="factsheetValueBrowse">First ${actionBean.predicatePageSize} values ...</stripes:link>
                                        </c:when>
                                        <c:otherwise>
                                            <stripes:link href="${crfn:predicateExpandLink(actionBean,predicate.key,predicatePageNumber-1)}" class="factsheetValueBrowse">Previous ${actionBean.predicatePageSize} values ...</stripes:link>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${predicatePageNumber != predicateNumberOfPages}">
                                    <c:choose>
                                        <c:when test="${predicatePageNumber == predicateNumberOfPages-1}">
                                            <c:if test="${predicatePageNumber>1}">&nbsp;|&nbsp;</c:if><stripes:link href="${crfn:predicateExpandLink(actionBean,predicate.key,predicateNumberOfPages)}" class="factsheetValueBrowse">Last ${predicateObjectsCount-(predicatePageNumber*actionBean.predicatePageSize)} values ...</stripes:link>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${predicatePageNumber>1}">&nbsp;|&nbsp;</c:if><stripes:link href="${crfn:predicateExpandLink(actionBean,predicate.key,predicatePageNumber+1)}" class="factsheetValueBrowse">Next ${actionBean.predicatePageSize} values ...</stripes:link>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                            <td>&nbsp;</td>
                        </tr>
                    </c:if>

                </c:forEach>
            </c:forEach>
        </tbody>
       </table>

</c:if>
</stripes:layout-definition>
