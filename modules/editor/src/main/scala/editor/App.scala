package editor

import imgui.*
import imgui.`type`.ImString
import imgui.app.{Application, Configuration}
import imgui.flag.{ImGuiConfigFlags, ImGuiInputTextFlags, ImGuiWindowFlags}

class App extends imgui.app.Application {
  var str = new ImString(5)
  var flt = Array[Float](1)
  var count = 0

  override def process(): Unit = {
    if (ImGui.begin("Demo", ImGuiWindowFlags.AlwaysAutoResize)) {
      ImGui.text("OS: [" + System.getProperty("os.name") + "] Arch: [" + System.getProperty("os.arch") + "]")
      ImGui.text("Hello, World! ")
      if (ImGui.button(" Save")) {
        count = count + 1
      }
      ImGui.sameLine()
      ImGui.text(String.valueOf(count))
      ImGui.inputText("string", str, ImGuiInputTextFlags.CallbackResize)
      ImGui.text("Result: " + str.get())
      ImGui.sliderFloat("float", flt, 0, 1)
      ImGui.separator()
      ImGui.text("Extra")
    }
    ImGui.end()
    if (ImGui.begin("Demo2", ImGuiWindowFlags.AlwaysAutoResize)) {
      ImGui.text("OS: [" + System.getProperty("os.name") + "] Arch: [" + System.getProperty("os.arch") + "]")
      ImGui.text("Hello, World! ")
      if (ImGui.button(" Save")) {
        count = count + 1
      }
      ImGui.sameLine()
      ImGui.text(String.valueOf(count))
      ImGui.inputText("string", str, ImGuiInputTextFlags.CallbackResize)
      ImGui.text("Result: " + str.get())
      ImGui.sliderFloat("float", flt, 0, 1)
      ImGui.separator()
      ImGui.text("Extra")
    }
    ImGui.end()
  }

  override def configure(config: Configuration): Unit = {
    config.setTitle("Example application")
  }
}
object App {
  def main(args: Array[String]): Unit = {
    imgui.app.Application.launch(new App())
  }
}
