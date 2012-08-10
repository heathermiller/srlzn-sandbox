
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


// class ListSerializer[V, T <: List[V]](emptyList: List[V]) extends KSerializer {
//   def write(kser: Kryo, out: Output, obj: T) {
//     //Write the size:
//     out.writeInt(obj.size, true)
    
//      * An excellent question arises at this point:
//      * How do we deal with List[List[T]]?
//      * Since by the time this method is called, the ListSerializer has
//      * already been registered, this iterative method will be used on
//      * each element, and we should be safe.
//      * The only risk is List[List[List[List[.....
//      * But anyone who tries that gets what they deserve
     
//     obj.foreach { t =>
//       val tRef = t.asInstanceOf[AnyRef]
//       kser.writeClassAndObject(out, tRef)
//       // After each itermediate object, flush
//       out.flush
//     }
//   }

//   def read(kser: Kryo, in: Input, cls: Class[T]) : T = {
//     //Produce the reversed list:
//     val size = in.readInt(true)
//     if (size == 0) {
//       emptyList.asInstanceOf[T]
//     }
//     else {
//       (0 until size).foldLeft(emptyList) { (l, i) =>
//         val iT = kser.readClassAndObject(in).asInstanceOf[V]
//         iT :: l
//       }.reverse.asInstanceOf[T]
//     }
//   }
// }
