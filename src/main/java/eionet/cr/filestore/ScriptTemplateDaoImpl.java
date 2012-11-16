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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.filestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.dao.ScriptTemplateDAO;
import eionet.cr.dto.ScriptTemplateDTO;

/**
 * Script template DAO implementation.
 *
 * @author Juhan Voolaid
 */
public class ScriptTemplateDaoImpl implements ScriptTemplateDAO {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ScriptTemplateDaoImpl.class);

    /** */
    private static final String PROPERTIES_FILE_NAME = "linkScripts.xml";

    /** */
    private static final Properties PROPERTIES = new Properties();

    /**
     *
     */
    static {
        try {
            PROPERTIES.loadFromXML(ScriptTemplateDaoImpl.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME));
        } catch (Exception e) {
            LOGGER.error("Failed to load properties from " + PROPERTIES_FILE_NAME, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScriptTemplateDTO> getScriptTemplates() {

        Map<String, ScriptTemplateDTO> scripts = new HashMap<String, ScriptTemplateDTO>();
        for (Object key : PROPERTIES.keySet()) {
            String id = StringUtils.substringBefore((String) key, ".");
            String property = StringUtils.substringAfterLast((String) key, ".");

            ScriptTemplateDTO script = scripts.get(id);
            if (script == null) {
                script = new ScriptTemplateDTO();
                script.setId(id);
                scripts.put(id, script);
            }

            if (property.equals("name")) {
                script.setName(PROPERTIES.getProperty((String) key).trim());
            }

            if (property.equals("script")) {
                script.setScript(PROPERTIES.getProperty((String) key).trim());
            }

        }

        return new ArrayList<ScriptTemplateDTO>(scripts.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptTemplateDTO getScriptTemplate(String id) {
        ScriptTemplateDTO result = new ScriptTemplateDTO();
        result.setId(id);
        for (Object key : PROPERTIES.keySet()) {
            String currentId = StringUtils.substringBefore((String) key, ".");
            String property = StringUtils.substringAfterLast((String) key, ".");

            if (currentId.equals(id)) {
                if (property.equals("name")) {
                    result.setName(PROPERTIES.getProperty((String) key).trim());
                }

                if (property.equals("script")) {
                    result.setScript(PROPERTIES.getProperty((String) key).trim());
                }
            }
        }
        return result;
    }

}
