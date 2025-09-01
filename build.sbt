ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

lazy val akkaVersion = "2.6.19"
val akkaHttpVersion = "10.2.10"
lazy val leveldbVersion = "0.7"
lazy val leveldbJniVersion = "1.8"
val scalaTestVersion = "3.0.9"

lazy val root = (project in file("."))
  .settings(
    name := "essential-akka"
  )

libraryDependencies ++= Seq(
  // akka test kit
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,

  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"          % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % leveldbJniVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
)