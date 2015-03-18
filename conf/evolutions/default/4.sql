# --- !Ups

UPDATE "public"."social_interest" SET "visibility"=1;
ALTER TABLE "public"."social_interest" ALTER COLUMN "visibility" SET DEFAULT 1;

# --- !Downs

