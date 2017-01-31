class Complex(real: Double, imaginary: Double) {
  def re = real
  def im = imaginary
}

object ComplexApp extends App {
  val cplx = new Complex(1.5, 2.3)
  println(cplx.re.toString)
  println(cplx.im.toString)
}