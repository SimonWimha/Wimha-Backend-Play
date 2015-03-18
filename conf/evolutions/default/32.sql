# --- !Ups
create table delayed_notification (
  id                        varchar(40) not null,
  type                      varchar(255) not null,
  state                     int,
  sender_id                 VARCHAR(40),
  receiver_id               VARCHAR(40) not null,
  totem_id                  VARCHAR(40),
  flash_id                  VARCHAR(40),
  date_saved                timestamp,
  constraint pk_delayed_notification primary key (id),
  constraint fk_sender_id foreign key (sender_id) 
  references wimha_user (id),
  constraint fk_areceiver_id foreign key (receiver_id) 
  references wimha_user (id),
  constraint fk_totem_id foreign key (totem_id) 
  references totem (id),
  constraint fk_flash_id foreign key (flash_id) 
  references position (id))
;

# --- !Downs
drop table if exists delayed_notification cascade;