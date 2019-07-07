package buffer;

import java.nio.IntBuffer;

public class IntBufferDemo02 {
	public static void main(String args[]){
		IntBuffer buf = IntBuffer.allocate(10);
		IntBuffer sub = null;
		for(int i = 0; i < 10; i++){
			buf.put(2*i+1);
		}
		System.out.println("position="+buf.position()+",limit="+buf.limit()+",capacity="+buf.capacity());
		//将指针移动到0
		buf.flip();
		while(buf.hasRemaining()){
			System.out.print(buf.get()+",");
		}
		System.out.println();
		
		
		buf.position(2);
		buf.limit(6);
		//从当前位置里剪出到limit位置成为子缓冲区，即2~6,但是不包括6
		sub = buf.slice();
		for(int i = 0; i < sub.capacity();i ++){
			int temp = sub.get(i);
			sub.put(temp-1);
		}
		
		sub.flip();
		while(sub.hasRemaining()){
			System.out.print(sub.get()+",");
		}
	}
}
