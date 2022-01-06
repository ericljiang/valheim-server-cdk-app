ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .settings(
    name := "valheim-server-cdk-app",
    libraryDependencies ++= Seq(
      "software.amazon.awscdk" % "aws-cdk-lib" % "2.3.0",
      "software.amazon.awscdk" % "apigatewayv2-alpha" % "2.3.0-alpha.0",
      "software.amazon.awscdk" % "apigatewayv2-integrations-alpha" % "2.3.0-alpha.0",
      "software.amazon.awscdk" % "codestar-alpha" % "2.3.0-alpha.0"
    )
  )
