INSERT INTO harvest_source (harvest_source_id, url_hash, url, time_created, last_harvest_id, last_harvest, interval_minutes, is_online_csv_tsv, csv_tsv_url, count_unavail)
VALUES (9, '-4591620959060771803', 'http://127.0.0.1:8080/cr3/home/tourikas/download.csv', '2017-07-04 16:00:02', 9, '2017-07-04 16:01:02', 60, 'Y', 'https://www.eea.europa.eu/data-and-maps/daviz/sds/tourism-overnights-per-season/download.csv', 0);

INSERT INTO harvest_source (harvest_source_id, url_hash, url, csv_tsv_url, count_unavail)
VALUES (6, '-4591620959060771800', 'http://127.0.0.1:8080/cr3/home/tourikas/download.csv', 'https://www.eea.europa.eu/data-and-maps/daviz/sds/tourism-overnights-per-season/download.csv', 0);

INSERT INTO harvest_source (harvest_source_id, url_hash, url, time_created, last_harvest, interval_minutes, is_online_csv_tsv, csv_tsv_url, count_unavail)
VALUES (7, '-4591620959060771801', 'http://127.0.0.1:8080/cr3/home/tourikas/download.csv', '2017-07-04 16:00:02', '2017-07-04 17:00:02', 60, 'Y', 'https://www.eea.europa.eu/data-and-maps/daviz/sds/tourism-overnights-per-season/download.csv', 0);

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created, last_harvest_id)
VALUES (2, '-4591620959060771807', 'http://rod.eionet.europa.eu/countries', 'jaanus.heinlaid@tietoenator.com', '2008-02-28 10:33:48.0', 30);

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created)
VALUES (3, '403633679657319373', 'http://rod.eionet.europa.eu/instruments', 'jaanus.heinlaid@tietoenator.com', '2008-02-28 10:34:10.0');

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created)
VALUES (1, '2951453010645159546', 'http://rod.eionet.europa.eu/obligations', 'jaanus.heinlaid@tietoenator.com', '2008-02-28 10:34:22.0');

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created, last_harvest_id)
VALUES (4, '-2760457918174006820', 'http://www.eionet.europa.eu/seris/rdf', 'jaanus.heinlaid@tietoenator.com', '2008-02-28 10:39:40.0', 29);

INSERT INTO harvest_source (harvest_source_id, url_hash, url, emails, time_created)
VALUES (5, '-6409923175068484775', 'http://localhost:8080/cr/pages/test.xml', 'bob@europe.eu', '2008-02-28 16:59:19.0');



INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (1, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:15:36.0', '2008-02-28 18:16:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (2, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:16:36.0', '2008-02-28 18:17:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (3, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:17:36.0', '2008-02-28 18:18:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (4, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:18:36.0', '2008-02-28 18:19:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (5, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:19:36.0', '2008-02-28 18:20:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (6, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:20:36.0', '2008-02-28 18:21:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (7, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:21:36.0', '2008-02-28 18:22:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (8, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:22:36.0', '2008-02-28 18:23:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (9, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:23:36.0', '2008-02-28 18:24:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (10, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:24:36.0', '2008-02-28 18:25:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, http_code)
VALUES (11, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:25:36.0', 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (12, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:26:36.0', '2008-02-28 18:27:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (13, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:27:36.0', '2008-02-28 18:28:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (14, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:28:36.0', '2008-02-28 18:29:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (15, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:29:36.0', '2008-02-28 18:30:01.0', 2378, 1337, 1041, 150, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (16, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:30:36.0', '2008-02-28 18:31:01.0', 2378, 1337, 1041, 150, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (17, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:31:36.0', '2008-02-28 18:32:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (18, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:32:36.0', '2008-02-28 18:33:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (19, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:33:36.0', '2008-02-28 18:34:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (20, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:34:36.0', '2008-02-28 18:35:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (21, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:35:36.0', '2008-02-28 18:36:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (22, 1, 'pull', 'heinlja', 'finished', '2008-02-28 18:36:36.0', '2008-02-28 18:37:01.0', 22043, 6390, 15653, 481, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (23, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:37:36.0', '2008-02-28 18:38:01.0', 0, 0, 0, 0, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (24, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:38:36.0', '2008-02-28 18:39:01.0', 40, 21, 19, 7, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (25, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:39:36.0', '2008-02-28 18:40:01.0', 0, 0, 0, 0, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (26, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:40:36.0', '2008-02-28 18:41:01.0', 0, 0, 0, 0, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (27, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:41:36.0', '2008-02-28 18:42:01.0', 0, 0, 0, 0, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (28, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:42:36.0', '2008-02-28 18:43:01.0', 40, 21, 19, 7, 200);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (29, 4, 'pull', 'heinlja', 'finished', '2008-02-28 18:43:36.0', '2008-02-28 18:44:01.0', 40, 21, 19, 7, 401);

INSERT INTO harvest (harvest_id, harvest_source_id, type, username, status, started, finished, tot_statements, lit_statements, res_statements, enc_schemes, http_code)
VALUES (30, 2, 'pull', 'heinlja', 'finished', '2008-02-28 18:44:36.0', '2008-02-28 18:45:01.0', 40, 21, 19, 7, 401);



INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (1, 1, 'wrn', 'There something funny in the XML file', 'stack(There something funny in the XML file)');

INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (2, 2, 'err', 'This is an error, I quit', 'stack(This is an error, I quit)');

INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (3, 5, 'wrn', 'Nothing harmful, just this little thing', 'stack (Nothing harmful, just this little thing)');

INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (4, 5, 'err', 'ERROR', 'Stack Trace: whatever');

INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (5, 5, 'wrn', 'WARNING', 'Stack Trace: warning - life is like a box of chocolates');

INSERT INTO harvest_message (harvest_message_id, harvest_id, type, message, stack_trace)
VALUES (6, 5, 'ftl', 'eionet.cr.harvest.HarvestException: org.xml.sax.SAXParseException: testFatal', 'eionet.cr.harvest.HarvestException: org.xml.sax.SAXParseException: testFatal
    at eionet.cr.harvest.Harvest.harvestFile(Harvest.java:163)
    at eionet.cr.harvest.PullHarvest.doExecute(PullHarvest.java:42)
    at eionet.cr.harvest.Harvest.execute(Harvest.java:99)
    at eionet.cr.web.action.HarvestSourceActionBean.harvestNow(HarvestSourceActionBean.java:170)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
    at java.lang.reflect.Method.invoke(Method.java:585)
    at net.sourceforge.stripes.controller.DispatcherHelper$6.intercept(DispatcherHelper.java:445)
    at net.sourceforge.stripes.controller.ExecutionContext.proceed(ExecutionContext.java:157)
    at net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor.intercept(BeforeAfterMethodInterceptor.java:107)
    at net.sourceforge.stripes.controller.ExecutionContext.proceed(ExecutionContext.java:154)
    at net.sourceforge.stripes.controller.ExecutionContext.wrap(ExecutionContext.java:73)
    at net.sourceforge.stripes.controller.DispatcherHelper.invokeEventHandler(DispatcherHelper.java:443)
    at net.sourceforge.stripes.controller.DispatcherServlet.invokeEventHandler(DispatcherServlet.java:241)
    at net.sourceforge.stripes.controller.DispatcherServlet.doPost(DispatcherServlet.java:154)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:709)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:802)
    at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:252)
    at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:173)
    at net.sourceforge.stripes.controller.StripesFilter.doFilter(StripesFilter.java:180)
    at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:202)
    at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:173)
    at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:213)
    at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:178)
    at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:126)
    at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:105)
    at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:107)
    at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:148)
    at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:869)
    at org.apache.coyote.http11.Http11BaseProtocol$Http11ConnectionHandler.processConnection(Http11BaseProtocol.java:664)
    at org.apache.tomcat.util.net.PoolTcpEndpoint.processSocket(PoolTcpEndpoint.java:527)
    at org.apache.tomcat.util.net.LeaderFollowerWorkerThread.runIt(LeaderFollowerWorkerThread.java:80)
    at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:684)
    at java.lang.Thread.run(Thread.java:595)
Caused by: org.xml.sax.SAXParseException: testFatal
    at eionet.cr.harvest.RDFHandler.&lt;init&gt;(RDFHandler.java:86)
    at eionet.cr.harvest.Harvest.harvestFile(Harvest.java:142)
    ... 34 more');
