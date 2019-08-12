# EIONET Content Registry (CR) software.

CR is a search engine for structured data. The data is collected
from various Semantic Web / Linked Data nodes on the Internet,
and stored as RDF triples in a triple store.

The following sections describe how to install and run CR.
Installation instructions can also be found at https://taskman.eionet.europa.eu/projects/reportnet/wiki/CR_Installation_Guide

## 1. Download and install Java, Tomcat and Maven.

CR runs on the Java platform, and has been tested to run on Tomcat Java Servlet Container.
CR source code is built with Maven.

Please download all of these software and install them according to the instructions
found at their websites.

The necessary versions are as follows:

* Java 1.8
* Maven 3.3.9
* Tomcat 8.5.20


## 2. Download and install Virtuoso

CR uses OpenLink Virtuoso as its backend for relational database and triple
store. Download Open-Source Edition of Virtuoso from here
http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main

CR has been tested and run on Open-Source Virtuoso version 6.1.5.

To install Virtuoso's Open-Source Edition, follow these guidelines:

* http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSUsageWindows (Windows)
* http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSCentosNotes  (CentOS Linux)
* http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDebianNotes  (Debian GNU/Linux)
* http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSFedoraNotes  (Fedora Core Linux)
* http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSUbuntuNotes  (Ubuntu Linux)

There's more useful information about Virtuoso's Open-Source Edition here:
http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSIndex

NB!!! For security reasons, be sure to change your Virtuoso administrator password to
something other than the default!

CR will ask virtuoso to load files from the temporary directory. You must ensure
the the values for harvester.tempFileDir and filestore.path in local.properties are
listed for DirsAllowed in virtuoso.ini.

## 3. Download, configure and build CR source code

The CR source code is kept in GitHub repository at https://github.com/eea/eionet.contreg

Install a Git client of your choice, and clone CR source code into a directory
that is denoted by CR_SOURCE_HOME in the below instructions.

Before you can build CR source code, you need to set your environment specific properties.
For that, make a copy of unittest.properties in CR_SOURCE_HOME, and rename it to local.properties.
Go through the resulting file and change properties that are specific to your environment or wishes.
Each property's exact meaning and effect is commented in the file.

Now you are ready to build your CR code. It is built with Maven.
The following command assumes that Maven's executable (mvn) is on the command path,
and that it is run while being in CR_SOURCE_HOME directory:

    shell> mvn clean install

### 3.1 Unit tests

When you ran the above command you probably noticed that it ran the unit tests. In case you don't want to, you can add `-Dmaven.test.skip=true` argument.

## 4. Import CR database creation scripts

The following commands shall import scripts that create necessary users,
table structures, indexes and other CR setup information into Virtuoso.

All of these commands use Virtuoso's Interactive SQL (ISQL) utility to import the scripts.
They assume Virtuoso is running on localhost, and listens to its default port which is 1111.
Please change these on the command line accordingly if this is not the case.

Options -U and -P indicate user and password under which the command is run.
All the below commands shall be executed as 'dba' user.

The scripts are located in CR_SOURCE_HOME/sql/virtuoso/install directory,
and the following commands should be executed while being in that directory.
The commands also assume that ISQL executable is on the command path
(it is located in VIRTUOSO_HOME/bin directory).

Create necessary Virtuoso users for CR:

    shell> isql localhost:1111 -U dba -P password < 1_create_users.sql

Set up the triple store's full text indexing

    shell> isql localhost:1111 -U dba -P password < 2_setup_full_text_indexing.sql

## 5. Integration tests

There are 2 ways to run integration tests: using a docker or manually.

### 5.1 Automated test execution using Docker

You need to have docker installed (on Windows it's called Docker Desktop). Then execute the following command: 

    shell> mvn clean verify

The integration tests runner (ie Maven's Failsafe plugin) will download (only when executed for the first time)
and start start a Virtuoso docker container (see inside `pom.xml` for the particular docker image it downloads)
that will be used by the tests. It will then start that container.

The tests runner will also start a dockerized Apache Web Server container (ie httpd) for serving files
that the tests will be trying to access over HTTP (these are typically CR harevster tests).

Finally, the tests runner will run all tests, and it will eventually tear down the started docker containers.

**NB!** The tests runner will attempt to create a directory defined by the `config.app.home` property in `tests.properties`.
This is the directory where integration tests will create some temporaey files they need to use.
If for some reason it fails to create that directory or you want to use a different location, then you can override
this property by using `-Dconfig.app.home` option on command line, like this:

    shell> mvn -Dconfig.app.home=/some/other/dir clean verify

**NB!** Other properties noteworthy from the integration tests point of view in `tests.properties` are

* `tests.virtuoso.host` - the host for the dockerized Virtuoso test instance (defaults to "localhost" and usually no need to change it);
* `tests.virtuoso.port` - the port for the dockerized Virtuoso test instance (defaults to "1112" and usually no need to change it);
* `tests.httpd.host` - the host for the dockerized Httpd test instance (defaults to "localhost" and usually no need to change it);
* `tests.httpd.port` - the port for the dockerized Httpd test instance (defaults to "8080" and usually no need to change it);
    
### 5.2 Manual Test Execution

By manual test execution it is meant that if you don't have docker installed or don't want to use the above-described
dockerized automated test environment, then you need to manually run a test Virtuoso instance and also a web server
for serving the files that integration tests will attempt to access over HTTP.

For being able to run this manual test execution, you need to execute Maven build with the "local" profile,
because by default it is run with the "docker" profile. For running it with "local" profile, you need to execute this:

    shell> mvn -P local clean verify

But before doing so, you need to ensure that you have manually started the above mentioned test instances of Virtuoso
and web server. It is up to you who exactly you will do it. The important point is that you need to ensure that the
properties mentioned in previous chapter will point to your Virtuoso and web server instances.

## 6. Conditional: register Eionet's GlobalSign CA certificates in your JVM.

This step is required only if you configured CR to use EEA's  Central Authentication Service (CAS) in step 3.
In other words: if you pointed edu.yale.its.tp.cas.client.filter.loginUrl and edu.yale.its.tp.cas.client.filter.validateUrl
to EEA's CAS server. In such a case you need to register Eionet's GlobalSign CA certificates in the JVM that runs
the Tomcat where you deploy CR.

The steps are as follows (note that the expected password for the keystore is "changeit", but don't change it):

a) Go to [http://www.eionet.europa.eu/certificates] and download the certificates called
GlobalSign-Root-CA.crt and GlobalSign-Domain-Validation-CA.crt to a temporary directory.

b) Check that the GlobalSign-Root-CA.crt certificate is not already in the keystore:
shell> openssl x509 -fingerprint -md5 -noout -in GlobalSign-Root-CA.crt

Compare the fingerprint to what is already in the keystore:
shell> keytool -list -keystore $JAVA_HOME/jre/lib/security/cacerts

c) Import the certificate from the .crt file into your JVM's default keystore of  trusted certificates.
Example:

    shell> keytool -import -file GlobalSign-Root-CA.crt -alias globalsignca28 -keystore $JAVA_HOME/jre/lib/security/cacerts

This certificate will be added under alias "globalsignca28".

d) Do steps b) and c) for GlobalSign-Domain-Validation-CA.crt as well.  Store it under the alias 'globalsigndomain14'.

## 7. Deploy CR web application and run Tomcat

If the build went well, you shall have cr.war file in CR_SOURCE_HOME/target directory.
Now all you have to do is to simply copy that file into Tomcat's webapps directory.
Optionally, you can also deploy the WAR file via Tomcat's web console, but be sure to
have made the following Tomcat configuration trick, before running Tomcat.

Before you run Tomcat, you need to change the way Tomcat handles URI encoding.
By default, it uses ISO-8859-1 for that. But CR needs UTF-8. Therefore make sure
that the <Connector> tag in Tomcat's server.xml has the following attributes:

    URIEncoding="UTF-8"
    useBodyEncodingForURI="true"

Once Tomcat is running, open CR in the browser. It's application context path is /cr,
unless you renamed cr.war to something else or you chose to deploy CR into a virtual host.

## 8. Setting up properties for local development (Optional)

1. Create a copy of *default.properties* with name *local.properties*
2. Change the property values on *local.properties* to meet your needs
3. Change property 'env' to local in pom.xml

## 9. Overriding properties by environment variables (Docker)

All the properties in *default.properties* can be overriden by environment variables.

Recommended properties to override:
* -Dconfig.application.homeURL=
* -Dconfig.edu.yale.its.tp.cas.client.filter.serverName=


## 10. Tips for Handling Virtuoso Errors

#### 10.1 Virtuoso Error : "Message: SR008: Function sequence_set needs an integer as argument 2, not an arg of type DB_NULL (204)" or similar error 
   * **Solution:** <br>
     It may be an indicator that we are trying to insert a wrong or null  value to a virtuoso column. 
     The best way to overcome it is to try to print from the code, the query to be executed along with the actual values, 
     and run the query manually to virtuoso server.
     In this case  where this error occurs, we were passing null value to an integer not null column.

