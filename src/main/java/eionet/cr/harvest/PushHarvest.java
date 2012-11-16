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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.harvest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PushHarvest extends BaseHarvest {

    /** */
    protected String pushedContent = null;

    /**
     *
     * @param contextUrl
     * @throws HarvestException
     */
    public PushHarvest(String contextUrl, String pushedContent) throws HarvestException {

        super(contextUrl);
        this.pushedContent = pushedContent;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.BaseHarvest#doHarvest()
     */
    @Override
    protected void doHarvest() throws HarvestException {

        if (StringUtils.isEmpty(pushedContent)) {
            throw new HarvestException("Pushed content must not be empty!");
        }

        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(pushedContent.getBytes("UTF-8"));
            int noOfTriples = getHarvestSourceDAO().loadIntoRepository(inputStream, null, getContextUrl(), false);
            setStoredTriplesCount(noOfTriples);
        } catch (UnsupportedEncodingException e) {
            throw new HarvestException(e.getMessage(), e);
        } catch (IOException e) {
            throw new HarvestException(e.getMessage(), e);
        } catch (OpenRDFException e) {
            throw new HarvestException(e.getMessage(), e);
        }
    }

    /**
     * @see eionet.cr.harvest.BaseHarvest#getHarvestType()
     */
    @Override
    protected String getHarvestType() {

        return HarvestConstants.TYPE_PUSH;
    }

    /**
     * @see eionet.cr.harvest.BaseHarvest#isBeingHarvested(java.lang.String)
     */
    @Override
    public boolean isBeingHarvested(String url) {
        boolean ret = false;
        if (url != null) {
            if (url.equals(getContextUrl())) {
                ret = true;
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#afterFinishActions()
     */
    @Override
    protected void afterFinish() {
        // TODO Auto-generated method stub

    }
}
