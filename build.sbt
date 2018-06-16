name := "awscliproto"

version := "0.1"

scalaVersion := "2.12.6"

// libraryDependencies in ThisBuild  += "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.348"
libraryDependencies in ThisBuild  += "software.amazon.awssdk" % "aws-sdk-java" % "2.0.0-preview-10"
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-java8-compat
libraryDependencies in ThisBuild += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"

