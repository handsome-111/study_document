package buffer;

import java.nio.IntBuffer;

public class IntBuffer03 {
	public static void main(String args[]){
		IntBuffer buf = IntBuffer.allocate(10);
		IntBuffer read = null;
		
		for(int i = 0; i < 10; i++){
			buf.put(i);
		}
		
		read = buf.asReadOnlyBuffer();
		read.flip();
		while(read.hasRemaining()){
			System.out.print(read.get()+",");
		}
		
		//异常
		read.put(11);
	}
}
