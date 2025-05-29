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

  // Mutable Java OpenGL Math library
  "org.joml" % "joml" % "1.10.8",

  // OS
  "com.lihaoyi" %% "os-lib" % "0.11.4",

  // Lwjgl
  "org.lwjgl" % "lwjgl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-stb" % lwjglVersion,

  // Lwjgl windows .dll's
  "org.lwjgl" % "lwjgl" % lwjglVersion classifier "natives-windows",
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier "natives-windows",
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier "natives-windows",
  "org.lwjgl" % "lwjgl-stb" % lwjglVersion classifier "natives-windows",

  // Lwjgl linux .dll's
  "org.lwjgl" % "lwjgl" % lwjglVersion classifier "natives-linux",
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier "natives-linux",
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier "natives-linux",
  "org.lwjgl" % "lwjgl-stb" % lwjglVersion classifier "natives-linux",
)

lazy val native = (project in file("native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "aether-flow-native",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.7-4972921"
    ),
    Compile / mainClass := Some("aetherflow.engine.NativeMain"),
  )

lazy val root = (project in file("."))
  .settings(
    name := "aether-flow",
    libraryDependencies ++= commonDependencies,
  )
