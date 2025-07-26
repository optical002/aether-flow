package aetherflow.engine.nativelink.gl

import scalanative.unsafe.*

@extern
object gl {
  type GLADapiproc = CFuncPtr0[Unit]
  type GLADloadfunc = CFuncPtr1[CString, GLADapiproc]
  
  def gladLoadGL(load: GLADloadfunc): CInt = extern
}
