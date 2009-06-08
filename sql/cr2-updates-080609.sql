alter table SPO add column OBJECT_DOUBLE double after OBJECT_HASH;
update SPO set OBJECT_DOUBLE=cast(OBJECT as decimal(25,10));
update SPO set OBJECT_DOUBLE=NULL where OBJECT_HASH not in (-5808608550200435537,-826884029810010605) and OBJECT_DOUBLE=0;
alter table SPO add index (OBJECT_DOUBLE);