
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("com.github.sbt" % "sbt-findbugs" % "2.0.0")

addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.0.2")

libraryDependencies += {
  val v = if(sbtVersion.value.startsWith("0.13")) "0.1.4" else "0.2.0"
  Defaults.sbtPluginExtra(
    "org.xerial.sbt" % "sbt-jcheckstyle" % v,
    sbtBinaryVersion.value,
    scalaBinaryVersion.value
  )
}

libraryDependencies += {
  val v = if(sbtVersion.value.startsWith("0.13")) "0.8.0" else "0.9.2"
  Defaults.sbtPluginExtra(
    "com.typesafe.sbt" % "sbt-osgi" % v,
    sbtBinaryVersion.value,
    scalaBinaryVersion.value
  )
}


scalacOptions ++= Seq("-deprecation", "-feature")
