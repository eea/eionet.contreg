CREATE RULE replace_spo_binary AS
    ON INSERT TO spo_binary
    WHERE
      EXISTS(SELECT 1 FROM spo_binary WHERE subject=NEW.subject)
    DO INSTEAD
       (UPDATE spo_binary SET object=NEW.object, obj_lang=NEW.obj_lang, datatype=NEW.datatype, must_embed=NEW.must_embed WHERE subject=NEW.subject);