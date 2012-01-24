ALTER TABLE CRTEST.cr3test.harvest_message
ADD CONSTRAINT hame_ha_fk_cr3test FOREIGN KEY (harvest_id) REFERENCES CRTEST.cr3test.harvest (harvest_id)
    ON DELETE CASCADE;

ALTER TABLE CRTEST.cr3test.harvest
ADD CONSTRAINT ha_haso_fk_cr3test FOREIGN KEY (harvest_source_id) REFERENCES CRTEST.cr3test.harvest_source (harvest_source_id)
    ON DELETE CASCADE;

