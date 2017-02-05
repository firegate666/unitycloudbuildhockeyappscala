name := "hello"
organization := "de.firegate"
version := "0.1.0"
scalaVersion := "2.11.8"

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
  "TypeSafe Ivy releases" at "http://dl.bintray.com/typesafe/ivy-releases/"
)


libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"   % "3.0.1" % "test" withSources(),
  "junit"             %  "junit"       % "4.12"  % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.3",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3"
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

cancelable := true
