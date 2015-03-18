# --- !Ups
create table mobile_notification (
  id                        VARCHAR(40),
  dtype                     TEXT,
  sent_date                 timestamp,
  tribu_id                  VARCHAR(40),
  constraint pk_mobile_notification primary key (id),
  constraint fk_tribu_id foreign key (tribu_id)
  references tribu (id)
);

# --- !Downs
drop table if exists mobile_notification cascade;