# --- !Ups
create table chat (
  id                         varchar(40) not null,
  title                      varchar(255) not null,
  type                       varchar(40) not null,
  date_dead                  timestamp,
  picture_id                 varchar(40),
  constraint pk_conversation primary key (id)
);

create table chat_member (
  chat_id                    varchar(40) not null,
  wimha_user_id              varchar(40) not null,
  mail_notif                 boolean not null default true,
  
  constraint fk_chat_id foreign key (chat_id) 
  references chat (id),

  constraint fk_wimha_user_id foreign key (wimha_user_id) 
  references wimha_user (id)
);

create table chat_message (
  id                         varchar(40) not null,
  chat_id                    varchar(40) not null,
  user_id                    varchar(40) not null,
  message                    text,
  date_post                  timestamp not null,
  constraint pk_message primary key (id),

  constraint fk_chat_id foreign key (chat_id) 
  references chat (id),

  constraint fk_user_id foreign key (user_id) 
  references wimha_user (id)
);


# --- !Downs
drop table if exists chat cascade;
drop table if exists chat_message;
drop table if exists chat_member;