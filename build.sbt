ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"

val lwjglVersion = "3.3.6"
val imguiVersion = "1.89.0"
val zioVersion = "2.1.17"

lazy val commonDependencies = Seq(
  // Effect
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-logging" % "2.5.0",
  "dev.zio" %% "zio-config-typesafe" % "4.0.4",

  // Newtype
  "io.github.arturaz" %% "yantl" % "0.2.4",

  // Vector math library
  "org.joml" % "joml" % "1.10.8",

  // OS
  "com.lihaoyi" %% "os-lib" % "0.11.4",
)

lazy val graphicsCore = (project in file("modules/engine/graphics/core"))
  .settings(
    libraryDependencies ++= commonDependencies
  )

lazy val openGlGraphics = (project in file("modules/engine/graphics/opengl"))
  .settings(
    libraryDependencies ++= Seq(
      "org.lwjgl" % "lwjgl" % lwjglVersion,
      "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
      "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
      "org.lwjgl" % "lwjgl" % lwjglVersion classifier "natives-windows",
      "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier "natives-windows",
      "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier "natives-windows",
    )
  ).dependsOn(graphicsCore)

lazy val engine = (project in file("modules/engine"))
  .settings(
    libraryDependencies ++= commonDependencies
  ).dependsOn(graphicsCore)

lazy val editor = (project in file("modules/editor"))
  .settings(
    libraryDependencies ++= Seq(
      "io.github.spair" % "imgui-java-app" % imguiVersion,
      "io.github.spair" % "imgui-java-binding" % imguiVersion,
      "io.github.spair" % "imgui-java-lwjgl3" % imguiVersion,
    )
  )
  .dependsOn(engine)

lazy val game = (project in file("modules/game"))
  .enablePlugins(NativeImagePlugin)
  .settings(
    Compile / mainClass := Some("GameMain"),
    assembly / mainClass := Some("GameMain"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
  )
  .dependsOn(engine, openGlGraphics)

lazy val root = (project in file("."))
  .settings(
    name := "fp-game-engine"
  )
  .aggregate(editor, engine, game, graphicsCore, openGlGraphics)
