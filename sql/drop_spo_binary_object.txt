---------------------------------------------------------------------------------------------------------------
-- This script drops the "object" column in "spo_binary" column, because it is not needed any more in CR3.
---------------------------------------------------------------------------------------------------------------

--
-- First drop the "replace_spo_binary" rule that depends on the "object" column.
--
drop rule replace_spo_binary on spo_binary;

--
-- Now re-create the rule without it being dependent on "object" column any more.
--
CREATE RULE replace_spo_binary AS
    ON INSERT TO spo_binary
    WHERE
      EXISTS(SELECT 1 FROM spo_binary WHERE subject=NEW.subject)
    DO INSTEAD
       (UPDATE spo_binary SET obj_lang=NEW.obj_lang, datatype=NEW.datatype, must_embed=NEW.must_embed WHERE subject=NEW.subject);

--
-- Finally drop the "object" column itself.
--
alter table spo_binary drop column object;
