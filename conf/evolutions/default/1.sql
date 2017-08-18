# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table v_branch (
  id                            varchar(255) not null,
  branch_name                   varchar(255),
  schema                        varchar(255),
  commit_id                     varchar(255),
  cts                           timestamp not null,
  ts                            timestamp not null,
  constraint pk_v_branch primary key (id)
);

create table v_root (
  id                            varchar(40) not null,
  branch                        varchar(255),
  uid                           varchar(255),
  content                       json,
  type                          varchar(255),
  ts                            timestamp not null,
  constraint pk_v_root primary key (id)
);

create table v_schema (
  id                            varchar(40) not null,
  branch                        varchar(255),
  uid                           varchar(255),
  content                       json,
  name                          varchar(255),
  ts                            timestamp not null,
  constraint uq_v_schema_name_branch unique (name,branch),
  constraint pk_v_schema primary key (id)
);

create table v_table (
  id                            varchar(40) not null,
  branch                        varchar(255),
  uid                           varchar(255),
  content                       json,
  name                          varchar(255),
  ts                            timestamp not null,
  constraint pk_v_table primary key (id)
);

create index ix_v_branch_cts on v_branch (cts);
create index ix_v_branch_ts on v_branch (ts);
create index ix_v_root_branch on v_root (branch);
create index ix_v_root_uid on v_root (uid);
create index ix_v_root_type on v_root (type);
create index ix_v_root_ts on v_root (ts);
create index ix_v_schema_branch on v_schema (branch);
create index ix_v_schema_uid on v_schema (uid);
create index ix_v_schema_name on v_schema (name);
create index ix_v_schema_ts on v_schema (ts);
create index ix_v_table_branch on v_table (branch);
create index ix_v_table_uid on v_table (uid);
create index ix_v_table_name on v_table (name);
create index ix_v_table_ts on v_table (ts);

# --- !Downs

drop table if exists v_branch cascade;

drop table if exists v_root cascade;

drop table if exists v_schema cascade;

drop table if exists v_table cascade;

drop index if exists ix_v_branch_cts;
drop index if exists ix_v_branch_ts;
drop index if exists ix_v_root_branch;
drop index if exists ix_v_root_uid;
drop index if exists ix_v_root_type;
drop index if exists ix_v_root_ts;
drop index if exists ix_v_schema_branch;
drop index if exists ix_v_schema_uid;
drop index if exists ix_v_schema_name;
drop index if exists ix_v_schema_ts;
drop index if exists ix_v_table_branch;
drop index if exists ix_v_table_uid;
drop index if exists ix_v_table_name;
drop index if exists ix_v_table_ts;
