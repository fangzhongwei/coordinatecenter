lazy val commonSettings = Seq(
  javacOptions ++= Seq("-encoding", "UTF-8"),
  organization := "com.lawsofnature.coordinatecenter",
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "commons-codec" % "commons-codec" % "1.10",
    "net.codingwell" % "scala-guice_2.11" % "4.1.0",
    "org.scala-lang" % "scala-library" % "2.11.8",
    "com.lawsofnature.common" % "common-edecrypt_2.11" % "1.0",
    "com.rabbitmq" % "amqp-client" % "3.6.5",
    "com.zeroc" % "ice" % "3.6.2",
    "com.google.protobuf" % "protobuf-java" % "3.0.0",
    "com.lawsofnature.common" % "common-utils_2.11" % "1.0",
    "com.lawsofnature.common" % "common-error_2.11" % "1.0",
    "com.lawsofnature.common" % "common-edecrypt_2.11" % "1.0",
    "com.lawsofnature.common" % "common-redis_2.11" % "1.0",
    "com.lawsofnature.client" % "ssoclient_2.11" % "1.0",
    "com.lawsofnature.common" % "common-rabbitmq_2.12.0-RC2" % "1.0",
    "com.lawsofnature.common" % "common-ice_2.11" % "1.0",
    "com.lawsofnature.gamecenter" % "gamecommonlib_2.11" % "1.0"
  )
)


lazy val coordinatecommonlib = (project in file("coordinatecommonlib")).settings(commonSettings: _*).settings(
  name := """coordinatecommonlib""",
  libraryDependencies ++= Seq(
  )
)

lazy val coordinateserver = (project in file("coordinateserver")).settings(commonSettings: _*).settings(
  name := """coordinateserver""",
  libraryDependencies ++= Seq(
    "com.lawsofnature.common" % "common-mysql_2.11" % "1.0",
    "com.lawsofnature.coordinatecenter" % "coordinatecommonlib_2.11" % "1.0",
    "com.lawsofnature.member" % "memberclient_2.11" % "1.0-SNAPSHOT",
    "com.lawsofnature.gamecenter" % "gamecommonlib_2.11" % "1.0",
    "com.typesafe.slick" % "slick-codegen_2.11" % "3.2.0-M2" % "test"
  )
)