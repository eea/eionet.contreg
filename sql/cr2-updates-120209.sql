alter table SPO add column OBJ_LANG_TEMP varchar(10) not null default '' after LIT_OBJ;
update SPO set OBJ_LANG_TEMP=OBJ_LANG;
alter table SPO drop column OBJ_LANG;
alter table SPO change column OBJ_LANG_TEMP OBJ_LANG varchar(10) not null default '';

alter table SPO add column OBJ_DERIV_SOURCE_GEN_TIME bigint(20) not null default 0 after OBJ_DERIV_SOURCE;

alter table HARVEST_SOURCE add column SOURCE bigint(20) not null default 0;
alter table HARVEST_SOURCE add column GEN_TIME bigint(20) not null default 0;

create table UNFINISHED_HARVEST (SOURCE bigint(20) not null default 0, GEN_TIME bigint(20) not null default 0, unique(SOURCE, GEN_TIME));
