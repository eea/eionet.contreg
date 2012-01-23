<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit Harvesting Source">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        // Open dialog
                        $("#changeOwnerButton").click(function() {
                            $('#changeOwnerDialog').dialog('open');
                            return false;
                        });

                        // Dialog setup
                        $('#changeOwnerDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        // Close dialog
                        $("#closeOwnerDialog").click(function() {
                            $('#changeOwnerDialog').dialog("close");
                            return false;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />
        <br />
        <br />
        <h1>Edit source</h1>

        <crfn:form action="/sourceEdit.action">
            <stripes:hidden name="uri"/>
            <stripes:hidden name="harvestSource.sourceId"/>
            <stripes:hidden name="harvestSource.owner"/>
            <table>
                <col style="width:10em"/>
                <col/>
                <tr>
                    <td>URL:</td>
                    <td><stripes:text name="harvestSource.url" size="100" style="width:100%"/></td>
                </tr>
                <tr>
                    <td>E-mails:</td>
                    <td><stripes:text name="harvestSource.emails" size="100" style="width:100%"/></td>
                </tr>
                <tr>
                    <td>Owner</td>
                    <td>
                        <c:out value="${actionBean.harvestSource.owner}" />
                    </td>
                </tr>
                <tr>
                    <td>Harvest interval:</td>
                    <td>
                        <stripes:text name="harvestSource.intervalMinutes" size="10"/>
                        <stripes:select name="intervalMultiplier" value="${actionBean.selectedIntervalMultiplier}">
                            <c:forEach items="${actionBean.intervalMultipliers}" var="intervalMultiplier">
                                <stripes:option value="${intervalMultiplier.key}" label="${intervalMultiplier.value}"/>
                            </c:forEach>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td>Media type:</td>
                    <td><stripes:select name="harvestSource.mediaType" value="${actionBean.harvestSource.mediaType}">
                        <c:forEach items="${actionBean.mediaTypes}" var="type">
                            <stripes:option value="${type}" label="${type}"/>
                        </c:forEach>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td><label class="question" for="schema">Is "Schema" source:</label></td>
                    <td>
                        <stripes:checkbox name="schemaSource" id="schema"/>
                    </td>
                </tr>
                <tr>
                    <td><label class="question" for="priority">Is "Priority" source:</label></td>
                    <td>
                        <stripes:checkbox name="harvestSource.prioritySource" id="priority"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <c:if test="${actionBean.userOwner}">
                            <stripes:submit name="save" value="Save"/>
                            <button id="changeOwnerButton">Change owner</button>
                            <c:if test="${!actionBean.harvestSource.prioritySource}">
                                <div style="display: inline; padding-left: 20px;">
                                    <stripes:submit name="delete" value="Delete"/>
                                </div>
                            </c:if>
                         </c:if>
                    </td>
                </tr>
            </table>
        </crfn:form>

        <div id="changeOwnerDialog" title="Change owner">
            <crfn:form action="/sourceEdit.action">
                <stripes:hidden name="uri" value="${actionBean.uri}" />

                <fieldset style="border: 0px;">
                    <label for="username" style="width: 200px; float: left;">New owner username:</label>
                    <stripes:text id="username" name="username"/>
                </fieldset>
                <br />
                <br />
                <stripes:submit name="changeOwner" value="Change owner"/>
                <button id="closeOwnerDialog">Cancel</button>
            </crfn:form>
        </div>

    </stripes:layout-component>

</stripes:layout-render>