name := "fptp"

version := "0.0.1"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "org.postgresql" %  "postgresql"    % "42.2.18",
  "org.jpmml"        %  "jpmml-sparkml" % "1.6.1",
  "org.tpolecat"   %% "doobie-core"   % "0.9.0",
  "org.tpolecat"   %% "doobie-hikari" % "0.9.0",
  "co.fs2"         %% "fs2-io"        % "2.3.0",
  "org.apache.spark" %% "spark-mllib"   % "3.0.1",
  "org.scalatest"  %% "scalatest"     % "3.2.0",
  "ch.qos.logback"     %  "logback-classic"      % "1.2.3",
  "jakarta.xml.bind"   %  "jakarta.xml.bind-api" % "2.3.2",
  "org.glassfish.jaxb" %  "jaxb-runtime"         % "2.3.2",
  "org.http4s"         %% "http4s-blaze-server"  % "0.21.11",
  "org.http4s"         %% "http4s-blaze-client"  % "0.21.11",
  "org.http4s"         %% "http4s-circe"         % "0.21.11",
  "org.http4s"         %% "http4s-dsl"           % "0.21.11",
  "io.circe"           %% "circe-generic"        % "0.13.0",
  "org.specs2"         %% "specs2-core"          % "4.10.5" % Test
)

