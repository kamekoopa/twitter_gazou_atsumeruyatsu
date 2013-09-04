import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("net.kamekoopa.twitter_gazou_atsumeruyatsu.Main")

name := "kneeso_collector"

organization := "net.kamekoopa"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

scalacOptions += "-feature"

resolvers += "Scala-Tools Maven2 Repository" at "https://oss.sonatype.org/content/groups/scala-tools/"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.twitter4j" % "twitter4j-core"  % "3.0.3",
  "org.twitter4j" % "twitter4j-async" % "3.0.3",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "org.clapper" %% "argot" % "1.0.1"
)

initialCommands := "import net.kamekoopa.twitter_gazou_atsumeruyatsu._"
