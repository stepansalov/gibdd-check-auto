// Name of the project
name := "Hello SBT"

// Project version
version := "1.0.6"

// Version of Scala used by the project
scalaVersion := "2.10.2"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "play" % "play_2.10" % "2.1.0"
