
<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/popuptemplate.jsp" siteTitle="${initParam.appDispName} bookmarklet installer">

    <stripes:layout-component name="contents">

        <script type="text/javascript">
            function closeWindow(){
                this.close();
            }
        </script>

        <h1>Add bookmark</h1>
        <!--<stripes:errors/>-->
        <c:choose>
           <c:when test="${actionBean.loggedIn}">
               <stripes:form action="/quickAddBookmark.action">
                <label for="url">Bookmark URL:</label>
                <stripes:text name="resource.source" value="${actionBean.resource.source}" id="url" style="margin-bottom: 5px;" size="60" />
                <br />

                <!-- <label for="title">Title:</label>
                <stripes:text name="resource.title" value="${actionBean.resource.title}" id="title" style="margin-bottom: 5px; margin-left: 63px;" size="60"/>
                <br />
                <label for="resource.tags">Tags:</label>
                <stripes:text name="resource.tags" id="resource.tags" size="40" style="margin-left: 60px;"/> -->

                <div class="auto_complete" id="tag_auto_complete" style="position: absolute; z-index: 99;"></div>
                <script type="text/javascript">
                                        //<![CDATA[
                                        var tag_auto_completer =
                                            new Ajax.Autocompleter(
                                                'resource.tags',
                                                'tag_auto_complete',
                                                '${pageContext.request.contextPath}/addEditResource.action?suggestTags',
                                                { tokens: ' ', paramName: 'tagNameToSuggest'});
                                        //]]>
                </script>
                <br />
                <label for="saveToBookmarks">Add to personal bookmarks:</label>
                <stripes:checkbox name="saveToBookmarks" checked="true"  style="margin-bottom: 5px;" id="saveToBookmarks"/>
                <br />
                <stripes:hidden name="originalPageUrl"/>
                <stripes:submit name="processForm" value="Add bookmark" style="margin-bottom: 5px;" />
            </stripes:form>
        </c:when>
        <c:otherwise>
            To add the bookmark you must be logged in. <a href="login.action?login=">Click here to login.</a>
        </c:otherwise>
    </c:choose>

        <form method="get" action="${actionBean.originalPageUrl}">
        <hr/>
        <p align="right">
        <input type="button" onClick="closeWindow()" value="Close window"/>
        </p>
        </form>

    </stripes:layout-component>
</stripes:layout-render>
