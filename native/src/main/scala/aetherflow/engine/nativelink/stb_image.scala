package aetherflow.engine.nativelink

import scalanative.unsafe.*

@extern
object stb_image {
  //noinspection SpellCheckingInspection
  // STBIDEF stbi_uc *stbi_load(char const *filename, int *x, int *y, int *comp, int req_comp)
  def stbi_load(filename: CString, x: Ptr[CInt], y: Ptr[CInt], comp: Ptr[CInt], req_comp: CInt): Ptr[Byte] = extern
  //noinspection SpellCheckingInspection
  def stbi_image_free(data: Ptr[Byte]): Unit = extern
}
