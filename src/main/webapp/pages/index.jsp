<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Content Registry">

	<stripes:layout-component name="contents">
<div style="float:right; background-color:white; width: 20em; padding-left: 1em;">
	<div style="background-color:#f0f0f0; padding:0.5em; margin: 0.3em; border:1px dotted black;">
		<c:choose>
			<c:when test='${sessionScope.crUser!=null}'>
				<stripes:link href="/registerUrl.action" style="font-weight:bold">Suggest a URL!</stripes:link>
			</c:when>
			<c:otherwise>
				<stripes:link title="Login" href="/login.action" event="login" style="font-weight:bold">Login to suggest a URL!</stripes:link>
			</c:otherwise>
		</c:choose>
		Help other researchers find the good datasets. Bookmark them on this site.
	</div>
	<div style="background-color:#f0f0f0; padding:0.5em; margin: 0.3em; border:1px dotted black;">
		<div style="font-size:1.2em;font-weight:bold">Tag cloud here!</div>
		In 2002 Jim Flanagan was the first using tag clouds as a display
		form. Flickr, <span style="font-size:1.2em">Technorati</span> and Blogs have tightened the trend in the design
		business and today leading enterprises like Spiegel.de, O2-online and
		others use this tool. Will <span style="font-size:1.4em">tag</span> clouds become a <span style="font-size:0.8em">trend</span> , like a lot of
		before, which came and disappeared again when we recognized that we benefit
		less than we thought from them ? A tag <span style="font-size:1.4em">cloud</span> is a means of visualisation
		of information within a specified area.
	</div>
	<div style="background-color:#f0f0f0; padding:0.5em; margin: 0.3em; border:1px dotted black;">
		<div style="font-size:1.2em;font-weight:bold">Recently discovered files:</div>
		<ul class="menu">
		<li>2009 submission under Article 3(2)</li>
		<li>Monthly Ozone April 2009 report</li>
		<li>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus vel.</li>
		<li>Nunc quis venenatis lacus. Vivamus pretium, nunc nec volutpat eleifend.</li>
		<li>2009 submission under Article 3(2)</li>
		<li>2009 submission under Article 3(2)</li>
		<li>2009 submission under Article 3(2)</li>
		<li>2009 submission under Article 3(2)</li>
		<li>...</li>
		</ul>
	</div>
</div>
        <h1>What is Content Registry?</h1>
	<p>
		Content Registry (CR) is an object-oriented search engine where you can search for the content of data in Eionet.
		Being object-oriented means it understands what e.g. a measuring station is and can show what measurements
		it has made.
		Not all of the Eionet services are included, only those that have been specified by the administrators of this site.
	</p>
	<h2>Simple search</h2>
	<p>
	    To quickly find content by text in any metadata element, you can start right here by using the text input below.
	<em>Words shorter than four letters are ignored!</em>
	</p>

	<stripes:form name="searchForm" action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
	    	<stripes:label for="expressionText">Expression:</stripes:label>
	    	<stripes:text name="searchExpression" id="expressionText" size="40"/>
	    	<stripes:submit name="search" value="Search" id="searchButton"/>
		<stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
	</stripes:form>

	<h2>Further searches</h2>
	<p>
		To the right you'll see two pre-cooked searches. The most popular tags used on resources and a list of
		recently discovered files.
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
		    <a href="typeSearch.action">Type search</a><br/>
		    finds all objects of the same type.
		</li>
		<li>
		    <a href="recentUploads.action">Recent uploads</a><br/>
		    displays the latest content that CR has discovered, classified by certain content types.
		</li>
	    </ul>

	    <h2>Support</h2>
	    <p>
	    If you experience any problem using Content Registry, please let the Eionet Helpdesk know immediately.
	    The Helpdesk can be reached by phone on +37 2 508 4992 from Monday through Friday 9:00 to 17:00 CET.
	    You can also email the helpdesk at any time: <a href="mailto:helpdesk@eionet.europa.eu">helpdesk@eionet.europa.eu</a>.
	    Do not hesitate, we are here to help.
	    </p>

	</stripes:layout-component>
</stripes:layout-render>
