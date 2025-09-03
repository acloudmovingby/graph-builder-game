enablePlugins(ScalaJSPlugin)

name := "Scala.js Tutorial"
scalaVersion := "3.7.2" // or a newer version such as "3.4.2", if you like

libraryDependencies += "acloudmovingby" %%% "graphi" % "0.0.1"

// This is an application with a main method
scalaJSUseMainModuleInitializer := true
