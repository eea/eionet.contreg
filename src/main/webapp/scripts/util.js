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

/*function openSpatialHist(ID) {

	var url = "spatialhistory.jsv?ID=" + ID;
	var name = "History";
	var features = "location=no, menubar=yes, width=750, height=600, top=50, left=30, scrollbars=yes";
	var w = window.open(url,name,features);
	w.focus(); 
} */


/*
function openHistory(ID,TYPE){
	var url = "history.jsv?entity=" + TYPE + "&id=" + ID;
	var name = "History";
	var features = "location=no, menubar=no, width=640, height=400, top=100, left=200, scrollbars=yes";
	var w = window.open(url,name,features);
	w.focus();

}
*/


//
// Opens edit help window
//
function openHelp(ID){
	var url = "help.jsv?helpID=" + ID;
	var name = "Help";
	var features = "location=no, menubar=no, width=730, height=480, top=100, left=200, scrollbars=no";
	var w = window.open(url,name,features);
	w.focus();
}

//
// Opens view help window
//
function openViewHelp(ID){
	var url = "viewhelp.jsv?helpID=" + ID;
	var name = "Help";
	var features = "location=no, menubar=no, width=730, height=480, top=100, left=200, scrollbars=yes, resizable=yes";
	var w = window.open(url,name,features);
	w.focus();
}

//
// Opens help list window
//
function openHelpList(mode){
	var url = "helplist.jsv?mode=" + mode;
	var name = "Help";
	var features = "location=no, menubar=no, width=730, height=480, top=100, left=200, scrollbars=yes, resizable=yes";
	var w = window.open(url,name,features);
	w.focus();
}

/**
* Opens deliveries window
*/
/*
function openDeliveries(ACT_ID, COUNTRY_ID){

	var url = "csdeliveries?ACT_DETAILS_ID=" + ACT_ID ; // + "&#038;mi6";
	url = url + "&COUNTRY_ID=" + escape(COUNTRY_ID);
	var name = "CSDeliveries";
	var features = "location=no, menubar=yes, width=750, height=600, top=50, left=30, scrollbars=yes";
	var w = window.open( url, name, features);
	w.focus();
}
*/
/**
* opens client/issuer window
*/
/*function openClient(ID){

	var url = "client.jsv?id=" + ID;
	var name = "Client";
	var features = "location=no, menubar=no, width=650, height=500, top=50, left=50, scrollbars=no, resizable=yes";
	var w = window.open(url,name,features);
	w.focus();

}*/

/**
* Link to Circa
*/
function openCirca(url){
	var name = "CSCIRCA";
	var features = "location=yes, menubar=yes, width=750, height=600, top=30, left=30, resizable=yes, scrollbars=yes";
	var w = window.open( url, name, features);
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
