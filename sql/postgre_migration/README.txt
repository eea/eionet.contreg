

This README describes how to migrate CR2 database from MySQL to PostgreSQL.
It refers to SQL scripts located in the same directory this README is located in.


***************************************
***********   ASSUMPTIONS   ***********
***************************************

PostgreSQL must already be installed in your system, and its executables (e.g. psql, pg_dump)
must be on path. The PostgreSQL server/service must be running.

In the below instructions the "root" user of PostgreSQL is "postgres". Change it as suitable
for your environment.

The instructions also assume that you already created an empty cr2 database in PostgreSQL, called 'cr2'.

NB!!!
In your postgresql.conf file, make sure that client_encoding = UTF8.
Also in the same file, make sure that client_min_messages = error.



***************************************
***********   INSTRUCTIONS   **********
***************************************


1)
Prepare for dumping your CR2 MySQL database, by renaming HARVEST.USER column to HARVEST.USERNAME.
This is because PostgreSQL does ot allow 'USER' in column names.
You can do this by importing the following script into your CR2 MySQL database:

[user@host]$ mysql -u root -p cr2 < rename-user-column.mysql



2)
Make a data dump of your CR2 MySQL database:

[user@host]$ mysqldump -u root -p --no-create-info --complete-insert --extended-insert --skip-add-locks --skip-comments --skip-disable-keys --skip-quote-names --skip-set-charset cr2 > cr2-data.sql

This dump exports data only! The various options ensure that it later imports into PostgreSQL.

3)
Create the 'cr2' database in postgresql:
[user@host]$ createdb -U postgres cr2 

3.1)
Import the script that creates tables and all other necessary structure in your freshly created cr2 database:

[user@host]$ psql -U postgres cr2 < cr2-no-data.pgsql

3.2)
Create the database user you want to connect as from the web-application
[user@host]$ psql -U postgres cr2 < create_devuser.sql

4)
To prepare for the data import, drop all indexes, constraints and rules by importing the following script:

[user@host]$ psql -U postgres cr2 < drop_indexes_and_rules.pqsql


5)
Import the data dump you created in step 2:

[user@host]$ psql -U postgres -q cr2 < cr2-data.sql

The -q option forces psql to not print out any messages, because otherwise the import would take way too long.



6)
Re-create the indexes, constraints and rules you dropped in step 5.

[user@host]$ psql -U postgres cr2 < create_indexes_and_rules.pgsql

7)
Fix the next values of sequences, because they go "broken" if serial IDs are inserted explicitly
(which is what the data import exactly did):

[user@host]$ psql -U postgres cr2 < fix_sequences.pgsql


8)
Switch CR to use the postqresql driver and the correct URL for the database.
Driver:  org.postgresql.Driver
URL:     jdbc:postgresql://localhost:5432/cr2

You might also have to add the postgresql-jdbc4 JAR file to tomcat5/common/lib

