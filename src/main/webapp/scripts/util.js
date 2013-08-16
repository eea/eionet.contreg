/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 * Jaanus Heinlaid, Tieto Eesti
 * Enriko Käsper, Tieto Eesti
 * Jaak Kapten, Tieto Eesti
 */
( function($) {
    $(document).ready(
        function(){

            //Factsheet edit & autocomplete
            $("#propertySelect").change(function(){
                if(this.value=='http://cr.eionet.europa.eu/ontologies/contreg.rdf#tag'){
                    $('#propertyText').replaceWith('<input id="propertyText" type="text" name="propertyValue" value="' +  $('#propertyText').text() + '"  size="60" />');
                    var ac = $('#propertyText').autocomplete({
                        serviceUrl:'json/tags.action',
                        minChars:2,
                        delimiter: /(,|;)\s*/,
                        maxHeight:400,
                        width:300,
                        zIndex: 9999,
                        deferRequestBy: 0,
                        noCache: true
                    });
                }
                else {
                    if ($('input[id="propertyText"]').length>0){
                        $('#propertyText').replaceWith('<textarea id="propertyText" cols="100" name="propertyValue" rows="2">' +  $('#propertyText').text() + '</textarea>');
                    }
                }
            });
            //Tag search autocomplete
            if ($("#tagText").length > 0){
                $('#tagText').autocomplete({
                    serviceUrl:'json/tags.action',
                    minChars:2,
                    delimiter: /(,|;)\s*/,
                    maxHeight:400,
                    width:300,
                    zIndex: 9999,
                    deferRequestBy: 0,
                    noCache: true
                });
            }
            //Harvest source filter auto-complete
            if ($("#harvestSource").length > 0){
                $('#harvestSource').autocomplete({
                    serviceUrl:'json/harvestSources.action',
                    minChars:4,
                    delimiter: /(,|;)\s*/,
                    maxHeight:400,
                    width:600,
                    zIndex: 9999,
                    deferRequestBy: 300,
                    noCache: true
                });
            }
            //RDF type filter auto-complete
            if ($("#rdfType").length > 0){
                $('#rdfType').autocomplete({
                    serviceUrl:'json/rdfTypes.action',
                    minChars:4,
                    delimiter: /(,|;)\s*/,
                    maxHeight:400,
                    width:600,
                    zIndex: 9999,
                    deferRequestBy: 300,
                    noCache: true
                });
            }

        });
} ) ( jQuery );

/**
 * Toggles the "select all" feature for ALL checkboxes found inside
 * the given form.
 *
 * Input parameters:
 *   formId - the id of the form object where these checkboxes are looked for
 *
 * Return value: none
 */
function toggleSelectAll(formId) {
	formobj = document.getElementById(formId);
	checkboxes = formobj.getElementsByTagName('input');
	var isAllSelected = (formobj.selectAll.value == "Select all") ? false : true;

	if (isAllSelected == null || isAllSelected == false) {
		for (i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i].type == 'checkbox') {
				checkboxes[i].checked = true ;
			}
		}
		formobj.selectAll.value = "Deselect all";
	} else {
		for (i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i].type == 'checkbox') {
				checkboxes[i].checked = false ;
			}
		}
		formobj.selectAll.value = "Select all";
	}
}

/**
 * Toggles the "select all" feature for given checkbox field.
 *
 * Input parameters:
 *   formId - the id of the form object where these checkboxes are looked for
 *   field - the name of the checkbox field
 *
 * Return value: none
 */
function toggleSelectAllForField(formId, fieldName) {
	formobj = document.getElementById(formId);
	checkboxes = formobj.getElementsByTagName('input');
	var isAllSelected = (formobj.selectAll.value == "Select all") ? false : true;

	if (isAllSelected == null || isAllSelected == false) {
		for (i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i].type == 'checkbox' && checkboxes[i].name == fieldName) {
				checkboxes[i].checked = true ;
			}
		}
		formobj.selectAll.value = "Deselect all";
	} else {
		for (i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i].type == 'checkbox' && checkboxes[i].name == fieldName) {
				checkboxes[i].checked = false ;
			}
		}
		formobj.selectAll.value = "Select all";
	}
}
