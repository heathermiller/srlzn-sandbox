
import java.nio.ByteBuffer
import scala.collection.immutable.Vector
import scala.collection.mutable.ListBuffer

import com.esotericsoftware.kryo._
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.{Serializer => KSerializer}

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport

class KryoSerializer {
  val kryo = new KryoReflectionFactorySupport

  // registering some basic types to try to serialize
  val toRegister = Seq(Array(1.0), Array(1), ListBuffer(1), (1, 1), Some(1), Vector(1), Array(new Object), 1 :: Nil)
  for (obj <- toRegister) kryo.register(obj.getClass)

  val buf = new ObjectBuffer(kryo, 1024 * 1024)

  // serialize
  def toBytes[T](obj: T): Array[Byte] = buf.writeClassAndObject(obj)

  // deserialize
  def fromBytes[T](arr: Array[Byte]): T = buf.readClassAndObject(arr).asInstanceOf[T]

  class SingletonSerializer(obj: AnyRef) extends KSerializer {
    override def writeObjectData(buf: ByteBuffer, obj: AnyRef) {}
    override def readObjectData[T](buf: ByteBuffer, cls: Class[T]): T = obj.asInstanceOf[T]
  }

  kryo.register(None.getClass, new SingletonSerializer(None))
  kryo.register(Nil.getClass, new SingletonSerializer(Nil))
}

object KryoListBench extends testing.Benchmark {
  val lst = ListBuffer() ++ (1 to 10000)
  val ser = new KryoSerializer

  override def run() {
    val res = ser.toBytes(lst)
  } 
}

object KryoVectorBench extends testing.Benchmark {
  val vec = Vector() ++ (1 to 100000)
  val ser = new KryoSerializer

  override def run() {
    val res = ser.fromBytes[Vector[Int]](ser.toBytes(vec))
  } 
}
