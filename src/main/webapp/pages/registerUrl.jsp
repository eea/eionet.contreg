<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple search">

	<stripes:layout-component name="contents">

	<h1>Register URL</h1>
	<p>
		This page enables you to register an URL and provide it with metadata.<br/>
		The purpose is to use CR as a way for users to help other users to find interesting resources on the Internet.<br/>
		The first person that discovers the resource, registers it with CR in similar way like it's done on
		<a class="link-external" href="http://en.wikipedia.org/wiki/Social_bookmarking">social bookmarking</a> websites.	
	</p>
	
	<crfn:form action="/registerUrl.action" method="post" focus="url">
	
		<stripes:label for="urlText">URL to register:</stripes:label>
		<br/>
		<stripes:text name="url" id="urlText" size="100"/>
		<br/><br/>
		<stripes:label for="bookmarkCheckbox">Bookmark this URL on my personal bookmark list</stripes:label>
		<stripes:checkbox name="bookmark" id="bookmarkCheckbox"/>
		<br/><br/>
		<stripes:submit name="save" value="Save" id="saveButton"/>
		<stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
		
	</crfn:form>
		
	</stripes:layout-component>
</stripes:layout-render>
	
