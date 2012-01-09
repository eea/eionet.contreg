<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Login">

    <stripes:layout-component name="contents">

        <c:if test="${actionBean.loginFailure}">
            <div class="error-msg">
                Login failed!<br/>
                Either you have entered a wrong user name or password, or there was a system error.<br/>
                Please try again.
            </div>
        </c:if>
        <h1>Login</h1>
        <p>Please enter your user name and password below; then click on the Login button to continue.</p>
        <div id="login_div">
            <crfn:form action="/login.action" method="post">
                <table>
                    <tr>
                        <td>
                            <label class="question" for="usernameInput">User name:</label>
                        </td>
                        <td>
                            <stripes:text id="usernameInput" name="username"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label class="question" for="passwordInput">Password:</label>
                        </td>
                        <td>
                            <input id="passwordInput" type="password" name="password"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <input id="loginSubmit" type="submit" name="doLogin" value="Login"/>
                        </td>
                    </tr>
                </table>
            </crfn:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
