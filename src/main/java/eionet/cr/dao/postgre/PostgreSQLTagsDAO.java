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
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.dao.postgre;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.TagsDAO;
import eionet.cr.dto.TagDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.sql.PairReader;

/**
 *
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */

public class PostgreSQLTagsDAO extends PostgreSQLBaseDAO implements TagsDAO {

    private static final String getTagsWithFrequencies_SQL =
        "select OBJECT as LCOL, count(*) as RCOL from SPO where PREDICATE =? " +
            "group by object order by RCOL desc";

    @Override
    public List<TagDTO> getTagCloud() throws DAOException {

        PairReader<String, Long> pairReader = new PairReader<String, Long>();
        List<Long> params = new ArrayList<Long>();
        params.add(Hashes.spoHash(Predicates.CR_TAG));

        executeSQL(getTagsWithFrequencies_SQL, params, pairReader);
        List<Pair<String,Long>> pairList = pairReader.getResultList();

        List<TagDTO> resultList = new ArrayList<TagDTO>(pairList.size());
        int maxTagCount = pairList.size() > 0 ? pairList.get(0).getRight().intValue() : 0;

        for (Pair<String, Long> tagPair : pairList) {
            resultList.add(new TagDTO(tagPair.getLeft(), tagPair.getRight().intValue(), maxTagCount));
        }
        return resultList;
    }

}
