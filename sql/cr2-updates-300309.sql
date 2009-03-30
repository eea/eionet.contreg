alter table HARVEST_SOURCE change column SCHEDULE_CRON INTERVAL_MINUTES int unsigned not null default 0;
update HARVEST_SOURCE set INTERVAL_MINUTES=60480 where TYPE in ('delivered file','qaw source','schema');
update HARVEST_SOURCE set INTERVAL_MINUTES=10080 where TYPE in ('data');