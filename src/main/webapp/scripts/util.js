/**
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
 * The Original Code is "EINRC-5 / WebROD Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Ander Tenno (TietoEnator)
 */

$(document).ready(
		function(){

			$("#propertyText").autocomplete("ever catch everyday cc");

			$("#export_form_noscript").hide();
			//export clicked
			$(".export_div").show().click(function(){
				//if div is in place - just show it
				if ($("#export_form_div").length > 0) {
					$("#export_form_div").show();
					return false;
				}
			    // create the export div
				$("#export_form_container")
						.append('<div id="export_form_div" ' +
								'style="padding: 20px; padding-top:0px; border: 1px solid; background-color: #cccccc; width: 500px;' +
								'height: 190px; position:absolute; z-index:100000;  left: 200px; top: 300px; ">' +
								'<a href="#" style="float:right; clear:both">close</a><br/>' + $("#export_form_noscript").html() + "</div>");
			    // attach close link
			    $("#export_form_div > a").click(function(){ $("#export_form_div").hide();});
			    // hide the div on submit
			    $("#export_form_submit").click(function(){
			    	$("#export_form_div").hide();
			    });
				return false;
			});

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

});


function showWait(contextRoot, url) {
	var v = $('#wait_link')[0].href ='#';
	
	if($('#wait_div').length > 0){
		return false;
	}
	$("#wait_container").append('<div id="wait_div"' + 
		'style="padding: 20px; border: 1px solid; background-color: #cccccc; width: 500px;' +
		'height: 50px; position:absolute; z-index:100000;  left: 300px; top: 300px; ">' +
		'<img src="' + contextRoot + '/images/wait.gif"/> Feedback will be available soon</div>');
	$("#wait_div").load(url, 
			function(){
				$("#wait_div").oneTime(
						5000,
						function(){
							$(this).hide(
									"slow",
									function(){$(this).remove();});
							});
				});
	return false;
}

//
// Replaces some text in a string with other text
//
function replaceText(string,text,by) {
  var strLength = string.length, txtLength = text.length;
  if((strLength == 0) || (txtLength == 0)) 
    return string;

  var i = string.indexOf(text);
  if((!i) && (text != string.substring(0,txtLength))) 
    return string;

  if(i == -1) 
    return string;

  var newstr = string.substring(0,i) + by;
  if(i+txtLength < strLength)
    newstr += replace(string.substring(i+txtLength,strLength),text,by);
  return newstr;
}

/**
* checks if the entered value is a valid URL, resets the filed if not
* quick hard-code - field is not selected - needs studying
*/
function chkUrl(fld) {
	var s = fld.value;
	if ( s != "" &&  (s.substr(0,7) != "http://") && (s.substr(0,8) != "https://") && (s.substr(0,6) != "ftp://") )	{
		alert("Wrong URL format");
	}
}

function openPopup(servletName, params) {
	var url = servletName + "?" + params;
	//alert(url);

	var name = servletName;

	if (servletName.indexOf(".") != -1)
		name=servletName.substr(0, servletName.indexOf("."));


	
	var features = "location=no, menubar=yes, width=750, height=600, top=50, left=30, resizable=yes, scrollbars=yes";
	var w = window.open(url,name,features);
	w.focus();
}

function openWindow(windowName) {
	var features = "";//"location=no, menubar=yes, width=750, height=600, top=50, left=30, resizable=yes, scrollbars=yes";
	var w = window.open(windowName,"",features);
	w.focus();
}

/**
*/
	function getRequestParameter(name) {
		var url = document.URL;
		var value="";

		i = url.indexOf(name + '=');

		len = name.length + 1;

		if (i > 0) {
			beg = url.substring(0,i+len);

			sStr= url.substring(i+len);
			j = sStr.indexOf('&');

			if (j > 0)
				value = sStr.substring(0,j);
			else
				value = sStr; //.substring(i+len);

		}
		else
			value="";
	
		
		//alert(value);
		return value;

	}
	function changeParamInUrl(sName, sValue){
		var sUrl, i, j,  sBeg, sEnd, sStr;
	
		sUrl = document.URL;
		i = sUrl.indexOf(sName + '=');
		if (i > 0) {
			sBeg=sUrl.substr(0, i); 
			sStr=sUrl.substr(i);
			j = sStr.indexOf('&');
			if (j > 0)
			   sEnd = sStr.substr(j);
			else
			   sEnd= '';

			sUrl=sBeg + sName + '=' + sValue + sEnd ;

			}
		else
			{

			j = sUrl.indexOf('?');
			if (j>0)
				sUrl = sUrl + '&' + sName + '=' + sValue;
			else
				sUrl = sUrl + '?' + sName + '=' + sValue;
			}
		return sUrl ;
	}


	function changeParamInString(sUrl, sName, sValue){
		var  i, j,  sBeg, sEnd, sStr;
		
		i = sUrl.indexOf(sName + '=');
		if (i > 0) {
			sBeg=sUrl.substr(0, i); 
			sStr=sUrl.substr(i);
			j = sStr.indexOf('&');
			if (j > 0)
			   sEnd = sStr.substr(j);
			else
			   sEnd= '';

			sUrl=sBeg + sName + '=' + sValue + sEnd ;

			}
		else
			{

			j = sUrl.indexOf('?');
			if (j>0)
				sUrl = sUrl + '&' + sName + '=' + sValue;
			else
				sUrl = sUrl + '?' + sName + '=' + sValue;
			}
		redirect(sUrl);
	}

	function redirect(url){
		document.location=url;
	}

/**
 *
 */
function toggleSelectAll(formname) {
  formobj = document.getElementById(formname);
  checkboxes = formobj.getElementsByTagName('input');
  var isAllSelected = (formobj.selectAll.value == "Select all")?false:true;

  if (isAllSelected == null || isAllSelected == false) {
	for (i = 0; i < checkboxes.length; i++) {
	  if (checkboxes[i].type == 'checkbox')
		checkboxes[i].checked = true ;
	}
	formobj.selectAll.value = "Deselect all";
  }
  else {
	for (i = 0; i < checkboxes.length; i++)
	  if (checkboxes[i].type == 'checkbox')
		checkboxes[i].checked = false ;
	formobj.selectAll.value = "Select all";
  }
}

function disableElement(elementId) {
	this.document.getElementById(elementId).disabled = true;
}

function hideElement(elementId) {
	this.document.getElementById(elementId).style.display = 'none';
}

