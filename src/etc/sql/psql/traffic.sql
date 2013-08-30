--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: 
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- Name: plpgsql_call_handler(); Type: FUNCTION; Schema: public; Owner: root
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '$libdir/plpgsql', 'plpgsql_call_handler'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_call_handler() OWNER TO root;

--
-- Name: plpgsql_validator(oid); Type: FUNCTION; Schema: public; Owner: root
--

CREATE FUNCTION plpgsql_validator(oid) RETURNS void
    AS '$libdir/plpgsql', 'plpgsql_validator'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_validator(oid) OWNER TO root;

--
-- Name: _a_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE _a_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public._a_id_seq OWNER TO root;

SET default_tablespace = '';

SET default_with_oids = true;

--
-- Name: addresses; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE addresses (
    id serial NOT NULL,
    client_id integer NOT NULL,
    city character varying(64),
    street character varying(128),
    country character varying(128),
    zip character varying(16),
    house character varying(32),
    corr_name character varying(128),
    post_box character varying(16),
    aprtmt character varying(32)
);


ALTER TABLE public.addresses OWNER TO root;

--
-- Name: aggreg; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE aggreg (
    a_id integer DEFAULT nextval(('_a_id_seq'::text)::regclass) NOT NULL,
    dat date NOT NULL,
    cisco character varying(64) NOT NULL,
    iface character varying(64) NOT NULL,
    t_in bigint NOT NULL,
    t_out bigint NOT NULL
);


ALTER TABLE public.aggreg OWNER TO root;

--
-- Name: tr; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE tr (
    dt timestamp without time zone,
    interface character varying(255),
    inoctets bigint,
    outoctets bigint,
    cisco character varying(64),
    id serial NOT NULL
);


ALTER TABLE public.tr OWNER TO root;

--
-- Name: c_i; Type: VIEW; Schema: public; Owner: root
--

CREATE VIEW c_i AS
    SELECT DISTINCT tr.cisco, tr.interface FROM tr ORDER BY tr.cisco, tr.interface;


ALTER TABLE public.c_i OWNER TO root;

--
-- Name: cisco_iface; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE cisco_iface (
    id serial NOT NULL,
    cisco character varying(64),
    interface character varying(64)
);


ALTER TABLE public.cisco_iface OWNER TO root;

--
-- Name: cl; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE cl (
    cisco character varying(64),
    interface character varying(255),
    client character varying(255),
    id serial NOT NULL,
    summarizing boolean DEFAULT false,
    deleted boolean DEFAULT false
);


ALTER TABLE public.cl OWNER TO root;

SET default_with_oids = false;

--
-- Name: cl_deleted; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE cl_deleted (
    cisco character varying(64),
    interface character varying(255),
    client character varying(255),
    id integer NOT NULL,
    summarizing boolean DEFAULT false,
    deleted boolean DEFAULT true,
    date_deleted timestamp without time zone DEFAULT now()
);


ALTER TABLE public.cl_deleted OWNER TO root;

SET default_with_oids = true;

--
-- Name: client; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE client (
    cisco character(64),
    interface character(255),
    client character(255)
);


ALTER TABLE public.client OWNER TO root;

--
-- Name: client1; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE client1 (
    cisco character varying(64),
    interface character varying(255),
    client character varying(255)
);


ALTER TABLE public.client1 OWNER TO root;

--
-- Name: client_group; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE client_group (
    id integer NOT NULL,
    innername character varying(255),
    userflag boolean DEFAULT true
);


ALTER TABLE public.client_group OWNER TO root;

--
-- Name: client_ntraffic; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE client_ntraffic (
    id serial NOT NULL,
    client integer NOT NULL,
    dat timestamp without time zone NOT NULL,
    incoming bigint,
    outcoming bigint
);


ALTER TABLE public.client_ntraffic OWNER TO root;

--
-- Name: clients_groups; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE clients_groups (
    group_id integer NOT NULL,
    client_id integer NOT NULL
);


ALTER TABLE public.clients_groups OWNER TO root;

--
-- Name: contact_info; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE contact_info (
    contact_name character varying(128) NOT NULL,
    contact_phone character varying(24) NOT NULL,
    contact_work character varying(64),
    contact_mail character varying(128),
    contact_id serial NOT NULL,
    client_id integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.contact_info OWNER TO root;

--
-- Name: groups_groups; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE groups_groups (
    parent_id integer NOT NULL,
    child_id integer NOT NULL
);


ALTER TABLE public.groups_groups OWNER TO root;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO root;

--
-- Name: netflow; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE netflow (
    id serial NOT NULL,
    dat date NOT NULL,
    host character varying(24),
    "input" bigint,
    output bigint
);


ALTER TABLE public.netflow OWNER TO root;

--
-- Name: netflow_details; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE netflow_details (
    id serial NOT NULL,
    dat timestamp without time zone NOT NULL,
    host character varying(64) NOT NULL,
    network_id integer NOT NULL,
    "input" bigint DEFAULT 0,
    output bigint DEFAULT 0
);
ALTER TABLE ONLY netflow_details ALTER COLUMN dat SET STATISTICS 250;
ALTER TABLE ONLY netflow_details ALTER COLUMN network_id SET STATISTICS 100;


ALTER TABLE public.netflow_details OWNER TO root;

--
-- Name: nn_summ; Type: VIEW; Schema: public; Owner: root
--

CREATE VIEW nn_summ AS
    SELECT netflow_details.dat, netflow_details.network_id, sum(netflow_details."input") AS "input", sum(netflow_details.output) AS output FROM netflow_details GROUP BY netflow_details.dat, netflow_details.network_id ORDER BY netflow_details.dat, netflow_details.network_id;


ALTER TABLE public.nn_summ OWNER TO root;

--
-- Name: netflow_day_summary; Type: VIEW; Schema: public; Owner: root
--

CREATE VIEW netflow_day_summary AS
    SELECT (date_trunc('day'::text, nn_summ.dat))::date AS dat, nn_summ.network_id, sum(nn_summ."input") AS "input", sum(nn_summ.output) AS output FROM nn_summ GROUP BY date_trunc('day'::text, nn_summ.dat), nn_summ.network_id;


ALTER TABLE public.netflow_day_summary OWNER TO root;

--
-- Name: netflow_networks; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE netflow_networks (
    id serial NOT NULL,
    dat date NOT NULL,
    network_id integer NOT NULL,
    "input" bigint,
    output bigint
);


ALTER TABLE public.netflow_networks OWNER TO root;

--
-- Name: netflow_networks_details; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE netflow_networks_details (
    id serial NOT NULL,
    network_id integer NOT NULL,
    dat timestamp without time zone NOT NULL,
    "input" bigint DEFAULT 0,
    output bigint DEFAULT 0
);


ALTER TABLE public.netflow_networks_details OWNER TO root;

--
-- Name: networks; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE networks (
    id serial NOT NULL,
    net character varying(32),
    mask character varying(32),
    client integer,
    nat_addr character varying(32)
);


ALTER TABLE public.networks OWNER TO root;

--
-- Name: network_traffview; Type: VIEW; Schema: public; Owner: root
--

CREATE VIEW network_traffview AS
    SELECT b.dat, a.net, b."input", b.output FROM networks a, netflow_networks b WHERE (a.id = b.network_id);


ALTER TABLE public.network_traffview OWNER TO root;

--
-- Name: nn_day_summ; Type: VIEW; Schema: public; Owner: root
--

CREATE VIEW nn_day_summ AS
    SELECT (date_trunc('day'::text, netflow_details.dat))::date AS dat, netflow_details.network_id, sum(netflow_details."input") AS "input", sum(netflow_details.output) AS output FROM netflow_details GROUP BY (date_trunc('day'::text, netflow_details.dat))::date, netflow_details.network_id;


ALTER TABLE public.nn_day_summ OWNER TO root;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE notifications (
    client_id integer NOT NULL,
    notif_id smallint NOT NULL,
    last_event bigint DEFAULT 0 NOT NULL,
    email character varying(64) NOT NULL
);


ALTER TABLE public.notifications OWNER TO root;

--
-- Name: snmp_addresses; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE snmp_addresses (
    id serial NOT NULL,
    address character varying(32) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    community character varying(64)
);


ALTER TABLE public.snmp_addresses OWNER TO root;

SET default_with_oids = false;

--
-- Name: summary_mail; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE summary_mail (
    id serial NOT NULL,
    mailaddress character varying(128) NOT NULL
);


ALTER TABLE public.summary_mail OWNER TO root;

SET default_with_oids = true;

--
-- Name: temp_ids; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE temp_ids (
    id integer
);


ALTER TABLE public.temp_ids OWNER TO root;

--
-- Name: uptime; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE uptime (
    dt timestamp without time zone,
    uptime bigint,
    cisco character varying(64),
    id serial NOT NULL
);


ALTER TABLE public.uptime OWNER TO root;

--
-- Name: user_perm; Type: TABLE; Schema: public; Owner: root; Tablespace: 
--

CREATE TABLE user_perm (
    username character varying(64) NOT NULL,
    "password" character varying(64) NOT NULL,
    "role" character varying(64) NOT NULL,
    client character varying(64),
    grp bigint
);


ALTER TABLE public.user_perm OWNER TO root;

--
-- Name: add_uniq; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY summary_mail
    ADD CONSTRAINT add_uniq UNIQUE (mailaddress);


--
-- Name: addr_uq; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY snmp_addresses
    ADD CONSTRAINT addr_uq UNIQUE (address);


--
-- Name: addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- Name: aggreg_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY aggreg
    ADD CONSTRAINT aggreg_pkey PRIMARY KEY (a_id);


--
-- Name: cisco_iface_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY cisco_iface
    ADD CONSTRAINT cisco_iface_pkey PRIMARY KEY (id);


--
-- Name: cl_d_uniq; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY client_ntraffic
    ADD CONSTRAINT cl_d_uniq UNIQUE (client, dat);


--
-- Name: cl_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY cl
    ADD CONSTRAINT cl_pkey PRIMARY KEY (id);


--
-- Name: client_group_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY client_group
    ADD CONSTRAINT client_group_pkey PRIMARY KEY (id);


--
-- Name: client_ntraffic_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY client_ntraffic
    ADD CONSTRAINT client_ntraffic_pkey PRIMARY KEY (id);


--
-- Name: client_unique; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY cl
    ADD CONSTRAINT client_unique UNIQUE (client);


--
-- Name: clients_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY clients_groups
    ADD CONSTRAINT clients_groups_pkey PRIMARY KEY (group_id, client_id);


--
-- Name: contact_info_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY contact_info
    ADD CONSTRAINT contact_info_pkey PRIMARY KEY (contact_id);


--
-- Name: dcl_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY cl_deleted
    ADD CONSTRAINT dcl_pkey PRIMARY KEY (id);


--
-- Name: dclient_unique; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY cl_deleted
    ADD CONSTRAINT dclient_unique UNIQUE (client);


--
-- Name: groups_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY groups_groups
    ADD CONSTRAINT groups_groups_pkey PRIMARY KEY (parent_id, child_id);


--
-- Name: ix_pk; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY summary_mail
    ADD CONSTRAINT ix_pk PRIMARY KEY (id);


--
-- Name: n_dat; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_networks
    ADD CONSTRAINT n_dat UNIQUE (network_id, dat);


--
-- Name: n_datx; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_networks_details
    ADD CONSTRAINT n_datx UNIQUE (network_id, dat);


--
-- Name: n_un; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY networks
    ADD CONSTRAINT n_un UNIQUE (net, mask);


--
-- Name: netflow_details_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_details
    ADD CONSTRAINT netflow_details_pkey PRIMARY KEY (id);


--
-- Name: netflow_dx; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_details
    ADD CONSTRAINT netflow_dx UNIQUE (dat, host);


--
-- Name: netflow_networks_details_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_networks_details
    ADD CONSTRAINT netflow_networks_details_pkey PRIMARY KEY (id);


--
-- Name: netflow_networks_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow_networks
    ADD CONSTRAINT netflow_networks_pkey PRIMARY KEY (id);


--
-- Name: netflow_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY netflow
    ADD CONSTRAINT netflow_pkey PRIMARY KEY (id);


--
-- Name: networks_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY networks
    ADD CONSTRAINT networks_pkey PRIMARY KEY (id);


--
-- Name: notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (client_id);


--
-- Name: snmp_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY snmp_addresses
    ADD CONSTRAINT snmp_addresses_pkey PRIMARY KEY (id);


--
-- Name: tr_ix_un; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY tr
    ADD CONSTRAINT tr_ix_un UNIQUE (dt, cisco, interface);


--
-- Name: tr_pk; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY tr
    ADD CONSTRAINT tr_pk PRIMARY KEY (id);


--
-- Name: un_c_up; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY uptime
    ADD CONSTRAINT un_c_up UNIQUE (dt, cisco);


--
-- Name: uptime_pk; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY uptime
    ADD CONSTRAINT uptime_pk PRIMARY KEY (id);


--
-- Name: user_perm_pkey; Type: CONSTRAINT; Schema: public; Owner: root; Tablespace: 
--

ALTER TABLE ONLY user_perm
    ADD CONSTRAINT user_perm_pkey PRIMARY KEY (username);


--
-- Name: a_dat_ix1; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE INDEX a_dat_ix1 ON aggreg USING btree (dat);


--
-- Name: ag_ix1; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE UNIQUE INDEX ag_ix1 ON aggreg USING btree (dat, cisco, iface);


--
-- Name: ci_ix2; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE UNIQUE INDEX ci_ix2 ON cisco_iface USING btree (cisco, interface);


--
-- Name: netflow_dt; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE INDEX netflow_dt ON netflow_details USING btree (dat);


--
-- Name: netflow_ix1; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE INDEX netflow_ix1 ON netflow_details USING btree (dat, network_id);


--
-- Name: netflow_ix2; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE UNIQUE INDEX netflow_ix2 ON netflow_details USING btree (dat, network_id, host);


--
-- Name: tr_ix1; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE INDEX tr_ix1 ON tr USING btree (dt, interface, cisco);


--
-- Name: tr_ix2; Type: INDEX; Schema: public; Owner: root; Tablespace: 
--

CREATE INDEX tr_ix2 ON tr USING btree (cisco, interface);


--
-- Name: c_fk; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY networks
    ADD CONSTRAINT c_fk FOREIGN KEY (client) REFERENCES cl(id);


--
-- Name: client_ntraffic_client_fkey; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY client_ntraffic
    ADD CONSTRAINT client_ntraffic_client_fkey FOREIGN KEY (client) REFERENCES cl(id) ON DELETE CASCADE;


--
-- Name: fk143ae6e4190e5; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY user_perm
    ADD CONSTRAINT fk143ae6e4190e5 FOREIGN KEY (grp) REFERENCES client_group(id);


--
-- Name: fk143ae6e4a55f4b45; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY user_perm
    ADD CONSTRAINT fk143ae6e4a55f4b45 FOREIGN KEY (grp) REFERENCES client_group(id);


--
-- Name: fk34207ba268cff56e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT fk34207ba268cff56e FOREIGN KEY (client_id) REFERENCES cl(id);


--
-- Name: fk34207ba28e81e60f; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT fk34207ba28e81e60f FOREIGN KEY (client_id) REFERENCES cl(id);


--
-- Name: fk4c268d6d68cff56e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY contact_info
    ADD CONSTRAINT fk4c268d6d68cff56e FOREIGN KEY (client_id) REFERENCES cl(id);


--
-- Name: fk4c268d6d8e81e60f; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY contact_info
    ADD CONSTRAINT fk4c268d6d8e81e60f FOREIGN KEY (client_id) REFERENCES cl(id);


--
-- Name: fk4e49ec058961032a; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY networks
    ADD CONSTRAINT fk4e49ec058961032a FOREIGN KEY (client) REFERENCES cl(id);


--
-- Name: fk4e49ec05af12f3cb; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY networks
    ADD CONSTRAINT fk4e49ec05af12f3cb FOREIGN KEY (client) REFERENCES cl(id);


--
-- Name: fk688f7eb1e2e76db; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY clients_groups
    ADD CONSTRAINT fk688f7eb1e2e76db FOREIGN KEY (group_id) REFERENCES client_group(id);


--
-- Name: fk688f7eb68cff56e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY clients_groups
    ADD CONSTRAINT fk688f7eb68cff56e FOREIGN KEY (client_id) REFERENCES cl(id);


--
-- Name: fk688f7ebc38c313b; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY clients_groups
    ADD CONSTRAINT fk688f7ebc38c313b FOREIGN KEY (group_id) REFERENCES client_group(id);


--
-- Name: fk7853069f20c46b30; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY groups_groups
    ADD CONSTRAINT fk7853069f20c46b30 FOREIGN KEY (parent_id) REFERENCES client_group(id);


--
-- Name: fk7853069f62ea171e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY groups_groups
    ADD CONSTRAINT fk7853069f62ea171e FOREIGN KEY (child_id) REFERENCES client_group(id);


--
-- Name: fk7853069f7b66b0d0; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY groups_groups
    ADD CONSTRAINT fk7853069f7b66b0d0 FOREIGN KEY (parent_id) REFERENCES client_group(id);


--
-- Name: fk7853069f847d17e; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY groups_groups
    ADD CONSTRAINT fk7853069f847d17e FOREIGN KEY (child_id) REFERENCES client_group(id);


--
-- Name: net_fk; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY netflow_networks
    ADD CONSTRAINT net_fk FOREIGN KEY (network_id) REFERENCES networks(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

