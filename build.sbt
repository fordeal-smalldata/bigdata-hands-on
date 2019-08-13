name := "bigdata-hands-on"

version := "0.1"

scalaVersion := "2.12.9"

lazy val root = project.in(file("."))
  .enablePlugins(OrnatePlugin)
// https://szeiger.github.io/ornate-doc/index.html

libraryDependencies += "com.novocode" %% "ornate" % "0.6"