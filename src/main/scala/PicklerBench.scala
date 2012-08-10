import Pickler._

object PicklerTest extends App {
  val arr: Array[Byte] = Array.fill[Byte](1000)(0)
  intPickler.pickle(arr, 0, 12)
  println(intPickler.unpickle(arr, 0)._1)

  val lst = List(5, 6, 7)
  seqPickler[Int].pickle(arr, 4, lst)
  val lst2 = seqPickler[Int].unpickle(arr, 4)
  println(lst2._1)

  val tup = (6, 7)
  tuple2Pickler[Int, Int].pickle(arr, lst2._2, tup)
  val tup2 = tuple2Pickler[Int, Int].unpickle(arr, lst2._2)
  println(tup2._1)

  val lstTup = List((1, 2), (3, 4), (5, 6))
  seqPickler[(Int, Int)].pickle(arr, tup2._2, lstTup)
  val lstTup2 = seqPickler[(Int, Int)].unpickle(arr, tup2._2)
  println(lstTup2._1)

  val lstLst = List(List(1), List(2), List(3))
  seqPickler[Seq[Int]].pickle(arr, lstTup2._2, lstLst)
  val lstLst2 = seqPickler[Seq[Int]].unpickle(arr, lstTup2._2)
  println(lstLst2._1)

}

object PicklerListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList
  val arr: Array[Byte] = Array.fill[Byte](lst.length * 4 + 4)(0)

  override def run() {
    listPickler[Int].pickle(arr, 0, lst)
    val res = listPickler[Int].unpickle(arr, 0)
  } 
}

object PicklerSeqListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList
  val arr: Array[Byte] = Array.fill[Byte](lst.length * 4 + 4)(0)

  override def run() {
    seqPickler[Int].pickle(arr, 0, lst)
    val res = seqPickler[Int].unpickle(arr, 0)
  } 
}

// object PicklerVectorBench extends testing.Benchmark {
//   val vec = Vector() ++ (1 to 100000)
//   val ser = new KryoSerializer

//   override def run() {
//     val res = ser.fromBytes[Vector[Int]](ser.toBytes(vec))
//   } 
// }