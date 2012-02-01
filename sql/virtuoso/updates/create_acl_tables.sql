create table CR.cr3user.acls 
(
	"acl_id" integer NOT NULL IDENTITY,
	"acl_name" varchar(100) NOT NULL default '',
	"parent_name" varchar(100) default NULL,
	"owner" varchar(255) NOT NULL default '',
	"description" varchar(255) default '',
	PRIMARY KEY  ("acl_id"),
	UNIQUE ("acl_name","parent_name")
);

create table CR.cr3user.acl_rows (
	"acl_id" integer NOT NULL default '0',
	"type" varchar (50) NOT NULL default 'object' 
		CHECK (
			"type" = 'object' OR "type" = 'doc' OR "type" = 'dcc'
		),
	"entry_type" varchar (50) NOT NULL default 'user'
		CHECK (
			"entry_type" = 'owner' OR "entry_type" = 'user' OR "entry_type" = 'localgroup' OR
			"entry_type" = 'other' OR "entry_type" = 'foreign' OR "entry_type" = 'unauthenticated' OR
			"entry_type" = 'authenticated' OR "entry_type" = 'mask'
		),
	"principal" varchar (16) NOT NULL default '',
	"permissions" varchar (255) NOT NULL default '',
	"status" integer NOT NULL default '0',
	PRIMARY KEY  ("acl_id","type","entry_type","principal","status")
);