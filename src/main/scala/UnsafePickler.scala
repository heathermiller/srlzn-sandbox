import java.nio.ByteBuffer
import scala.collection.mutable.ListBuffer
import UnsafeMemory._


object UnsafePickler {

  implicit val unsafeIntPickler: Pickler[Int] = new Pickler[Int] {
    def pickle(arr: Array[Byte], i: Int, value: Int): Int = {
      UnsafeMemory.putInt(arr, i, value)
      i+4
    }

    def unpickle(arr: Array[Byte], i: Int): (Int, Int) = {
      val res = UnsafeMemory.getInt(arr, i)
      (res, i+4)
    }
  }

  implicit def unsafeListPickler[T](implicit p: Pickler[T]): Pickler[List[T]] = new Pickler[List[T]] {
    def pickle(arr: Array[Byte], i: Int, xs: List[T]): Int = {
      var nextPos = unsafeIntPickler.pickle(arr, i, xs.length)
      for (x <- xs) {
        nextPos = p.pickle(arr, nextPos, x)
      }
      nextPos
    }
    def unpickle(arr: Array[Byte], i: Int): (List[T], Int) = {
      val len = unsafeIntPickler.unpickle(arr, i)._1
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
} 