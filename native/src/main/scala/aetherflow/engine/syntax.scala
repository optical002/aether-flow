package aetherflow.engine

import scalanative.unsafe.*

object syntax {
  extension (inline str: String) {
    inline def asCString(using Zone): CString = toCString(str) 
  }
  extension [R](inline fn: () => R) {
    inline def asCFunc: CFuncPtr0[R] = CFuncPtr0.fromScalaFunction(fn)
  }
  extension [A1, R](inline fn: A1 => R) {
    inline def asCFunc: CFuncPtr1[A1, R] = CFuncPtr1.fromScalaFunction(fn)
  }
  extension [A1, A2, R](inline fn: (A1, A2) => R) {
    inline def asCFunc: CFuncPtr2[A1, A2, R] = CFuncPtr2.fromScalaFunction(fn)
  }
  extension [A1, A2, A3, A4, A5, R](inline fn: (A1, A2, A3, A4, A5) => R) {
    inline def asCFunc: CFuncPtr5[A1, A2, A3, A4, A5, R] = CFuncPtr5.fromScalaFunction(fn)
  }
}
