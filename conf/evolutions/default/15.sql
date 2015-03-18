# --- !Ups
create table thread (
  	id                        varchar(40) NOT NULL,
    saction_id                bigint NOT NULL,
  	last_update               timestamp,

  	CONSTRAINT pk_thread PRIMARY KEY (id),
    CONSTRAINT fk_saction_thread FOREIGN KEY(saction_id) REFERENCES social_action(id) ON UPDATE CASCADE ON DELETE CASCADE
);

create table comment (
  	id                        varchar(40) NOT NULL,
  	created_at                timestamp,
  	last_update               timestamp,
  	thread_id                 varchar(40) NOT NULL,
    user_id                   varchar(40) NOT NULL,
    message                   varchar(240) NOT NULL,

  	CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT pk_comment_thread FOREIGN KEY(thread_id) REFERENCES thread(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT pk_comment_user FOREIGN KEY(user_id) REFERENCES wimha_user(id) ON UPDATE CASCADE ON DELETE CASCADE
);

create table followed_thread (
  	id                        varchar(40) NOT NULL,
  	user_id                   varchar(40) NOT NULL,
  	thread_id                 varchar(40) NOT NULL,
  	last_see                  timestamp,

  	CONSTRAINT pk_followed_thread PRIMARY KEY (id),
    CONSTRAINT pk_followed_thread_thread FOREIGN KEY(user_id) REFERENCES wimha_user(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT pk_followed_thread_user FOREIGN KEY(thread_id) REFERENCES thread(id) ON UPDATE CASCADE ON DELETE CASCADE
);


# --- !Downs
DROP TABLE if exists thread cascade;

DROP TABLE if exists comment cascade;

DROP TABLE if exists followed_thread cascade;