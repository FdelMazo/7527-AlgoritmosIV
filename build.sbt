val Http4sVersion = "0.21.11"
val CirceVersion = "0.13.0"
val Specs2Version = "4.10.5"
val LogbackVersion = "1.2.3"
val pmmlVersion = "1.5.5"
val doobieVersion = "0.9.4"
val postgresVersion = "42.2.18"
val xmlVersion = "2.3.2"

name := "fptp"

version := "0.0.1"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "ch.qos.logback"     %  "logback-classic"      % LogbackVersion,
  "co.fs2"             %% "fs2-io"               % "2.3.0",
  "io.circe"           %% "circe-generic"        % CirceVersion,
  "jakarta.xml.bind"   %  "jakarta.xml.bind-api" % xmlVersion,
  "org.apache.spark"   %% "spark-mllib"          % "3.0.1",
  "org.glassfish.jaxb" %  "jaxb-runtime"         % xmlVersion,
  "org.http4s"         %% "http4s-blaze-client"  % Http4sVersion,
  "org.http4s"         %% "http4s-blaze-server"  % Http4sVersion,
  "org.http4s"         %% "http4s-circe"         % Http4sVersion,
  "org.http4s"         %% "http4s-dsl"           % Http4sVersion,
  "org.jpmml"          %  "pmml-evaluator"       % pmmlVersion,
  "org.jpmml"          %  "jpmml-sparkml"        % "1.6.1",
  "org.postgresql"     %  "postgresql"           % postgresVersion,
  "org.scalatest"      %% "scalatest"            % "3.2.0",
  "org.specs2"         %% "specs2-core"          % Specs2Version % Test,
  "org.tpolecat"       %% "doobie-hikari"        % doobieVersion,
  "org.tpolecat"       %% "doobie-core"          % "0.9.0",
)

