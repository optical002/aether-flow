package aetherflow.engine.nativelink.gl

import scalanative.unsafe.*

@link("glfw")
@extern
object GLFW {
  type GLFWWindow = CStruct0
  type GLFWMonitor = CStruct0

  type GLFWKeyFun = CFuncPtr5[Ptr[GLFWWindow], CInt, CInt, CInt, CInt, Unit]
  type GLFWErrorFun = CFuncPtr2[CInt, CString, Unit]
  type GLFWglProc = CFuncPtr0[Unit]

  def glfwInit(): CBool = extern
  def glfwCreateWindow(
    width: CInt,
    height: CInt,
    title: CString,
    monitor: Ptr[GLFWMonitor],
    share: Ptr[GLFWWindow]
  ): Ptr[GLFWWindow] = extern
  def glfwCreateWindow(
    width: CInt,
    height: CInt,
    title: CString,
  ): Ptr[GLFWWindow] = glfwCreateWindow(width = width, height = height, title = title, monitor = null, share = null)
  def glfwWindowHint(hint: CInt, value: CInt): Unit = extern
  def glfwDestroyWindow(window: Ptr[GLFWWindow]): Unit = extern
  def glfwMakeContextCurrent(window: Ptr[GLFWWindow]): Unit = extern
  def glfwWindowShouldClose(window: Ptr[GLFWWindow]): CInt = extern
  def glfwSetKeyCallback(window: Ptr[GLFWWindow], callback: GLFWKeyFun): GLFWKeyFun = extern
  def glfwSetWindowShouldClose(window: Ptr[GLFWWindow], value: CInt): Unit = extern
  def glfwGetFramebufferSize(window: Ptr[GLFWWindow], width: Ptr[CInt], height: Ptr[CInt]): Unit = extern
  def glfwGetTime(): CDouble = extern
  def glfwSwapBuffers(window: Ptr[GLFWWindow]): Unit = extern
  def glfwSwapInterval(interval: CInt): Unit = extern
  def glfwPollEvents(): Unit = extern
  def glfwSetErrorCallback(callback: GLFWErrorFun): GLFWErrorFun = extern
  def glfwTerminate(): Unit = extern
  def glfwGetProcAddress(procname: CString): GLFWglProc = extern
}
