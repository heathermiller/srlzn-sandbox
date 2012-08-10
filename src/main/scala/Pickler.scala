import java.nio.ByteBuffer
import scala.collection.mutable.ListBuffer

abstract class Pickler[T] {

  // returns next write position
  def pickle(arr: Array[Byte], i: Int, x: T): Int

  // returns result plus next read position
  def unpickle(arr: Array[Byte], i: Int): (T, Int)

}

object Pickler {

  implicit val intPickler: Pickler[Int] = new Pickler[Int] {
    def pickle(arr: Array[Byte], i: Int, value: Int): Int = {
      val fst = (value >>> 24).asInstanceOf[Byte]
      val snd = (value >>> 16 & 0xff).asInstanceOf[Byte]
      val thrd = (value >>> 8 & 0xff).asInstanceOf[Byte]
      val frth = (value & 0xff).asInstanceOf[Byte]
      arr(i) = fst
      arr(i+1) = snd
      arr(i+2) = thrd
      arr(i+3) = frth
      i+4
    }
    def unpickle(arr: Array[Byte], i: Int): (Int, Int) = {
      val fst = (arr(i) << 24).toInt
      val snd = ((arr(i+1) << 16) & 0x00FFFFFF).toInt
      val thrd = ((arr(i+2) << 8) & 0x0000FFFF).toInt
      val frth = (arr(i+3) & 0x000000FF).toInt
      (fst | snd | thrd | frth, i+4)
    }
  }

  implicit def seqPickler[T](implicit cm: ClassManifest[T], p: Pickler[T]): Pickler[Seq[T]] = new Pickler[Seq[T]] {
    def pickle(arr: Array[Byte], i: Int, xs: Seq[T]): Int = {
      // first, write #elems
      var nextPos = intPickler.pickle(arr, i, xs.length)
      for (x <- xs) {
        nextPos = p.pickle(arr, nextPos, x)
      }
      nextPos
    }
    def unpickle(arr: Array[Byte], i: Int): (Seq[T], Int) = {
      // first, read #elems
      val len = intPickler.unpickle(arr, i)._1
      var res = Array.ofDim[T](len)
      var nextPos = i + 4
      for (j <- 0 until len) {
        val (elem, pos) = p.unpickle(arr, nextPos)
        res(j) = elem
        nextPos = pos
      }
      (res, nextPos)
    }
  }

  implicit def listPickler[T](implicit p: Pickler[T]): Pickler[List[T]] = new Pickler[List[T]] {
    def pickle(arr: Array[Byte], i: Int, xs: List[T]): Int = {
      var nextPos = intPickler.pickle(arr, i, xs.length)
      for (x <- xs) {
        nextPos = p.pickle(arr, nextPos, x)
      }
      nextPos
    }
    def unpickle(arr: Array[Byte], i: Int): (List[T], Int) = {
      val len = intPickler.unpickle(arr, i)._1
      var nextPos = i + 4
      var res = ListBuffer[T]()
      for (j <- 0 until len) {
        val (elem, pos) = p.unpickle(arr, nextPos)
        res = res += elem
        nextPos = pos
      }
      (res.toList, nextPos)
    }
  }

  implicit def tuple2Pickler[T, U](implicit pt: Pickler[T], pu: Pickler[U]): Pickler[(T, U)] = new Pickler[(T, U)] {
    def pickle(arr: Array[Byte], i: Int, tup: (T, U)): Int = {
      val nextPos = pt.pickle(arr, i, tup._1)
      val finalPos = pu.pickle(arr, nextPos, tup._2)
      finalPos
    }
    def unpickle(arr: Array[Byte], i: Int): ((T, U), Int) = {
      val (t, nextPos) = pt.unpickle(arr, i)
      val (u, finalPos) = pu.unpickle(arr, nextPos)
      ((t, u), finalPos)
    }
  }
} 