<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Bookmarklet installer">

    <stripes:layout-component name="contents">
        <h1>Installation of Bookmarklet</h1>
        <p>Bookmark this link to your favorites: <a
            href="${actionBean.bookmarklet}">add to Eionet CR</a>.</p>
        <p>When you come across a link worth adding to CR, you can simply
        select the full url and push "Add to Eionet CR" link. This will take
        you to the login page (if you are not logged in) or directly to the
        quick add resource form.</p>
        <p>Please note, that you can also bookmark any page by just
        clicking the "Add to Eionet CR" link in your favourites.</p>

    </stripes:layout-component>

</stripes:layout-render>
