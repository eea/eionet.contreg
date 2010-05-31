drop table if exists cache_SPO_TYPE;
create table  cache_SPO_TYPE as select distinct SUBJECT from SPO where PREDICATE=-5251507576730213845 and OBJECT_HASH=-413436351744164071;
drop table if exists cache_SPO_TYPE_SUBJECT;
create table  cache_SPO_TYPE_SUBJECT as select distinct object_hash, subject from SPO where PREDICATE=-5251507576730213845 and OBJECT_HASH in (select subject from cache_SPO_TYPE);
drop table if exists cache_SPO_TYPE_PREDICATE;
create table  cache_SPO_TYPE_PREDICATE as select distinct s.object_hash, spo.predicate from SPO, cache_SPO_TYPE_SUBJECT s where spo.subject=s.subject;


