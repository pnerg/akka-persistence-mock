name := "akka-persistence-mock"
organization := "org.dmonix.akka"
version := "1.0"

scalaVersion := "2.11.6"

val akkaVersion = "2.4-M3"


scalacOptions <++= scalaVersion map { (v: String) => 
  if (v.trim.startsWith("2.1"))
    Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:higherKinds", "-target:jvm-1.7")
  else
    Seq("-deprecation", "-unchecked")
}

scalacOptions in (Compile, doc) ++= Seq("-doc-title", "Akka Persistence Mock API")
scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/src/main/scaladoc/root-doc.txt")
scalacOptions in (Compile, doc) ++= Seq("-doc-footer", "Copyright (c) 2015 Peter Nerg, Apache License v2.0.")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.slf4j" % "slf4j-log4j12" % "1.7.5",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test", 
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-persistence-tck" % akkaVersion % "test"
)

//sbt-coverage settings
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else false
}

//setting for eclipse plugin to download sources
EclipseKeys.withSource := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.endsWith("-SNAPSHOT"))
    Some("snapshots" at nexus+"content/repositories/snapshots")
  else
    Some("releases" at nexus+"service/local/staging/deploy/maven2")
}



//----------------------------
//info for where and how to publish artifacts
//----------------------------
credentials ++= {
  val sonatype = ("Sonatype Nexus Repository Manager", "oss.sonatype.org")
  def loadMavenCredentials(file: java.io.File) : Seq[Credentials] = {
    xml.XML.loadFile(file) \ "servers" \ "server" map (s => {
      val host = (s \ "id").text
      val realm = if (host == sonatype._2) sonatype._1 else "Unknown"
      Credentials(realm, host, (s \ "username").text, (s \ "password").text)
    })
  }
  val ivyCredentials   = Path.userHome / ".ivy2" / ".credentials"
  val mavenCredentials = Path.userHome / ".m2"   / "settings.xml"
  (ivyCredentials.asFile, mavenCredentials.asFile) match {
    case (ivy, _) if ivy.canRead => Credentials(ivy) :: Nil
    case (_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
    case _ => Nil
  }
}

//----------------------------
//needed to create the proper pom.xml for publishing to mvn central
//----------------------------
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
pomExtra := (
  <url>https://github.com/pnerg/akka-persistence-mock</url>
  <licenses>
    <license>
      <name>Apache</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:pnerg/akka-persistence-mock.git</url>
    <connection>scm:git:git@github.com:pnerg/akka-persistence-mock.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pnerg</id>
      <name>Peter Nerg</name>
      <url>http://github.com/pnerg</url>
    </developer>
  </developers>)
