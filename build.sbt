ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

lazy val akkaVersion = "2.6.19"
lazy val leveldbVersion = "0.7"
lazy val leveldbJniVersion = "1.8"

lazy val root = (project in file("."))
  .settings(
    name := "essential-akka"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,

  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"          % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % leveldbJniVersion

)