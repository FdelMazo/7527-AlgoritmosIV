package fiuba.fp.rand

import scala.math._

trait RNG {
  def nextProb: (Double, RNG)
}

case class LCG(
  seed: Long,
  multiplier: Long = 1103515245,
  increment: Long = 12345,
  modulo: Long = pow(2, 31).toInt) extends RNG {
  def nextProb: (Double, RNG) = {
    val newSeed = (seed * multiplier + increment) % modulo
    val nextLCG = LCG(newSeed, multiplier, increment, modulo)
    val n = newSeed.toFloat / modulo
    (n, nextLCG)
  }
}

object LCG {
  def randStream(r: RNG): Stream[Double] = r.nextProb match {
    case (prob, next) => prob #:: randStream(next)
  }
}
