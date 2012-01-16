<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Delivery search">

    <stripes:useActionBean beanclass="eionet.cr.web.action.DeliverySearchActionBean" id="deliverySearchActionBean"/>

    <stripes:layout-component name="contents">

        <c:choose>
            <c:when test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/mergedeliveries", "v")}'>
                <h1>Search Reportnet deliveries</h1>
                        <p class="documentDescription">
                        A Reportnet delivery is a response from a country on a reporting obligation. Deliveries can reside on several locations
                        such as <a href="http://cdr.eionet.europa.eu/">CDR</a> or the country's own website.
                        Deliveries are tagged with the obligation, spatial coverage (locality) and temporal coverage (year).
                        </p>

                <crfn:form action="/deliverySearch.action" method="get">
                    <div style="margin-top:15px">

                        <label for="obligationSelect" class="question" style="margin-bottom:3px">Obligation:</label>
                        <stripes:select name="obligation" id="obligationSelect" size="15" style="width:100%">
                            <c:forEach var="instr" items="${deliverySearchActionBean.instrumentsObligations}">
                                <optgroup label="${instr.key.label}">
                                    <c:forEach var="oblig" items="${instr.value}">
                                        <stripes:option value="${oblig.uri}" label="${oblig.label}"/>
                                    </c:forEach>
                                </optgroup>
                            </c:forEach>
                        </stripes:select>
                        <br/>
                        <br/>

                        <label for="localitySelect" class="question">Locality:</label>
                        <stripes:select name="locality" id="localitySelect" style="max-width:200px">
                            <stripes:option value="" label="-- All --"/>
                            <c:forEach var="loclty" items="${deliverySearchActionBean.localities}">
                                <stripes:option value="${crfn:addQuotesIfWhitespaceInside(loclty.left)}" label="${loclty.right}"/>
                            </c:forEach>
                        </stripes:select>
                        <label for="yearSelect" class="question" style="display:inline;margin-left:20px">Coverage year:</label>
                        <stripes:select name="year" id="yearSelect">
                            <stripes:option value="" label="-- All --"/>
                            <c:forEach var="y" items="${deliverySearchActionBean.years}">
                                <stripes:option value="${y}" label="${y}"/>
                            </c:forEach>
                        </stripes:select><stripes:submit name="search" value="Search" id="searchButton" style="display:inline;margin-left:60px"/>
                    </div>
                </crfn:form>
                <br/>
                <c:if test="${not empty param.search}">
                    <c:choose>
                        <c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                            <crfn:form action="/saveFiles.action" method="post" id="deliveriesForm">
                                <display:table name="${actionBean.deliveries}" class="sortable" sort="external" id="listItem"
                                            htmlId="resourcesResultList" requestURI="/deliverySearch.action"
                                            decorator="eionet.cr.web.util.DeliverySearchTableDecorator">
                                    <display:setProperty name="paging.banner.all_items_found" value="{0} deliveries found." />
                                    <display:setProperty name="paging.banner.one_item_found" value="One delivery found." />
                                    <display:column title="">
                                        <stripes:checkbox name="selectedDeliveries" value="${listItem.subjectUri}"/>
                                    </display:column>
                                    <display:column property="title" title="Title" sortable="true"/>
                                    <display:column property="fileCnt" title="Files"/>
                                    <display:column property="periodValue" title="Period" sortable="true" sortProperty="period"/>
                                    <display:column property="locality" title="Locality"/>
                                    <display:column property="date" title="Date" sortable="true"/>
                                    <display:column property="coverageNote" title="Coverage Note" sortable="true"/>
                                </display:table>
                                <stripes:submit name="getFiles" value="Merge" title="Merge selected deliveries"/>
                                <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('deliveriesForm');return false"/>
                            </crfn:form>
                        </c:when>
                        <c:otherwise>
                            <display:table name="${actionBean.deliveries}" class="sortable" sort="external" id="listItem"
                                            htmlId="resourcesResultList" requestURI="/deliverySearch.action"
                                            decorator="eionet.cr.web.util.DeliverySearchTableDecorator">
                                <display:column property="title" title="Title" sortable="true"/>
                                <display:column property="fileCnt" title="Files"/>
                                <display:column property="periodValue" title="Period" sortable="true" sortProperty="period"/>
                                <display:column property="locality" title="Locality"/>
                                <display:column property="date" title="Date" sortable="true"/>
                            </display:table>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="note-msg">You are not logged in or you do not have enough privileges!</div>
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
