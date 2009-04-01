alter table HARVEST_SOURCE change column SCHEDULE_CRON INTERVAL_MINUTES int unsigned not null default 0;
update HARVEST_SOURCE set INTERVAL_MINUTES=60480 where TYPE in ('delivered file','qaw source','schema');
update HARVEST_SOURCE set INTERVAL_MINUTES=10080 where TYPE in ('data');

alter table HARVEST_SOURCE change column LAST_HARVEST LAST_HARVEST timestamp null default null;
alter table HARVEST_SOURCE change column DATE_CREATED TIME_CREATED timestamp default 0;

rename table HARVEST_QUEUE to URGENT_HARVEST_QUEUE;
alter table URGENT_HARVEST_QUEUE drop column PRIORITY;