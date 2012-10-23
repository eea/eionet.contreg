update CR.cr3user.harvest_source hs
set last_harvest_id = (
    select max(h.harvest_id) as max_harvest_id
    from CR.cr3user.harvest h
    where h.harvest_source_id = hs.harvest_source_id
    group by h.harvest_source_id);