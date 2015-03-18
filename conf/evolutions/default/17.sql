# --- !Ups
create table link (
	id                          varchar(40) not null,
	activity_id					varchar(40),
  	url			             	varchar(255) not null,
  	title		             	varchar(255),
  	description             	varchar(255),
  	picture_id       		   	varchar(40),

  	PRIMARY KEY(id),
  	UNIQUE (url,activity_id),
    FOREIGN KEY(activity_id) REFERENCES activity(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY(picture_id) REFERENCES picture(id) ON UPDATE CASCADE ON DELETE CASCADE 

);

# --- !Downs
DROP TABLE if exists link cascade;
