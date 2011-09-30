<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Delivery search">

    <stripes:useActionBean beanclass="eionet.cr.web.action.DeliverySearchActionBean" id="deliverySearchActionBean"/>

    <stripes:layout-component name="contents">

        <h1>Search Reportnet deliveries</h1>
                <p class="documentDescription">
                A Reportnet delivery is a response from a country on a reporting obligation. Deliveries can reside on several locations
                such as <a href="http://cdr.eionet.europa.eu/">CDR</a> or the country's own website.
                Deliveries are tagged with the obligation, spatial coverage (locality) and temporal coverage (year).
                </p>

        <div style="margin-top:15px">
            <crfn:form action="/deliverySearch.action" method="get">

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

            </crfn:form>
        </div>
		<br/>
        <c:if test="${not empty param.search}">
        	<display:table name="${actionBean.deliveries}" class="sortable" sort="external" id="listItem" htmlId="resourcesResultList" requestURI="/deliverySearch.action" decorator="eionet.cr.web.util.DeliverySearchTableDecorator">
				<display:column property="title" title="Title" sortable="true"/>
				<display:column property="fileCnt" title="Files"/>
				<display:column property="period" title="Period" sortable="true"/>
				<display:column property="locality" title="Locality"/>
				<display:column property="date" title="Date" sortable="true"/>
			</display:table>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
