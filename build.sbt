import com.heroku.sbt.HerokuPlugin.autoImport._
import sbt.Keys._
import sbt.Level

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "unity-cloud-deploy-scala",
    organization := "de.firegate",
    version := "0.1.0",
    scalaVersion := "2.11.8",
    herokuAppName in Compile := "unity-cloud-deploy-scala",
    herokuJdkVersion in Compile := "1.8",
    logLevel := Level.Debug,
    logLevel in compile := Level.Warn,
    logLevel in test := Level.Info,
    cancelable := true
)

herokuProcessTypes in Compile := Map(
  "web" -> "target/universal/stage/bin/unity-cloud-deploy-scala -Dhttp.port=$PORT",
  "worker" -> "java -jar target/universal/stage/lib/de.firegate.unity-cloud-deploy-scala-0.1.0.jar"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlint"
)

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Typesafe Simple Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/",
  "TypeSafe Ivy releases" at "http://dl.bintray.com/typesafe/ivy-releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)


libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"   % "3.0.1" % "test" withSources(),
  "junit"             %  "junit"       % "4.12"  % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.3",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2",
  "com.netaporter" %% "scala-uri" % "0.4.16",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.propensive" %% "rapture" % "2.0.0-M7",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.apache.httpcomponents" % "httpmime" % "4.3.1"

)

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

Revolver.settings
