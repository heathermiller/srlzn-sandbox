
import java.nio.ByteBuffer
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
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

  var in: Input = null
  var out: Output = null

  val outstream = new ByteArrayOutputStream()

  // val op = new Output(256, 2048 * 2048)
  // val buf = new ObjectBuffer(kryo, 1024 * 1024)

  // serialize
  def toBytes[T](obj: T): Array[Byte] = {
    var res: Array[Byte] = null
    try {
      out = new Output(outstream)
      kryo.writeClassAndObject(out, obj)
    }
    finally {
      res = out.toBytes
      out.close()
    }
    res
  }

  // deserialize
  def fromBytes[T](arr: Array[Byte]): T = {
    in = null
    var newObj: Option[T] = None
    try {
      in = new Input(new ByteArrayInputStream(arr))
      newObj = Some(kryo.readClassAndObject(in).asInstanceOf[T])
    }
    finally {
      in.close()
    }
    newObj.get
  }

  class TraversableSerializer[T, C <: Traversable[T]](builder: Builder[T, C],
    override val isImmutable: Boolean = true)
    extends KSerializer[C] {

    var in: Input = null
    var out: Output = null

    val outstream = new ByteArrayOutputStream()

    def write(kser: Kryo, op: Output, obj: C) {
      println("i swear i called write")
      var res: Array[Byte] = null
      try {
        out = new Output(outstream)
        out.writeInt(obj.size, true)
        obj.foreach { t  =>
          val tRef = t.asInstanceOf[AnyRef]
          kser.writeClassAndObject(out, tRef)
          // After each intermediate object, flush
        }
      }
      finally {
        res = out.toBytes
        out.close()
      }
      res
    }

    def read(kser: Kryo, ip: Input, cls: Class[C]): C = {
      in = null
      var newObj: Option[C] = None
      try {
        in = new Input(new ByteArrayInputStream(outstream.toByteArray))
        val size = in.readInt(true)
        // Go ahead and be faster, and not as functional cool, and be mutable in here
        val asArray = new Array[AnyRef](size)
        var idx = 0
        while(idx < size) { asArray(idx) = kser.readClassAndObject(in); idx += 1 }
        // the builder is shared, so only one Serializer at a time should use it:
        // That the array of T is materialized, build:
        builder.clear()
        asArray.foreach { item => builder += item.asInstanceOf[T] }
        newObj = Some(builder.result())
      }
      finally {
        in.close()
      }
      newObj.get
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

  override def run() {
    val ser = new KryoSerializer
    ser.out = null
    val pickled = ser.toBytes(vec)
    // println("Size: "+pickled.length)
    val res = ser.fromBytes[Vector[Int]](pickled)
  }
}
