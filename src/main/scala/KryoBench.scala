
import java.nio.ByteBuffer
import scala.collection.immutable.Vector
import scala.collection.mutable.ListBuffer

import com.esotericsoftware.kryo._
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.{Serializer => KSerializer}
import com.esotericsoftware.kryo.io.{Input, Output}

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport

import scala.collection.mutable.Builder

class KryoSerializer {
  val kryo = new KryoReflectionFactorySupport

  // registering some basic types to try to serialize
  val toRegister = Seq(Array(1.0), Array(1), (1, 1), Some(1), Array(new Object), 1 :: Nil)
  for (obj <- toRegister) kryo.register(obj.getClass)

  // kryo.addDefaultSerializer(mf.erasure, new TraversableSerializer(List.newBuilder[Any]))

  val op = new Output(256, 2048 * 2048)
  // val buf = new ObjectBuffer(kryo, 1024 * 1024)

  // serialize
  def toBytes[T](obj: T): Array[Byte] = {
    kryo.writeClassAndObject(op, obj)
    op.getBuffer()
  }

  // deserialize
  def fromBytes[T](arr: Array[Byte]): T = {
    val ip = new Input(arr)
    kryo.readClassAndObject(ip).asInstanceOf[T]
  }

  class TraversableSerializer[T, C <: Traversable[T]](builder: Builder[T, C],
    override val isImmutable: Boolean = true)
    extends KSerializer[C] {

    def write(kser: Kryo, out: Output, obj: C) {
      //Write the size:
      out.writeInt(obj.size, true)
      obj.foreach { t  =>
        val tRef = t.asInstanceOf[AnyRef]
        kser.writeClassAndObject(out, tRef)
        // After each intermediate object, flush
        out.flush
      }
    }

    def read(kser: Kryo, in: Input, cls: Class[C]): C = {
      val size = in.readInt(true)
      // Go ahead and be faster, and not as functional cool, and be mutable in here
      val asArray = new Array[AnyRef](size)
      var idx = 0
      while(idx < size) { asArray(idx) = kser.readClassAndObject(in); idx += 1 }
      // the builder is shared, so only one Serializer at a time should use it:
      // That the array of T is materialized, build:
      builder.clear()
      asArray.foreach { item => builder += item.asInstanceOf[T] }
      builder.result()
    }
  }
  kryo.register(List(1).getClass, new TraversableSerializer(List.newBuilder[Int]))
  kryo.register(Vector(1).getClass, new TraversableSerializer(Vector.newBuilder[Int]))

  class SingletonSerializer[T](obj: T) extends KSerializer[T] {
    override def write(kryo: Kryo, output: Output, obj: T) {}
    override def read(kryo: Kryo, input: Input, cls: java.lang.Class[T]): T = obj
  }

  kryo.register(None.getClass, new SingletonSerializer[AnyRef](None))
  kryo.register(Nil.getClass, new SingletonSerializer[AnyRef](Nil))
}

object KryoListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList
  val ser = new KryoSerializer

  override def run() {
    val pickled = ser.toBytes(lst)
    // println("Size: " + pickled.length)
    val res = ser.fromBytes[List[Int]](pickled)
  }
}

object KryoVectorBench extends testing.Benchmark {
  val vec = Vector() ++ (1 to 100000)
  val ser = new KryoSerializer

  override def run() {
    val pickled = ser.toBytes(vec)
    // println("Size: "+pickled.length)
    val res = ser.fromBytes[Vector[Int]](pickled)
  }
}
