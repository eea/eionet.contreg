<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add Harvesting Source">

    <stripes:layout-component name="contents">

        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        // Open prefixes dialog
                        $("#chkAuthenticated").click(function() {
                            setAuthRowVisibility();
                            return true;
                        });

                        $('#isIntervalDynamic').click(function() {
                            setHarvestIntervalVisibility();
                            return true;
                        })

                        function setAuthRowVisibility(){
                            if($('#chkAuthenticated').is(':checked')) {
                                $('#usernameRow').show();
                                $('#passwordRow').show();
                            } else {
                                $('#usernameRow').hide();
                                $('#passwordRow').hide();
                            }
                        }

                        function setHarvestIntervalVisibility() {
                            if ( $('#isIntervalDynamic').is(':checked') ) {
                                $('#harvestIntervalRow').hide();
                            }
                            else {
                                $('#harvestIntervalRow').show();
                            }
                        }

                        setAuthRowVisibility();
                        setHarvestIntervalVisibility;
                    });
            } ) ( jQuery );
            // ]]>
        </script>


        <h1>Add source</h1>
        <crfn:form action="/source.action" method="post">
            <table>
                <tr>
                    <td><label class="question required" for="harvesturl">URL</label></td>
                    <td><stripes:text id="harvesturl" name="harvestSource.url" size="80"/></td>
                </tr>
                <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                    <tr>
                        <td><label class="question" for="emails">E-mails</label></td>
                        <td><stripes:text id="emails" name="harvestSource.emails" size="80"/></td>
                    </tr>
                    <tr id="harvestIntervalRow">
                        <td><label class="question" for="interval">Harvest interval</label></td>
                        <td>
                            <stripes:text id="interval" name="harvestSource.intervalMinutes" size="10" value="6"/>
                            <stripes:select name="intervalMultiplier" value="10080">
                                <c:forEach items="${actionBean.intervalMultipliers}" var="intervalMultiplier">
                                    <stripes:option value="${intervalMultiplier.key}" label="${intervalMultiplier.value}"/>
                                </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="mediaType">Media type</label></td>
                        <td><stripes:select id="mediaType" name="harvestSource.mediaType" value="${actionBean.harvestSource.mediaType}">
                            <c:forEach items="${actionBean.mediaTypes}" var="type">
                                <stripes:option value="${type}" label="${type}"/>
                            </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="isIntervalDynamic">Is Interval dynamic?</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.intervalDynamic" id="isIntervalDynamic"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="priority">Is "Priority" source</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.prioritySource" id="priority"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="chkSparqlEndpoint">Is SPARQL endpoint</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.sparqlEndpoint" id="chkSparqlEndpoint"/>
                        </td>
                    </tr>


                    <tr>
                        <td><label class="question" for="chkAuthenticated">Add authentication info</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.authenticated" id="chkAuthenticated"/>
                        </td>
                    </tr>
                    <tr id="usernameRow">
                        <td><label class="question" for="userName">Source username</label></td>
                        <td>
                            <stripes:text name="harvestSource.username" id="userName" size="30"/>
                        </td>
                    </tr>

                    <tr id="passwordRow">
                        <td><label class="question" for="passWord">Source password</label></td>
                        <td>
                            <stripes:text name="harvestSource.password" id="passWord" size="30"/>
                        </td>
                    </tr>

                </c:if>
                <tr>
                    <td>
                        <stripes:submit name="add" value="Add"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${empty actionBean.harvestSource || actionBean.harvestSource.sparqlEndpoint == false}">
                                <input type="checkbox" name="dontHarvest" id="chkDontHarvest"/><label for="chkDontHarvest">Don't schedule urgent harvest</label>
                            </c:when>
                            <c:otherwise>&nbsp;<input type="hidden" name="dontHarvest" value="true"/></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </crfn:form>

    </stripes:layout-component>
</stripes:layout-render>
