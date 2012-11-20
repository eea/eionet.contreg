<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Browse VoID datasets">
    <stripes:layout-component name="contents">

        <h1>Browse VoID datasets</h1>

        <display:table name="${actionBean.datasets}" class="datatable" id="dataset" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

            <display:setProperty name="paging.banner.item_name" value="dataset"/>
            <display:setProperty name="paging.banner.items_name" value="datasets"/>
            <display:setProperty name="paging.banner.all_items_found" value='<span class="pagebanner">{0} {1} found.</span>'/>
            <display:setProperty name="paging.banner.onepage" value=""/>

            <display:column title='<span title="Title or label of the dataset, either supplied or derived from dataset URI.">Title</span>'>
                <stripes:link beanclass="${actionBean.factsheetActionBeanClass.name}" title="Go to the dataset factsheet">
                    <c:out value="${dataset.label}"/>
                    <stripes:param name="uri" value="${dataset.uri}"/>
                </stripes:link>
            </display:column>
            <display:column style="width:3em;text-align:center" title='<span title="The dataset creator(s)">Creator</span>'>
                <c:out value="${dataset.creator}"/>
            </display:column>
            <display:column style="width:12em;text-align:center" title='<span title="The dataset subject(s)">Subject</span>'>
                <c:out value="${dataset.subjects}"/>
            </display:column>

        </display:table>

    </stripes:layout-component>
</stripes:layout-render>
