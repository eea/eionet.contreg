alter table RESOURCE add column LASTMODIFIED_TIME bigint(20) not null default 0;
alter table RESOURCE add key (LASTMODIFIED_TIME);