inThisBuild(List(
  crossScalaVersions := Seq(scalaVersion.value),
  description := "Trying out ",
  organization := "com.julianpeeters",
  homepage := Some(url("https://github.com/julianpeeters/calico-centipede")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "julianpeeters",
      "Julian Peeters",
      "julianpeeters@gmail.com",
      url("http://github.com/julianpeeters")
    )
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Werror",
    "-source:future",
    "-Wunused:all",
    "-Wvalue-discard"
  ),
  scalaVersion := "3.3.5",
  versionScheme := Some("semver-spec"),
))

lazy val centipede = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.armanbilge" %%% "calico" % "0.2.3"
    )
  )