ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

val akkaVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    name := "essential-akka"
  )

libraryDependencies ++= Seq(
  //akka
  "com.typesafe.akka" %% "akka-actor" % "2.5.12"
)