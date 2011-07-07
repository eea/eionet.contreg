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
 */
package eionet.cr.util.export;

public enum ExportFormat {
    XLS(".xls", "application/xls", "Exported_data.xls"), XML(".xml", "text/xml;charset=utf-8", "Exported_data.xml"),
    XML_WITH_SCHEMA(".xml (with XML Schema)", "text/xml;charset=utf-8", "Exported_data.xml");

    private String name;
    private String contentType;
    private String filename;

    /**
     * @param format
     */
    private ExportFormat(String format, String contentType, String filename) {
        this.name = format;
        this.contentType = contentType;
        this.filename = filename;
    }

    /**
     * @param exportFormat
     * @return
     */
    public static ExportFormat fromName(String exportFormat) {
        for (ExportFormat format : ExportFormat.values()) {
            if (format.name.equals(exportFormat)) {
                return format;
            }
        }
        return ExportFormat.XLS;
    }

    /**
     * @return the contentType
     */
    public String getName() {
        return name;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
}
