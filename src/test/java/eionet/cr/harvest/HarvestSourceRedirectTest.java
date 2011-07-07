package eionet.cr.harvest;

import org.dbunit.dataset.IDataSet;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class HarvestSourceRedirectTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    /**
     *
     */
    public void testNothing() {

        // TODO
        // Dummy test method until the actual out-commented test methods below
        // have been become testable. This requires either usage of Maven Jetty plugin
        // or somehow mocking the redirection. The former option did start working after
        // several tries, the latter option requires too much refactoring.

    }

    /**
     * This test should test more than 4 redirections and throw harvest exception.
     */

    // @Test
    // public void testHarvestRedirectedURLsMoreThan4(){
    //
    // try {
    //
    // String url = "http://localhost:8080/url-redirect-testcase/url1.jsp";
    // try {
    // DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(url, 200, false, "bob@europe.eu");
    // }
    // catch (DAOException ex){
    // }
    //
    // PullHarvest harvest = new PullHarvest(url, null);
    // harvest.execute();
    //
    // fail("Should have thrown too many redirections exception");
    // }
    // catch (HarvestException e) {
    // // If test reaches exception, it is considered successful.
    // assertTrue (true);
    // }
    // }
    //
    // /**
    // * This test should test up to 4 redirections and throw harvest exception as the original source is not found in DB.
    // */
    //
    // @Test
    // public void testHarvestRedirectedOriginalSourceNotInDB(){
    //
    // try {
    //
    // String url = "http://localhost:8080/url-redirect-testcase/url2.jsp";
    //
    // PullHarvest harvest = new PullHarvest(url, null);
    // harvest.execute();
    //
    // fail("Should have thrown exception for not having the source in DB");
    // }
    // catch (HarvestException e) {
    // // If test reaches exception, it is considered successful.
    // assertTrue (true);
    // }
    // }
    //
    //
    //
    //
    // /**
    // * This test should test up to 4 redirections and harvest all the sources.
    // */
    //
    // @Test
    // public void testHarvestRedirectedURLUpTo4(){
    //
    // try {
    //
    // String url = "http://localhost:8080/url-redirect-testcase/url2.jsp";
    //
    // HarvestSourceDTO harvestSource = new HarvestSourceDTO();
    // harvestSource = new HarvestSourceDTO();
    // harvestSource.setUrl(url);
    // harvestSource.setEmails("bob@europe.eu");
    // harvestSource.setIntervalMinutes(200);
    // try {
    // DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(url, 200, false, "bob@europe.eu");
    // }
    // catch (DAOException ex){
    // }
    //
    // PullHarvest harvest = new PullHarvest(url, null);
    // harvest.execute();
    //
    // assertNotNull(harvest.getSourceAvailable());
    // assertTrue(harvest.getSourceAvailable().booleanValue()); // This source is available
    // assertEquals((int)1, harvest.getDistinctSubjectsCount());
    // assertEquals((int)6, harvest.getStoredTriplesCount());
    // }
    // catch (HarvestException e) {
    // e.printStackTrace();
    // fail("Was not expecting this exception: " + e.toString());
    // }
    // }
    //
    // /**
    // * This test should test http://rod.eionet.europa.eu/clients/10.
    // */
    //
    // @Test
    // public void testSpecificUrl_1(){
    //
    // try {
    //
    // String url = "http://rod.eionet.europa.eu/clients/10";
    //
    // HarvestSourceDTO harvestSource = new HarvestSourceDTO();
    // harvestSource = new HarvestSourceDTO();
    // harvestSource.setUrl(url);
    // harvestSource.setEmails("bob@europe.eu");
    // harvestSource.setIntervalMinutes(200);
    // try {
    // DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(url, 200, false, "bob@europe.eu");
    // }
    // catch (DAOException ex){
    // }
    //
    // PullHarvest harvest = new PullHarvest(url, null);
    // harvest.execute();
    //
    // // If we get this far without exception, it's OK.
    // assertTrue (true);
    // }
    // catch (HarvestException e) {
    // // If test reaches exception, it is considered failed.
    // assertTrue (false);
    // }
    // }

}
