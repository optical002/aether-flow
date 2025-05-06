ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"

val lwjglVersion = "3.3.6"
val zioVersion = "2.1.17"

lazy val commonDependencies = Seq(
  // ZIO
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-logging" % "2.5.0",
  "dev.zio" %% "zio-config-typesafe" % "4.0.4",
  "dev.zio" %% "zio-metrics-connectors" % "2.3.1",

  // Newtype
  "io.github.arturaz" %% "yantl" % "0.2.4",

  // Vector math library
  "org.joml" % "joml" % "1.10.8",

  // OS
  "com.lihaoyi" %% "os-lib" % "0.11.4",

  // Scalafx
  "org.scalafx" %% "scalafx" % "24.0.0-R35",

  // Lwjgl
  "org.lwjgl" % "lwjgl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
  "org.lwjgl" % "lwjgl" % lwjglVersion classifier "natives-windows",
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier "natives-windows",
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier "natives-windows",
)

lazy val root = (project in file("."))
  .settings(
    name := "aether-flow",
    libraryDependencies ++= commonDependencies,
  )
