
object UnsafeMemory {

  val unsafe: sun.misc.Unsafe = scala.concurrent.util.Unsafe.instance
  val byteArrayOffset: Long = unsafe.arrayBaseOffset(classOf[Array[Byte]])

  def putInt(buffer: Array[Byte], pos: Int, value: Int): Unit = {
    unsafe.putInt(buffer, byteArrayOffset + pos, value)
  }

  def getInt(buffer: Array[Byte], pos: Int): Int = {
    unsafe.getInt(buffer, byteArrayOffset + pos)
  }

/*
    public void putLongArray(final long[] values)
    {
        putInt(values.length);

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(values, longArrayOffset,
                          buffer, byteArrayOffset + pos,
                          bytesToCopy);
        pos += bytesToCopy;
    }

    public long[] getLongArray()
    {
        int arraySize = getInt();
        long[] values = new long[arraySize];

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(buffer, byteArrayOffset + pos,
                          values, longArrayOffset,
                          bytesToCopy);
        pos += bytesToCopy;

        return values;
    }
*/
}
