name := "Event Filters"
version := "1.0.5"
scalaVersion := "2.10.2"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "play" % "play_2.10" % "2.1.0"

// Fork a new JVM for 'run' and 'test:run'
fork := true
