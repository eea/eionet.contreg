
--
-- Table "authurl" is used for ticket #2444 implementation to store authenticated urls login data
--

create table CR.cr3user.authurl
(
authurl_id integer NOT NULL IDENTITY,
url_namestart varchar(200) NOT NULL default '',
url_username varchar(50) NOT NULL default '',
url_password varchar(50) NOT NULL default '',
PRIMARY KEY  (authurl_id)
);