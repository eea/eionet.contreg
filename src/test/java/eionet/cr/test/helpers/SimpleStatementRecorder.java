package eionet.cr.test.helpers;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * An extremely simple extension of {@link RDFHandlerBase}. See further documentation there.
 * This implementation records RDF statements as POJOs of 3 strings, and allows to ask if a particular statement has been recorded.
 *
 * @author Jaanus
 * @see RDFHandlerBase
 */
public class SimpleStatementRecorder extends RDFHandlerBase {

    /** Hash-set of the recorded statements. */
    private HashSet<SimpleStatement> recordedStatements = new HashSet<SimpleStatement>();

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.helpers.RDFHandlerBase#handleStatement(org.openrdf.model.Statement)
     */
    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {

        String subject = statement.getSubject().stringValue();
        String predicate = statement.getPredicate().stringValue();
        String object = statement.getObject().stringValue();
        SimpleStatement simpleStatement = new SimpleStatement(subject, predicate, object);
        recordedStatements.add(simpleStatement);
    }

    /**
     * Returns true if this statement recorder has recorded the given statement. Otherwise returns true.
     *
     * @param subject The given statement's subject.
     * @param predicate The given statement's predicate.
     * @param object The given statement's object.
     * @return The boolean.
     */
    public boolean hasStatement(String subject, String predicate, String object) {

        return recordedStatements.contains(new SimpleStatement(subject, predicate, object));
    }

    /**
     * Returns the number of statements recorded by this recorder.
     *
     * @return The number.
     */
    public int getNumberOfRecordedStatements() {
        return recordedStatements.size();
    }

    /**
     * A simple POJO representing an RDF statement as 3 strings.
     *
     * @author Jaanus
     */
    private static class SimpleStatement {

        /** The statement's subject. */
        private String subject;

        /** The statement's predicate. */
        private String predicate;

        /** The statement's object. */
        private String object;

        /** The object's hash-code as {@link Object#hashCode()}. */
        private int hashCode;

        /**
         * Default constructor.
         *
         * @param subject
         * @param predicate
         * @param object
         */
        SimpleStatement(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            hashCode = new HashCodeBuilder().append(subject).append(predicate).append(object).toHashCode();
        }

        /**
         * @return the subject
         */
        String getSubject() {
            return subject;
        }

        /**
         * @return the predicate
         */
        String getPredicate() {
            return predicate;
        }

        /**
         * @return the object
         */
        String getObject() {
            return object;
        }

        /**
         * Overrides java.lang.Object#equals(java.lang.Object).
         * Implemented because we need to add objects if this class into hashmap/set.
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (!(obj instanceof SimpleStatement)) {
                return false;
            }

            SimpleStatement ss = (SimpleStatement) obj;

            return StringUtils.equals(subject, ss.getSubject()) && StringUtils.equals(predicate, ss.getPredicate())
                    && StringUtils.equals(object, ss.getObject());
        }

        /**
         * Overrides java.lang.Object#hashCode().
         * Implemented because we need to add objects if this class into hashmap/set.
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
