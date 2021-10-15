<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/pages/common/taglibs.jsp" %>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error page">
    <stripes:layout-component name="head">
        <title>Error</title>
        <style>
            .error-template {
                padding: 40px 15px;
                text-align: center;
                color: red;
            }
        </style>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <div class="error-template">
            <h1 style="color:red">${msgOne}</h1>
            <div>Sorry, an error has occured!</div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>