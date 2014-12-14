/*
 * Copyright 2012 Taro L. Saito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import de.johoop.findbugs4sbt.ReportType
import sbt._
import Keys._
import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import sbtrelease.ReleasePlugin._
import xerial.sbt.Sonatype._

object Build extends Build {

  val SCALA_VERSION = "2.11.1"

  lazy val buildSettings = Defaults.coreDefaultSettings ++
    releaseSettings ++
    findbugsSettings ++
    jacoco.settings ++
    Seq[Setting[_]](
      organization := "org.msgpack",
      organizationName := "MessagePack",
      organizationHomepage := Some(new URL("http://msgpack.org/")),
      description := "MessagePack for Java",
      scalaVersion in Global := SCALA_VERSION,
      logBuffered in Test := false,
      //parallelExecution in Test := false,
      autoScalaLibrary := false,
      crossPaths := false,
      concurrentRestrictions in Global := Seq(
        Tags.limit(Tags.Test, 1)
      ),
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      parallelExecution in jacoco.Config := false,
      // Since sbt-0.13.2
      incOptions := incOptions.value.withNameHashing(true),
      //resolvers += Resolver.mavenLocal,
      scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-target:jvm-1.6", "-feature"),
      javaOptions in Test ++= Seq("-ea"),
      javacOptions in (Compile, compile) ++= Seq("-encoding", "UTF-8", "-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.6", "-target", "1.6"),
      javacOptions in doc := Seq("-source", "1.6", "-Xdoclint:none"),
      findbugsReportType := Some(ReportType.FancyHtml),
      findbugsReportPath := Some(crossTarget.value / "findbugs" / "report.html"),
      pomExtra := {
        <url>http://msgpack.org/</url>
          <licenses>
            <license>
              <name>Apache 2</name>
              <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            </license>
          </licenses>
          <scm>
            <connection>scm:git:github.com/msgpack/msgpack-java.git</connection>
            <developerConnection>scm:git:git@github.com:msgpack/msgpack-java.git</developerConnection>
            <url>github.com/msgpack/msgpack-java.git</url>
          </scm>
          <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          </properties>
          <developers>
            <developer>
              <id>frsyuki</id>
              <name>Sadayuki Furuhashi</name>
              <email>frsyuki@users.sourceforge.jp</email>
            </developer>
            <developer>
              <id>muga</id>
              <name>Muga Nishizawa</name>
              <email>muga.nishizawa@gmail.com</email>
            </developer>
            <developer>
              <id>oza</id>
              <name>Tsuyoshi Ozawa</name>
              <url>https://github.com/oza</url>
            </developer>
            <developer>
              <id>komamitsu</id>
              <name>Mitsunori Komatsu</name>
              <email>komamitsu@gmail.com</email>
            </developer>
            <developer>
              <id>xerial</id>
              <name>Taro L. Saito</name>
              <email>leo@xerial.org</email>
            </developer>
          </developers>
      }
    )

  import Dependencies._


  lazy val root = Project(
    id = "msgpack-java",
    base = file("."),
    settings = buildSettings ++ Seq(
      findbugs := {
        // do not run findbugs for the root project
      },
      // Do not publish the root project
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    )
  ) aggregate(msgpackCore, msgpackJackson)


  lazy val msgpackCore = Project(
    id = "msgpack-core",
    base = file("msgpack-core"),
    settings = buildSettings ++ Seq(
      description := "Core library of the MessagePack for Java",
      libraryDependencies ++= testLib
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/rickynils/scalacheck.git#235be999587"), "jvm")
  )

  lazy val msgpackJackson = Project(
    id = "msgpack-jackson",
    base = file("msgpack-jackson"),
    settings = buildSettings ++ Seq(
      name := "jackson-dataformat-msgpack",
      description := "Jackson extension that adds support for MessagePack",
      libraryDependencies ++= jacksonLib,
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
    )
  ).dependsOn(msgpackCore)

  object Dependencies {

    val testLib = Seq(
      "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
      "org.xerial" % "xerial-core" % "3.3.0" % "test",
      "org.msgpack" % "msgpack" % "0.6.9" % "test",
      "com.novocode" % "junit-interface" % "0.10" % "test",
      "commons-codec" % "commons-codec" % "1.9" % "test"
    )

    val jacksonLib = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3",
      "com.novocode" % "junit-interface" % "0.10" % "test",
      "org.apache.commons" % "commons-math3" % "3.3" % "test"
    )
  }

}








