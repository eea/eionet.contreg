<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/popuptemplate.jsp" pageTitle="Bookmark installed">

    <stripes:layout-component name="contents">



    <script type="text/javascript">
    function goBackWithDelay() {
        setTimeout('closeWindow()', 5000);
    }

    function closeWindow(){
        this.close();
    }

    function goBackToPage() {
        document.location = '${actionBean.originalPageUrl}';
    }
    addEvent(window,'load',goBackWithDelay);
    </script>


    <h1> Thank you, ${actionBean.userName} </h1>
    <form method="get" action="${actionBean.originalPageUrl}">
    <p>
    URL was successfully added to the system.
    <input type="button" onClick="closeWindow()" value="OK"/>
    </p>
    <p>
    In a few seconds this window will be closed.
    </p>
    </form>

    </stripes:layout-component>
</stripes:layout-render>
