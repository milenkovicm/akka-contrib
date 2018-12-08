// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `akka-contrib` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.scalaTest % Test,
        library.scalaLogging
      ),
        libraryDependencies ++= library.akka,
        libraryDependencies ++= library.kamon
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaTest    = "3.0.5"
      val akka         = "2.5.19"
      val scalaLogging = "3.9.0"
    }

    val scalaTest       = "org.scalatest"                    %% "scalatest"               % Version.scalaTest
    val scalaLogging    = "com.typesafe.scala-logging"       %% "scala-logging"           % Version.scalaLogging
    val akka = Seq(
      "com.typesafe.akka" %% "akka-actor"               % Version.akka,
      "com.typesafe.akka" %% "akka-stream"              % Version.akka,
    )

    val kamon = Seq(
      "io.kamon"    %% "kamon-core"            % "1.1.3",
      "io.kamon"    %% "kamon-system-metrics"  % "1.0.0",
      "org.aspectj" % "aspectjweaver"          % "1.9.2"
    )

  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    organization := "com.github.milenkovicm",
    organizationName := "Marko Milenkovic",
    startYear := Some(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
)

lazy val bintrayPublishing = Seq(
  bintrayPackageLabels := Seq("akka", "scala", "kamon"),
  bintrayVcsUrl := Some("https://github.com/milenkovicm/akka-contrib"),
  bintrayReleaseOnPublish := false
)

val VersionRegex = "([0-9]+).([0-9]+).([0-9]+)-?(.*)?".r
lazy val gitSettings =
  Seq(
    git.useGitDescribe := true,
    git.gitTagToVersionNumber := {
      case VersionRegex(v, s, t, "") ⇒ Some(s"$v.$s.$t")
      case VersionRegex(v, s, t, _)  ⇒ Some(s"$v.$s.${Integer.parseInt(t) + 1}-SNAPSHOT")
      case _                         ⇒ None
    }
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val settings =
  commonSettings ++
  scalafmtSettings ++
  bintrayPublishing ++
  gitSettings
