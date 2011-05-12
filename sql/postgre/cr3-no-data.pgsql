--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- Name: ynboolean; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ynboolean AS ENUM (
    'Y',
    'N'
);


ALTER TYPE public.ynboolean OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: cache_spo_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cache_spo_type (
    subject bigint
);


ALTER TABLE public.cache_spo_type OWNER TO postgres;

--
-- Name: cache_spo_type_predicate; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cache_spo_type_predicate (
    object_hash bigint,
    predicate bigint
);


ALTER TABLE public.cache_spo_type_predicate OWNER TO postgres;

--
-- Name: cache_spo_type_subject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cache_spo_type_subject (
    object_hash bigint,
    subject bigint
);


ALTER TABLE public.cache_spo_type_subject OWNER TO postgres;

--
-- Name: harvest; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE harvest (
    harvest_id integer NOT NULL,
    harvest_source_id integer NOT NULL,
    type character varying(20) DEFAULT ''::character varying NOT NULL,
    username character varying(45) DEFAULT NULL::character varying,
    status character varying(10) DEFAULT ''::character varying NOT NULL,
    started timestamp without time zone,
    finished timestamp without time zone,
    tot_statements integer,
    lit_statements integer,
    res_statements integer,
    enc_schemes integer
);


ALTER TABLE public.harvest OWNER TO postgres;

--
-- Name: harvest_harvest_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE harvest_harvest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.harvest_harvest_id_seq OWNER TO postgres;

--
-- Name: harvest_harvest_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE harvest_harvest_id_seq OWNED BY harvest.harvest_id;


--
-- Name: harvest_message; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE harvest_message (
    harvest_message_id integer NOT NULL,
    harvest_id integer NOT NULL,
    type character varying(3) DEFAULT ''::character varying NOT NULL,
    message text DEFAULT ''::character varying NOT NULL,
    stack_trace text
);


ALTER TABLE public.harvest_message OWNER TO postgres;

--
-- Name: harvest_message_harvest_message_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE harvest_message_harvest_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.harvest_message_harvest_message_id_seq OWNER TO postgres;

--
-- Name: harvest_message_harvest_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE harvest_message_harvest_message_id_seq OWNED BY harvest_message.harvest_message_id;


--
-- Name: harvest_source; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE harvest_source (
    harvest_source_id integer NOT NULL,
    url_hash bigint NOT NULL,
    url character varying(1024) NOT NULL,
    emails character varying(255) DEFAULT NULL::character varying,
    time_created timestamp without time zone NOT NULL,
    statements integer,
    resources integer,
    count_unavail integer DEFAULT 0 NOT NULL,
    last_harvest timestamp without time zone,
    interval_minutes integer DEFAULT 0 NOT NULL,
    source bigint DEFAULT (0)::bigint NOT NULL,
    gen_time bigint DEFAULT (0)::bigint NOT NULL,
    last_harvest_failed ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    priority_source ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    source_owner character varying(20) DEFAULT 'harvester'::character varying NOT NULL,
    permanent_error ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    media_type character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.harvest_source OWNER TO postgres;

--
-- Name: harvest_source_harvest_source_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE harvest_source_harvest_source_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.harvest_source_harvest_source_id_seq OWNER TO postgres;

--
-- Name: harvest_source_harvest_source_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE harvest_source_harvest_source_id_seq OWNED BY harvest_source.harvest_source_id;


--
-- Name: remove_source_queue; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE remove_source_queue (
    url character varying(250) NOT NULL
);


ALTER TABLE public.remove_source_queue OWNER TO postgres;

--
-- Name: resource; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resource (
    uri text NOT NULL,
    uri_hash bigint NOT NULL,
    firstseen_source bigint DEFAULT (0)::bigint NOT NULL,
    firstseen_time bigint DEFAULT (0)::bigint NOT NULL,
    lastmodified_time bigint DEFAULT (0)::bigint NOT NULL
);


ALTER TABLE public.resource OWNER TO postgres;

--
-- Name: resource_temp; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resource_temp (
    uri text NOT NULL,
    uri_hash bigint NOT NULL
);


ALTER TABLE public.resource_temp OWNER TO postgres;

--
-- Name: spo; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE spo (
    subject bigint NOT NULL,
    predicate bigint NOT NULL,
    object text NOT NULL,
    object_hash bigint NOT NULL,
    object_double double precision,
    anon_subj ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    anon_obj ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    lit_obj ynboolean DEFAULT 'Y'::ynboolean NOT NULL,
    obj_lang character varying(10) DEFAULT ''::character varying NOT NULL,
    obj_deriv_source bigint DEFAULT (0)::bigint NOT NULL,
    obj_deriv_source_gen_time bigint DEFAULT (0)::bigint NOT NULL,
    obj_source_object bigint DEFAULT (0)::bigint NOT NULL,
    source bigint NOT NULL,
    gen_time bigint NOT NULL
);


ALTER TABLE public.spo OWNER TO postgres;

--
-- Name: spo_binary; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE spo_binary (
    subject bigint NOT NULL,
    object bytea NOT NULL,
    obj_lang character varying(10) DEFAULT ''::character varying NOT NULL,
    datatype character varying(50) DEFAULT ''::character varying NOT NULL,
    must_embed boolean DEFAULT false
);


ALTER TABLE public.spo_binary OWNER TO postgres;

--
-- Name: spo_temp; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE spo_temp (
    subject bigint NOT NULL,
    predicate bigint NOT NULL,
    object text NOT NULL,
    object_hash bigint NOT NULL,
    object_double double precision,
    anon_subj ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    anon_obj ynboolean DEFAULT 'N'::ynboolean NOT NULL,
    lit_obj ynboolean DEFAULT 'Y'::ynboolean NOT NULL,
    obj_lang character varying(10) DEFAULT ''::character varying NOT NULL,
    obj_deriv_source bigint DEFAULT (0)::bigint NOT NULL,
    obj_deriv_source_gen_time bigint DEFAULT (0)::bigint NOT NULL,
    obj_source_object bigint DEFAULT (0)::bigint NOT NULL
);


ALTER TABLE public.spo_temp OWNER TO postgres;

--
-- Name: unfinished_harvest; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE unfinished_harvest (
    source bigint NOT NULL,
    gen_time bigint NOT NULL
);


ALTER TABLE public.unfinished_harvest OWNER TO postgres;

--
-- Name: urgent_harvest_queue; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE urgent_harvest_queue (
    url character varying(1024) NOT NULL,
    "timestamp" timestamp without time zone DEFAULT now() NOT NULL,
    pushed_content text
);


ALTER TABLE public.urgent_harvest_queue OWNER TO postgres;

--
-- Name: harvest_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE harvest ALTER COLUMN harvest_id SET DEFAULT nextval('harvest_harvest_id_seq'::regclass);


--
-- Name: harvest_message_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE harvest_message ALTER COLUMN harvest_message_id SET DEFAULT nextval('harvest_message_harvest_message_id_seq'::regclass);


--
-- Name: harvest_source_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE harvest_source ALTER COLUMN harvest_source_id SET DEFAULT nextval('harvest_source_harvest_source_id_seq'::regclass);


--
-- Name: harvest_message_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY harvest_message
    ADD CONSTRAINT harvest_message_pkey PRIMARY KEY (harvest_message_id);


--
-- Name: harvest_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY harvest
    ADD CONSTRAINT harvest_pkey PRIMARY KEY (harvest_id);


--
-- Name: harvest_source_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY harvest_source
    ADD CONSTRAINT harvest_source_pkey PRIMARY KEY (harvest_source_id);


--
-- Name: harvest_source_unique; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY harvest_source
    ADD CONSTRAINT harvest_source_unique UNIQUE (url_hash);


--
-- Name: harvest_unique; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY harvest
    ADD CONSTRAINT harvest_unique UNIQUE (harvest_source_id, started);


--
-- Name: remove_source_queue_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY remove_source_queue
    ADD CONSTRAINT remove_source_queue_pkey PRIMARY KEY (url);


--
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (uri_hash);


--
-- Name: unfinished_harvest_unique; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY unfinished_harvest
    ADD CONSTRAINT unfinished_harvest_unique UNIQUE (source, gen_time);


--
-- Name: harvest_message_harvest_id; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX harvest_message_harvest_id ON harvest_message USING btree (harvest_id);


--
-- Name: resource_firstseen_source; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX resource_firstseen_source ON resource USING btree (firstseen_source);


--
-- Name: resource_firstseen_time; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX resource_firstseen_time ON resource USING btree (firstseen_time);


--
-- Name: resource_lastmodified_time; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX resource_lastmodified_time ON resource USING btree (lastmodified_time);


--
-- Name: spo_binary_subject_unq; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX spo_binary_subject_unq ON spo_binary USING btree (subject);


--
-- Name: spo_gen_time; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_gen_time ON spo USING btree (gen_time);


--
-- Name: spo_obj_deriv_source; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_obj_deriv_source ON spo USING btree (obj_deriv_source);


--
-- Name: spo_obj_deriv_source_gen_time; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_obj_deriv_source_gen_time ON spo USING btree (obj_deriv_source_gen_time);


--
-- Name: spo_obj_source_object; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_obj_source_object ON spo USING btree (obj_source_object);


--
-- Name: spo_object_double; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_object_double ON spo USING btree (object_double);


--
-- Name: spo_object_hash; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_object_hash ON spo USING btree (object_hash);


--
-- Name: spo_object_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_object_idx ON spo USING gin (to_tsvector('simple'::regconfig, object));


--
-- Name: spo_predicate; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_predicate ON spo USING btree (predicate);


--
-- Name: spo_source; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_source ON spo USING btree (source);


--
-- Name: spo_subject; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spo_subject ON spo USING btree (subject);


--
-- Name: replace_resource; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE replace_resource AS ON INSERT TO resource WHERE (EXISTS (SELECT 1 FROM resource WHERE (resource.uri_hash = new.uri_hash))) DO INSTEAD UPDATE resource SET lastmodified_time = new.lastmodified_time WHERE (resource.uri_hash = new.uri_hash);


--
-- Name: replace_spo_binary; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE replace_spo_binary AS ON INSERT TO spo_binary WHERE (EXISTS (SELECT 1 FROM spo_binary WHERE (spo_binary.subject = new.subject))) DO INSTEAD UPDATE spo_binary SET object = new.object, obj_lang = new.obj_lang, datatype = new.datatype, must_embed = new.must_embed WHERE (spo_binary.subject = new.subject);


--
-- Name: skip_duplicate_harvest_source; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE skip_duplicate_harvest_source AS ON INSERT TO harvest_source WHERE (EXISTS (SELECT 1 FROM harvest_source WHERE (harvest_source.url_hash = new.url_hash))) DO INSTEAD UPDATE harvest_source SET url_hash = new.url_hash WHERE (harvest_source.url_hash = new.url_hash);


--
-- Name: skip_duplicate_remove_source; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE skip_duplicate_remove_source AS ON INSERT TO remove_source_queue WHERE (EXISTS (SELECT 1 FROM remove_source_queue WHERE ((remove_source_queue.url)::text = (new.url)::text))) DO INSTEAD UPDATE remove_source_queue SET url = new.url WHERE ((remove_source_queue.url)::text = (new.url)::text);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: harvest; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE harvest FROM PUBLIC;
REVOKE ALL ON TABLE harvest FROM postgres;
GRANT ALL ON TABLE harvest TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE harvest TO cr3user;


--
-- Name: harvest_harvest_id_seq; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON SEQUENCE harvest_harvest_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE harvest_harvest_id_seq FROM postgres;
GRANT ALL ON SEQUENCE harvest_harvest_id_seq TO postgres;
GRANT ALL ON SEQUENCE harvest_harvest_id_seq TO cr3user;


--
-- Name: harvest_message; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE harvest_message FROM PUBLIC;
REVOKE ALL ON TABLE harvest_message FROM postgres;
GRANT ALL ON TABLE harvest_message TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE harvest_message TO cr3user;


--
-- Name: harvest_message_harvest_message_id_seq; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON SEQUENCE harvest_message_harvest_message_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE harvest_message_harvest_message_id_seq FROM postgres;
GRANT ALL ON SEQUENCE harvest_message_harvest_message_id_seq TO postgres;
GRANT ALL ON SEQUENCE harvest_message_harvest_message_id_seq TO cr3user;


--
-- Name: harvest_source; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE harvest_source FROM PUBLIC;
REVOKE ALL ON TABLE harvest_source FROM postgres;
GRANT ALL ON TABLE harvest_source TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE harvest_source TO cr3user;


--
-- Name: harvest_source_harvest_source_id_seq; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON SEQUENCE harvest_source_harvest_source_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE harvest_source_harvest_source_id_seq FROM postgres;
GRANT ALL ON SEQUENCE harvest_source_harvest_source_id_seq TO postgres;
GRANT ALL ON SEQUENCE harvest_source_harvest_source_id_seq TO cr3user;


--
-- Name: remove_source_queue; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE remove_source_queue FROM PUBLIC;
REVOKE ALL ON TABLE remove_source_queue FROM postgres;
GRANT ALL ON TABLE remove_source_queue TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE remove_source_queue TO cr3user;


--
-- Name: resource; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE resource FROM PUBLIC;
REVOKE ALL ON TABLE resource FROM postgres;
GRANT ALL ON TABLE resource TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE resource TO cr3user;


--
-- Name: resource_temp; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE resource_temp FROM PUBLIC;
REVOKE ALL ON TABLE resource_temp FROM postgres;
GRANT ALL ON TABLE resource_temp TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE resource_temp TO cr3user;


--
-- Name: spo; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE spo FROM PUBLIC;
REVOKE ALL ON TABLE spo FROM postgres;
GRANT ALL ON TABLE spo TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE spo TO cr3user;


--
-- Name: spo_binary; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE spo_binary FROM PUBLIC;
REVOKE ALL ON TABLE spo_binary FROM postgres;
GRANT ALL ON TABLE spo_binary TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE spo_binary TO cr3user;


--
-- Name: spo_temp; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE spo_temp FROM PUBLIC;
REVOKE ALL ON TABLE spo_temp FROM postgres;
GRANT ALL ON TABLE spo_temp TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE spo_temp TO cr3user;


--
-- Name: unfinished_harvest; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE unfinished_harvest FROM PUBLIC;
REVOKE ALL ON TABLE unfinished_harvest FROM postgres;
GRANT ALL ON TABLE unfinished_harvest TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE unfinished_harvest TO cr3user;


--
-- Name: urgent_harvest_queue; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE urgent_harvest_queue FROM PUBLIC;
REVOKE ALL ON TABLE urgent_harvest_queue FROM postgres;
GRANT ALL ON TABLE urgent_harvest_queue TO postgres;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE urgent_harvest_queue TO cr3user;


--
-- PostgreSQL database dump complete
--

