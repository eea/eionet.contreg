/**
 * Provides classes that form the Virtuoso-specific plugin for Liquibase.
 * It all starts to thread from the {@link eionet.liquibase.VirtuosoDatabase} class.
 * {@link eionet.cr.util.liquibase.CRLiquibaseServletListener} is the servlet context listener that ensures that the
 * {@link eionet.liquibase.VirtuosoDatabase} class gets registered in Liquibase, and after that the processing is
 * passed on to the original Liquibase servlet context listener.
 */

package eionet.cr.util.liquibase;

