name := "lenses"
 
version := "1.0" 
      
lazy val `lenses` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  jdbc,
  caffeine,
  ws,
  guice,
  specs2 % Test,
  "org.joda" % "joda-money" % "1.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
