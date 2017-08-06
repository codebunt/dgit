# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table v_root (
  id                            varchar(40) not null,
  branch                        varchar(255),
  uid                           varchar(255),
  type                          varchar(255),
  content                       json,
  ts                            timestamp not null,
  constraint pk_v_root primary key (id)
);

create index ix_v_root_branch on v_root (branch);
create index ix_v_root_uid on v_root (uid);
create index ix_v_root_type on v_root (type);
create index ix_v_root_ts on v_root (ts);

# --- !Downs

drop table if exists v_root cascade;

drop index if exists ix_v_root_branch;
drop index if exists ix_v_root_uid;
drop index if exists ix_v_root_type;
drop index if exists ix_v_root_ts;
