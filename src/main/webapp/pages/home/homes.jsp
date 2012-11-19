<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
  pageTitle="User Folders">
  <stripes:layout-component name="contents">

    <h1>User Folders</h1>
    <c:choose>
      <c:when test="${not empty actionBean.resultList}">
        <p>User folder can contain files and sub-folders. There are 4
          special folders registered under each user's home folder:
          registrations, bookmarks, reviews, history.</p>

        <c:set var="pagination" value="${actionBean.pagination}" />
        <div class="pagination">
          <c:if test="${actionBean.matchCount>0}">
            <span class="matchescount"> <c:if
                test="${actionBean.exactCount == false}">About </c:if>
              ${actionBean.matchCount} matches found <c:if
                test="${pagination!=null}">
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
                  <a href="${pagination.first.href}" class="firstpage">First</a>, <a
                    href="${pagination.prev.href}" class="prevpage">Prev</a>,
                                </c:otherwise>
              </c:choose>
              <c:forEach items="${pagination.group}" var="numberedPage"
                varStatus="paginationGroupStatus">
                <c:choose>
                  <c:when test="${numberedPage.selected}">
                    <c:if test="${paginationGroupStatus.count>1}">, </c:if>
                    <span class="selectedpage">${numberedPage.number}</span>
                  </c:when>
                  <c:otherwise>
                    <c:if test="${paginationGroupStatus.count>1}">, </c:if>
                    <a href="${numberedPage.href}" class="numberedpage">${numberedPage.number}</a>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
              <c:choose>
                <c:when test="${pagination.curPageNum==pagination.numOfPages}">
                  <span class="nextpage">Next</span>
                  <c:if test="${actionBean.exactCount == true}">
                    ,<span class="lastpage">Last</span>
                  </c:if>
                </c:when>
                <c:otherwise>
                  <a href="${pagination.next.href}" class="nextpage">Next</a>
                  <c:if test="${actionBean.exactCount == true}">
                                        , <a
                      href="${pagination.last.href}" class="lastpage">Last</a>
                  </c:if>
                </c:otherwise>
              </c:choose>
            </c:if>
          </c:if>
        </div>


        <table id="resourcesResultList" class="sortable">
          <thead>
            <tr>
              <c:forEach items="${actionBean.columns}" var="col">
                <th scope="col"><c:choose>
                    <c:when test="${col.sortable && not empty col.sortParamValue}">
                      <a title="${col.title}"
                        href="${crfn:sortUrl(actionBean,col)}"> ${col.title} </a>
                    </c:when>
                    <c:otherwise>
                                            ${col.title}
                                        </c:otherwise>
                  </c:choose></th>
              </c:forEach>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${actionBean.resultList}" var="folder"
              varStatus="rowStatus">
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
                  <td><stripes:link href="/view.action">
                      <stripes:param name="uri" value="${folder.url}" />
                           ${folder.label}
                        </stripes:link> (${folder.subFilesCount} files,
                    ${folder.subFoldersCount} folders), <stripes:link
                      href="/view.action">
                      <stripes:param name="uri" value="${folder.url}/bookmarks" />
                           Bookmarks
                        </stripes:link>, <stripes:link href="/view.action">
                      <stripes:param name="uri" value="${folder.url}/registrations" />
                           Registrations
                        </stripes:link>, <stripes:link href="/view.action">
                      <stripes:param name="uri" value="${folder.url}/history" />
                           History
                        </stripes:link>, <stripes:link href="/view.action">
                      <stripes:param name="uri" value="${folder.url}/reviews" />
                           Reviews
                        </stripes:link></td>
                </c:forEach>
              </tr>
            </c:forEach>

          </tbody>
        </table>




      </c:when>
      <c:otherwise>
        <p>Users have not registered any folders yet.</p>
      </c:otherwise>
    </c:choose>
  </stripes:layout-component>
</stripes:layout-render>
