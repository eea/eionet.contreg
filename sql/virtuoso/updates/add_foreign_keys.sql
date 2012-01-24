ALTER TABLE CR.cr3user.harvest_message
ADD CONSTRAINT hame_ha_fk FOREIGN KEY (harvest_id) REFERENCES CR.cr3user.harvest (harvest_id)
    ON DELETE CASCADE;

ALTER TABLE CR.cr3user.harvest
ADD CONSTRAINT ha_haso_fk FOREIGN KEY (harvest_source_id) REFERENCES CR.cr3user.harvest_source (harvest_source_id)
    ON DELETE CASCADE;

