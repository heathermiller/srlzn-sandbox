
import java.io.ObjectOutputStream 

object JavaSerializationListBench extends testing.Benchmark {
  val lst = (1 to 100000).toList

  override def run() = {

  }
}


// MyClass object1 = new MyClass("Hello", -7, 2.7e10); 
// System.out.println("object1: " + object1); 
// FileOutputStream fos = new FileOutputStream("serial"); 
// ObjectOutputStream oos = new ObjectOutputStream(fos); 
// oos.writeObject(object1); 
// oos.flush(); 
// oos.close();