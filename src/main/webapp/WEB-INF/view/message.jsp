<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/pages/common/taglibs.jsp" %>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error page">
    <stripes:layout-component name="head">
        <title>Error</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
        <style>
            .error-template {
                padding: 40px 15px;
                text-align: center;
                color: red;
            }
        </style>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <div class="row">
            <div class="col-md-12">
                <div class="error-template">
                    <h1 style="color:red">${msgOne}</h1>
                    <div>Sorry, an error has occured!</div>
                </div>
            </div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>