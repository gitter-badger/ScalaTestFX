import java.net.URL

import scala.xml._

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined Scaladocs will be build without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

val scalaTestFxVersion = "0.1.0-SNAPSHOT"
val versionTagDir = if (scalaTestFxVersion.endsWith("SNAPSHOT")) "master" else "v" + scalaTestFxVersion

// ScalaTestFX project
lazy val scalatestfx = Project(
  id = "scalatestfx",
  base = file("scalatestfx"),
  settings = scalaTestFxSettings ++ Seq(
    description := "The ScalaTestFX Framework",
    fork in run := true,
    libraryDependencies ++= Seq(
      scalatest,
      testfxCore,
      scalafx
    )
  )
)

// ScalaFX Demos project
lazy val scalatestfxDemos = Project(
  id = "scalatestfx-demos",
  base = file("scalatestfx-demos"),
  settings = scalaTestFxSettings ++ Seq(
    description := "The ScalaTestFX Demonstrations",
    fork in run := true,
    javaOptions ++= Seq(
      "-Xmx512M",
      "-Djavafx.verbose"
    ),
    publishArtifact := false,
    libraryDependencies ++= Seq(
      scalafx
    )
  )
) dependsOn (scalatestfx % "compile;test->test")

// Dependencies
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.0-RC2"
lazy val testfxCore = "org.testfx" % "testfx-core" % "4.0.4-alpha"
lazy val scalafx = "org.scalafx" %% "scalafx" % "8.0.92-R10"

// Resolvers
lazy val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
lazy val sonatypeNexusStaging = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"

// Add snapshots to root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
resolvers += sonatypeNexusSnapshots

// Common settings
lazy val scalaTestFxSettings = Seq(
  organization := "io.scalatestfx",
  version := scalaTestFxVersion,
  crossScalaVersions := Seq("2.11.8", "2.12.0-M4"),
  scalaVersion <<= crossScalaVersions { versions => versions.head },
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature"),
  scalacOptions in(Compile, doc) ++= Opts.doc.title("ScalaTestFX API"),
  scalacOptions in(Compile, doc) ++= Opts.doc.version(scalaTestFxVersion),
  scalacOptions in(Compile, doc) += s"-doc-external-doc:${scalaInstance.value.libraryJar}#http://www.scala-lang.org/api/${scalaVersion.value}/",
  scalacOptions in(Compile, doc) ++= Seq("-doc-footer", s"ScalaTestFX API v.$scalaTestFxVersion"),
  javacOptions ++= Seq(
    "-target", "1.8",
    "-source", "1.8",
    "-Xlint:deprecation"
  ),
  autoAPIMappings := true,
  manifestSetting,
  publishSetting,
  fork in Test := true,
  parallelExecution in Test := false,
  resolvers += sonatypeNexusSnapshots,
  shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> " }
) ++ mavenCentralSettings

lazy val manifestSetting = packageOptions <+= (name, version, organization) map {
  (title, version, vendor) =>
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> Option(System.getenv("JAR_BUILT_BY")).getOrElse(System.getProperty("user.name")),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> version,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> version,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
}

lazy val publishSetting = publishTo <<= version {
  version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      Some(sonatypeNexusSnapshots)
    else
      Some(sonatypeNexusStaging)
}

// Metadata needed by Maven Central
// See also http://maven.apache.org/pom.html#Developers
lazy val mavenCentralSettings = Seq(
  homepage := Some(new URL("https://github.com/haraldmaida/ScalaTestFX")),
  startYear := Some(2016),
  licenses := Seq(("Apache License 2.0", new URL("https://github.com/haraldmaida/ScalaTestFX/LICENSE"))),
  pomExtra <<= (pomExtra, name, description) {
    (pom, name, desc) => pom ++ Group(
      <scm>
        <url>https://github.com/haraldmaida/ScalaTestFX</url>
        <connection>scm:git:https://github.com/haraldmaida/ScalaTestFX.git</connection>
      </scm>
        <developers>
          <developer>
            <id>haraldmaida</id>
            <name>Harald Maida</name>
            <url>https://github.com/haraldmaida</url>
          </developer>
        </developers>
    )
  }
)