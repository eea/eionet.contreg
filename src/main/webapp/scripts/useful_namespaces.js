
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

function handlePrefixClick(prefix) {
    var query = document.getElementById('queryText').value;
    document.getElementById('queryText').value = addOrRemove(query, prefix);

    var areas = document.querySelectorAll('.expandingArea');
    var l = areas.length;
    while (l--) {
        makeExpandingArea(areas[l]);
    }

    return false;
}

function addOrRemove(original, prefix) {
    if (stringExists(original, prefix)) {
        return removeString(original, prefix);
    } else {
        return addString(original, prefix);
    }
}

function stringExists(string, subString) {
    if (string.length < 1) {
        return false;
    }
    if (subString.length < 1) {
        return false;
    }
    if (string.indexOf(subString) == -1) {
        return false;
    }
    return true;
}

function addString(string, addable) {
    return addable + "\n" + string;
}

function removeString(string, removable) {
    if (stringExists(string, removable + "\n")) {
        return string.replace(removable + "\n", "");
    } else {
        return string.replace(removable, "");
    }
}