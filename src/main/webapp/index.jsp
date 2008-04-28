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
	    this site. CR uses the <a href="dcmes.jsp" onclick="pop(this.href);return false;">DublinCore Metadata Element Set</a>
	    to describe the content, but it also keeps track of Eionet specific metadata elements.
	    </p>
	    <h2>Simple search</h2>
	    <p>
	    To quickly find content by text in any metadata element, you can start
	    right here by using the text input below. You can choose between free
	    text search, exact search and substring search. For free text search,
	    the words have to be at least 3 letters in length. Note that substring
	    search is usually the slowest of the three.
	    </p>
	    
	    <stripes:form action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
			
	    	<stripes:label for="expressionText">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionText" size="30"/>
	    	<stripes:submit name="search" value="Search" id="searchButton"/>
	    	
	    </stripes:form>
	    
	    <h2>Further searches and login</h2>
		<p>
	    On the left of the screen you can choose between different search cases:
	    </p>
	    <ul>
	        <li>
	            <a href="/cr/dataflowSearch.action">Dataflow search</a>
	        </li>
	        <li>
	            <a href="/cr/customSearch.action">Custom search</a>
	        </li>
	        <li>
	            <a href="/cr/simpleSearch.action">Simple search</a>
	        </li>
	        <li>
	            <a href="/cr/recentUploads.action">Recent uploads</a>
	        </li>
	    </ul>
	    <p>
	    There's more help on each search case when you go
	    there. Generally, <a href="/cr/dataflowSearch.action">Dataflow
	    search</a> is meant for dataflow managers to observe
	    specific dataflows in the dimensions of country and year.
	    <a href="/cr/customSearch.action">Custom search</a> is for
	    those more familiar with the DublinCore Metadata Elements,
	    while <a href="/cr/simpleSearch.action">Simple search</a>
	    represents the same quick-find search displayed above.
	    <br/> Administrators can use additional functions that
	    will appear on the left of the screen after logging in
	    by clicking the Login button on the left.  Those functions will enable to
	    administrate the Harvester which is the process running
	    in the background, harvesting the metadata from specified
	    Eionet services.
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
