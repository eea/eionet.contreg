<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>

    <div style="margin-top:20px; clear:both;">
        <c:choose>
            <c:when test="${(actionBean.resultList!=null && fn:length(actionBean.resultList)>0) || actionBean.matchCount>0}">
                <c:set var="pagination" value="${actionBean.pagination}"/>
                <div class="pagination">
                    <c:if test="${actionBean.matchCount>0}">
                        <span class="matchescount">
                            <c:if test="${actionBean.exactCount == false}">About </c:if>
                            ${actionBean.matchCount} matches found
                            <c:if test="${pagination!=null}">
                                <c:choose>
                                    <c:when test="${pagination.rowsFrom==pagination.rowsTo}">, displaying last one</c:when>
                                    <c:otherwise>, displaying ${pagination.rowsFrom} to ${pagination.rowsTo}</c:otherwise>
                                </c:choose>
                            </c:if>
                        </span>
                        <c:if test="${pagination!=null}">
                            <c:choose>
                                <c:when test="${pagination.curPageNum==1}">
                                    <span class="firstpage">First</span>, <span class="prevpage">Prev</span>,
                                </c:when>
                                <c:otherwise>
                                    <a href="${pagination.first.href}" class="firstpage">First</a>, <a href="${pagination.prev.href}" class="prevpage">Prev</a>,
                                </c:otherwise>
                            </c:choose>
                            <c:forEach items="${pagination.group}" var="numberedPage" varStatus="paginationGroupStatus">
                                <c:choose>
                                    <c:when test="${numberedPage.selected}">
                                        <c:if test="${paginationGroupStatus.count>1}">, </c:if><span class="selectedpage">${numberedPage.number}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${paginationGroupStatus.count>1}">, </c:if><a href="${numberedPage.href}" class="numberedpage">${numberedPage.number}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pagination.curPageNum==pagination.numOfPages}">
                                    <span class="nextpage">Next</span>
                                    <c:if test="${actionBean.exactCount == true}">
                                        <span class="lastpage">, Last</span>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pagination.next.href}" class="nextpage">Next</a>
                                    <c:if test="${actionBean.exactCount == true}">
                                        , <a href="${pagination.last.href}" class="lastpage">Last</a>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </c:if>
                </div>
                <c:choose>
                <c:when test="${actionBean.resultList!=null && fn:length(actionBean.resultList)>0}">
                    <table id="resourcesResultList" class="${tableClass}">
                        <thead>
                            <tr>
                            <c:forEach items="${actionBean.columns}" var="col">
                                <th scope="col">
                                    <c:choose>
                                        <c:when test="${col.sortable && not empty col.sortParamValue}">
                                            <a title="${col.title}" href="${crfn:sortUrl(actionBean,col)}">
                                                ${col.title}
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            ${col.title}
                                        </c:otherwise>
                                    </c:choose>
                                </th>
                            </c:forEach>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actionBean.resultList}" var="resultListItem" varStatus="rowStatus">
                                <tr
                                    <c:choose>
                                        <c:when test="${rowStatus.count%2 != 0}">
                                             class="odd"
                                        </c:when>
                                        <c:otherwise>
                                            class="even"
                                        </c:otherwise>
                                    </c:choose>>
                                    <c:forEach items="${actionBean.columns}" var="col">
                                        <%-- assume crfn:format() does the XML escaping --%>
                                        <%-- is implemented in: public class JstlFunctions --%>
                                        <td>${crfn:format(col, resultListItem, actionBean)}</td>
                                    </c:forEach>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="system-msg">Oups, already at the end.</div>
                </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <div class="system-msg">No matches found</div>
            </c:otherwise>
        </c:choose>
    </div>

</stripes:layout-definition>
