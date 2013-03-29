
import java.nio.ByteBuffer
import scala.collection.immutable.Vector
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Builder

import com.esotericsoftware.kryo._
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.{Serializer => KSerializer}

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport

class KryoSerializer {
  val kryo = new KryoReflectionFactorySupport

  // registering some basic types to try to serialize
  val toRegister = Seq(Array(1.0), Array(1), ListBuffer(1), (1, 1), Some(1), Vector(1), Array(new Object), 1 :: Nil)
  for (obj <- toRegister) kryo.register(obj.getClass)

  val buf = new ObjectBuffer(kryo, 4096 * 4096)

  // serialize
  def toBytes[T](obj: T): Array[Byte] = buf.writeClassAndObject(obj)

  // deserialize
  def fromBytes[T](arr: Array[Byte]): T = buf.readClassAndObject(arr).asInstanceOf[T]

  class ListSerializer(builder: Builder[Any, List[Any]])
  extends KSerializer {
    override def writeObjectData(buf: ByteBuffer, obj: AnyRef) {
      val lst = obj.asInstanceOf[List[Any]]
      kryo.writeObject(buf, lst.size.asInstanceOf[Int])
      for (el <- lst) {
        kryo.writeClassAndObject(buf, el)
      }
    }
    override def readObjectData[T](buf: ByteBuffer, cls: Class[T]): T = {
      val size = kryo.readObject(buf, classOf[java.lang.Integer]).intValue
      val elems = new Array[Any](size)
      for (i <- 0 until size)
        elems(i) = kryo.readClassAndObject(buf).asInstanceOf[T]
      builder.clear()
      elems.foreach { item => builder += item }
      builder.result().asInstanceOf[T]
    }
  }
  kryo.register(List(1).getClass, new ListSerializer(List.newBuilder[Any]))

  class SingletonSerializer(obj: AnyRef) extends KSerializer {
    override def writeObjectData(buf: ByteBuffer, obj: AnyRef) {}
    override def readObjectData[T](buf: ByteBuffer, cls: Class[T]): T = obj.asInstanceOf[T]
  }

  kryo.register(None.getClass, new SingletonSerializer(None))
  kryo.register(Nil.getClass, new SingletonSerializer(Nil))
}

object KryoListBench extends testing.Benchmark {
  val sz = System.getProperty("size").toInt
  val lst = ListBuffer() ++ (1 to sz)
  val ser = new KryoSerializer

  override def run() {
    val res = ser.toBytes(lst)
  }
}

object KryoVectorBench extends testing.Benchmark {
  val sz = System.getProperty("size").toInt
  val vec = Vector() ++ (1 to sz)
  val ser = new KryoSerializer

  override def run() {
    val pickled = ser.toBytes(vec)
    // println("Size: "+pickled.length)
    val res = ser.fromBytes[Vector[Int]](pickled)
  }
}
