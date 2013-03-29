
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream, ObjectInputStream }

import org.evactor.model.events.DataEvent
import scala.util.Random

object JavaSerializationListBench extends testing.Benchmark {
  val size = System.getProperty("size").toInt
  val lst = (1 to size).toList

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

object JavaSerializationEvactorBench extends testing.Benchmark {
  val size = System.getProperty("size").toInt
  //val lst = (1 to size).toList

  val time: Int = System.currentTimeMillis.toInt

  override def run() = {
    val bos = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bos)    

        // random events
    val evts = for (i <- 1 to size) yield
      DataEvent("event" + i, time + Random.nextInt(100), Random.nextString(5))

    val pickles = for (evt <- evts) yield {
      out.writeObject(evt) // pickle evt
      bos.toByteArray()
    }

    val results = for (pickle <- pickles) yield {
      //pickle.unpickle[DataEvent]
      val bis = new ByteArrayInputStream(pickle)
      val in = new ObjectInputStream(bis)
      in.readObject.asInstanceOf[DataEvent]
    }
  }
}
