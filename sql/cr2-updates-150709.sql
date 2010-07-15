CREATE TABLE spo_binary
(
  subject bigint NOT NULL,
  "object" bytea NOT NULL,
  obj_lang character varying(10) NOT NULL DEFAULT ''::character varying,
  datatype character varying(50) NOT NULL DEFAULT ''::character varying,
  must_embed boolean DEFAULT false
)
WITH (
  OIDS=FALSE
);
ALTER TABLE spo_binary OWNER TO postgres;