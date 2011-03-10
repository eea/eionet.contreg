<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Query">

    <stripes:layout-component name="contents">
        <script type="text/javascript">
            function getQueryResult() {

                  var event = "query";
                  var form = document.f;
                  if (!form.onsubmit) { form.onsubmit = function() { return false } };
                  var params = Form.serialize(form, {submit:event});
                  new Ajax.Updater('result', form.action, {method:'post', parameters:params});
            }
        </script>

        <h1>Virtuoso Query</h1>
        <stripes:form action="/virtuosoQuery.action" method="post" name="f">
            <table width="470" border="0">
                <tr>
                    <td valign="top">
                        <stripes:label for="query">Query:</stripes:label>
                    </td>
                    <td>
                        <stripes:textarea name="query" id="query" cols="75" rows="8"></stripes:textarea><br/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="right">
                        <stripes:submit name="query" value="Run query" onclick="javascript:getQueryResult();"/>
                    </td>
                </tr>
            </table>
        </stripes:form>
        <div id="result">
        </div>


    </stripes:layout-component>
</stripes:layout-render>
