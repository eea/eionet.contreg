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
 *        jaanus
 */

package eionet.cr.util.sesame;

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.DecimalLiteralImpl;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.CRException;

/**
 * Type definition ...
 *
 * @author jaanus
 */
public class ResultCompareUtil {

    /**
     *
     * @param tqr1
     * @param tqr2
     * @return
     * @throws QueryEvaluationException
     */
    public static boolean equals(TupleQueryResult tqr1, TupleQueryResult tqr2) throws QueryEvaluationException {
        List<BindingSet> list1 = Iterations.asList(tqr1);
        List<BindingSet> list2 = Iterations.asList(tqr2);

        // Compare the number of statements in both sets
        if (list1.size() != list2.size()) {
            return false;
        }

        return matchBindingSets(list1, list2);
    }

    /**
     *
     * @param queryResult1
     * @param queryResult2
     * @return
     */
    private static boolean matchBindingSets(List<? extends BindingSet> queryResult1, Iterable<? extends BindingSet> queryResult2) {
        return matchBindingSets(queryResult1, queryResult2, new HashMap<BNode, BNode>(), 0);
    }

    /**
     *
     * @param queryResult1
     * @param queryResult2
     * @param bNodeMapping
     * @param idx
     * @return
     */
    private static boolean matchBindingSets(List<? extends BindingSet> queryResult1, Iterable<? extends BindingSet> queryResult2,
            Map<BNode, BNode> bNodeMapping, int idx) {

        boolean result = false;

        if (idx < queryResult1.size()) {
            BindingSet bs1 = queryResult1.get(idx);

            if (idx % 5000 == 0) {
                System.out.println("checking idx #" + idx);
            }
            List<BindingSet> matchingBindingSets = findMatchingBindingSets(bs1, queryResult2, bNodeMapping);

            if (matchingBindingSets.isEmpty()) {
                System.out.println("Result2 has no match for result1's BindingSet #" + idx + ":\n" + toString(bs1));
            }

            for (BindingSet bs2 : matchingBindingSets) {
                // Map bNodes in bs1 to bNodes in bs2
                Map<BNode, BNode> newBNodeMapping = new HashMap<BNode, BNode>(bNodeMapping);

                for (Binding binding : bs1) {
                    if (binding.getValue() instanceof BNode) {
                        newBNodeMapping.put((BNode) binding.getValue(), (BNode) bs2.getValue(binding.getName()));
                    }
                }

                // FIXME: this recursive implementation has a high risk of
                // triggering a stack overflow

                // Enter recursion
                result = matchBindingSets(queryResult1, queryResult2, newBNodeMapping, idx + 1);

                if (result == true) {
                    // models match, look no further
                    break;
                }
            }
        } else {
            // All statements have been mapped successfully
            result = true;
        }

        return result;
    }

    /**
     *
     * @param bs
     * @return
     */
    private static String toString(BindingSet bs) {

        ArrayList<String> list = new ArrayList<String>();
        for (String name : bs.getBindingNames()) {
            list.add(bs.getValue(name).stringValue());
        }
        return list.toString();
    }

    /**
     *
     * @param st
     * @param model
     * @param bNodeMapping
     * @return
     */
    private static List<BindingSet> findMatchingBindingSets(BindingSet st, Iterable<? extends BindingSet> model,
            Map<BNode, BNode> bNodeMapping) {

        List<BindingSet> result = new ArrayList<BindingSet>();

        for (BindingSet modelSt : model) {
            if (bindingSetsMatch(st, modelSt, bNodeMapping)) {
                // All components possibly match
                result.add(modelSt);
            }
        }

        return result;
    }

    /**
     *
     * @param bs1
     * @param bs2
     * @param bNodeMapping
     * @return
     */
    private static boolean bindingSetsMatch(BindingSet bs1, BindingSet bs2, Map<BNode, BNode> bNodeMapping) {

        if (bs1.size() != bs2.size()) {
            return false;
        }

        for (Binding binding1 : bs1) {
            Value value1 = binding1.getValue();
            Value value2 = bs2.getValue(binding1.getName());

            if (value1 instanceof BNode && value2 instanceof BNode) {
                BNode mappedBNode = bNodeMapping.get(value1);

                if (mappedBNode != null) {
                    // bNode 'value1' was already mapped to some other bNode
                    if (!value2.equals(mappedBNode)) {
                        // 'value1' and 'value2' do not match
                        return false;
                    }
                } else {
                    // 'value1' was not yet mapped, we need to check if 'value2' is a
                    // possible mapping candidate
                    if (bNodeMapping.containsValue(value2)) {
                        // 'value2' is already mapped to some other value.
                        return false;
                    }
                }
            } else {

                if (!StringUtils.equals(value1.stringValue(), value2.stringValue())) {
                    return false;
                }
                // values are not (both) bNodes
                if (value1 instanceof Literal && value2 instanceof Literal) {
                    // do literal value-based comparison for supported datatypes
                    Literal leftLit = (Literal) value1;
                    Literal rightLit = (Literal) value2;

                    URI dt1 = leftLit.getDatatype();
                    URI dt2 = rightLit.getDatatype();

                    if (dt1 != null && dt2 != null && dt1.equals(dt2) && XMLDatatypeUtil.isValidValue(leftLit.getLabel(), dt1)
                            && XMLDatatypeUtil.isValidValue(rightLit.getLabel(), dt2)) {
                        Integer compareResult = null;
                        if (dt1.equals(XMLSchema.DOUBLE)) {
                            compareResult = Double.compare(leftLit.doubleValue(), rightLit.doubleValue());
                        } else if (dt1.equals(XMLSchema.FLOAT)) {
                            compareResult = Float.compare(leftLit.floatValue(), rightLit.floatValue());
                        } else if (dt1.equals(XMLSchema.DECIMAL)) {
                            compareResult = leftLit.decimalValue().compareTo(rightLit.decimalValue());
                        } else if (XMLDatatypeUtil.isIntegerDatatype(dt1)) {
                            compareResult = leftLit.integerValue().compareTo(rightLit.integerValue());
                        } else if (dt1.equals(XMLSchema.BOOLEAN)) {
                            Boolean leftBool = Boolean.valueOf(leftLit.booleanValue());
                            Boolean rightBool = Boolean.valueOf(rightLit.booleanValue());
                            compareResult = leftBool.compareTo(rightBool);
                        } else if (XMLDatatypeUtil.isCalendarDatatype(dt1)) {
                            XMLGregorianCalendar left = leftLit.calendarValue();
                            XMLGregorianCalendar right = rightLit.calendarValue();

                            compareResult = left.compare(right);
                        }

                        if (compareResult != null) {
                            if (compareResult.intValue() != 0) {
                                return false;
                            }
                        } else if (!value1.equals(value2)) {
                            return false;
                        }
                    } else if (!value1.equals(value2)) {
                        return false;
                    }
                } else if (!value1.equals(value2)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     *
     * @param result1
     * @param result2
     * @return
     * @throws Exception
     */
    private static boolean compare(TupleQueryResult result1, TupleQueryResult result2) throws Exception {

        int numOfUnequalRows = 0;
        int i = -1;
        boolean hasNext1 = false;
        boolean hasNext2 = false;
        do {
            i++;

            if (i % 5000 == 0) {
                System.out.println("At row#" + i);
            }

            hasNext1 = result1.hasNext();
            hasNext2 = result2.hasNext();
            if (hasNext1 && hasNext2) {

                BindingSet bindingSet1 = result1.next();
                BindingSet bindingSet2 = result2.next();
                Set<String> names1 = bindingSet1.getBindingNames();
                if (i == 0) {

                    Set<String> names2 = bindingSet1.getBindingNames();
                    if (!names1.equals(names2)) {
                        throw new Exception("Binding sets not equal:\n" + names1 + "\n" + names2);
                    }
                }

                ArrayList<String> values1 = new ArrayList<String>();
                ArrayList<String> values2 = new ArrayList<String>();
                for (String name : names1) {

                    Value value1 = bindingSet1.getValue(name);
                    Value value2 = bindingSet2.getValue(name);

                    String str1 = value1.stringValue();
                    String str2 = value2.stringValue();

                    values1.add("0.0".equals(str1) ? "0" : str1);
                    values2.add("0.0".equals(str2) ? "0" : str2);
                }

                if (!values1.equals(values2)) {
                    numOfUnequalRows++;
                    System.out.println("Row #" + i + " is different:\n1: " + values1 + "\n2: " + values2);
                    return false;
                }
            }
        } while (hasNext1 && hasNext2);

        System.out.println("numOfUnequalRows = " + numOfUnequalRows);

        if (!hasNext1 && hasNext2) {
            throw new CRException("At row#" + i + " result2 has next, but result1 does not!");
        }
        else if (hasNext1 && !hasNext2) {
            throw new CRException("At row#" + i + " result1 has next, but result2 does not!");
        }

        return numOfUnequalRows == 0;
    }

    /**
     *
     * @param value
     * @return
     */
    private static boolean isNumber(Value value) {
        return value instanceof DecimalLiteralImpl || value instanceof IntegerLiteralImpl || value instanceof NumericLiteralImpl;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //        String query1 = "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/29> where {?s ?p ?o} order by ?s ?p";
        //        String query2 =
        //                "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/manualHH> where {?s ?p ?o} order by ?s ?p";

        //        String query1 = "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/30> where {?s ?p ?o} order by ?s ?p";
        //        String query2 = "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/ent1_Andrei> where {?s ?p ?o} order by ?s ?p";

        String query1 = "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/32> where {?s ?p ?o} order by ?s ?p";
        String query2 = "select ?s ?p ?o from <http://semantic.digital-agenda-data.eu/import/ent2_Andrei> where {?s ?p ?o} order by ?s ?p";

        TupleQueryResult result1 = null;
        TupleQueryResult result2 = null;
        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery1 = repoConn.prepareTupleQuery(QueryLanguage.SPARQL, query1);
            TupleQuery tupleQuery2 = repoConn.prepareTupleQuery(QueryLanguage.SPARQL, query2);
            System.out.println("Evaluating query1 ...");
            result1 = tupleQuery1.evaluate();
            System.out.println("Evaluating query2 ...");
            result2 = tupleQuery2.evaluate();
            System.out.println("Comparing ...");
            boolean b = compare(result1, result2);
            System.out.println("result: " + b);
        } finally {
            SesameUtil.close(result1);
            SesameUtil.close(result2);
            SesameUtil.close(repoConn);
        }
    }
}
