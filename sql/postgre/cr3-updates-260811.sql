CREATE TABLE documentation
(
   page_id character varying(50) NOT NULL, 
   content_type character varying(50) NOT NULL DEFAULT 'text/html', 
   "content" bytea NOT NULL, 
   CONSTRAINT documentation_pkey PRIMARY KEY (page_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE
     ON documentation
     TO cr3user;