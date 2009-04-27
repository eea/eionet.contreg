-- add OBJ_LANG into primary key (already did it on cougar!!!!)


alter table SPO drop primary key;
alter table SPO add primary key (SUBJECT,PREDICATE,OBJECT_HASH,OBJ_LANG,SOURCE,GEN_TIME);
