import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

name := "literalinclude-scala"

version := "0.2"

scalaVersion := "2.11.6"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
resolvers += "spray repo" at "http://repo.spray.io"
resolvers += "spray nightlies repo" at "http://nightlies.spray.io"


libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  val sprayJsonV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % sprayJsonV,
    "io.spray"            %%  "spray-testkit" % sprayV  % Test,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % Test,
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % Test
  )
}

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % Test,
  "commons-codec" % "commons-codec" % "1.9"
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.10",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := """.*\.service\.Boot"""

// IntelliJ Scala plugin reports false positive error here
enablePlugins(JavaAppPackaging)