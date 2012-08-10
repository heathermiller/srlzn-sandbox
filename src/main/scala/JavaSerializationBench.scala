
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream, ObjectInputStream }

object JavaSerializationListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList

  override def run() = {
    val bos = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bos)    
    out.writeObject(lst)
    val ba = bos.toByteArray()
    // println("Bytes: " + ba.length)
    val bis = new ByteArrayInputStream(ba)
    val in = new ObjectInputStream(bis)
    val res = in.readObject.asInstanceOf[List[Int]]
  }
}