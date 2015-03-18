lazy val root = (project in file(".")).enablePlugins(PlayJava)

name := "wimha"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaCore, javaJdbc, javaEbean, filters, javaWs, cache,
  "org.julienrf" %% "play-jsmessages" % "1.6.2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-io" % "commons-io" % "2.4",
  "javax.mail" % "mail" % "1.4.5",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "com.restfb" % "restfb" % "1.6.16",
  "com.cloudinary" % "cloudinary" % "1.0.8",
  "org.twitter4j" % "twitter4j-core" % "4.0.2",
  "com.github.segmentio" % "analytics" % "1.0.3"
)

resolvers ++= Seq(
  "julienrf.github.com" at "http://julienrf.github.com/repo/",
  "Spy Repository" at "http://files.couchbase.com/maven2",
  "Maven Repository" at "http://repo1.maven.org/maven2/"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

LessKeys.compress := true

pipelineStages := Seq(uglify, digest, gzip)

sources in (Compile,doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false