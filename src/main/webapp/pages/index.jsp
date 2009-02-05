<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Content Registry">

	<stripes:layout-component name="contents">
	
        <h1>What is Content Registry?</h1>
        
        <p>
	    Content Registry (CR) is a place where you can search for the
	    content of services in Eionet, based on their metadata.
	    Not all of the Eionet services are included, only those
	    that have been specified by the administrators of
	    this site. CR uses the <a href="http://dublincore.org/documents/dces/" onclick="pop(this.href);return false;">DublinCore's metadata elements</a>
	    to describe the content, but also keeps track of Eionet-specific metadata.
	    </p>
	    <h2>Simple search</h2>
	    <p>
	    To quickly find content by text in any metadata element, you can start right here by using the text input below.
	    </p>
	    
	    <stripes:form name="searchForm" action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
			
	    	<stripes:label for="expressionText">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionText" size="30"/>
	    	<stripes:submit name="search" value="Search" id="searchButton"/>
				<stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
	    	
	    </stripes:form>
	    
	    <h2>Further searches and administrative functions</h2>
		<p>
	    On the left of the screen you can choose between different search cases:
	    </p>
	    <ul>
	    	<li>
	            <a href="simpleSearch.action">Simple search</a><br/>
	            is the same quick-find search that is also displayed above.
	        </li>
	        <li>
	            <a href="dataflowSearch.action">Dataflow search</a><br/>
	            is meant for dataflow managers to observe specific dataflows in the dimensions of country and year.
	        </li>
	        <li>
	            <a href="customSearch.action">Custom search</a><br/>
	            enables you to choose the criteria you want to search by and offers picklists of existing values too.
	        </li>
			<li>
	            <a href="recentUploads.action">Recent uploads</a><br/>
	            displays the latest content that CR has discovered, classified by certain content types.
	        </li>
	    </ul>
	    <p>
	    Administrators can use additional functions that will appear on the left of the screen after logging in. These will enable to administrate the <em>Harvester</em> which is the process that
	    runs in the background and harvests the metadata from specified Eionet services.
	    </p>
	
	    <h2>Support</h2>
	    <p>
	    If you experience any problem using Content Registry, please let the Eionet Helpdesk know immediately.
	    The Helpdesk can be reached by phone on +37 2 508 4992 from Monday through Friday 9:00 to 17:00 CET.
	    You can also email the helpdesk at any time: <a href="mailto:helpdesk@eionet.europa.eu">helpdesk@eionet.europa.eu</a>.
	    Do not hesitate, we are here to help.
	    </p>
		
	</stripes:layout-component>
</stripes:layout-render>
