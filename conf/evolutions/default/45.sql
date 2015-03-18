# --- !Ups
create table url (
  id                        bigint not null,
  short_id                  text,
  type                      int not null,
  member_id                 VARCHAR(40),
  target_url                text,
  constraint pk_url primary key (id),
  constraint fk_member_id foreign key (member_id)
  references totem (id))
;

create sequence url_seq;


# --- !Downs
drop table if exists url cascade;
drop sequence if exists url_seq;
