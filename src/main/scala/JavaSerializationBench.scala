
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream, ObjectInputStream }

object JavaSerializationListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList

  override def run() = {
    val bos = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bos)    
    out.writeObject(lst)
    val bis = new ByteArrayInputStream(bos.toByteArray())
    val in = new ObjectInputStream(bis)
    val res = in.readObject.asInstanceOf[List[Int]]
  }
}