package buffer;

import java.nio.Buffer;
import java.nio.IntBuffer;

public class IntBufferDemo01 {
	public static void main(String args[]){
		IntBuffer buf = IntBuffer.allocate(10);
		System.out.println("1.写入数据之前的position,limit,capacity");
		System.out.println("position="+buf.position()+",limit="+buf.limit()+",capacity="+buf.capacity());
		System.out.println();
		
		int[] temp = {7,8,9};
		buf.put(3);
		buf.put(temp);
		System.out.println("2.写入数据之后的position,limit,capacity");
		System.out.println("position="+buf.position()+",limit="+buf.limit()+",capacity="+buf.capacity());
		System.out.println();

		buf.flip();
		System.out.println("3.准备输出数据时的position.limit,capacity");
		System.out.println("position="+buf.position()+",limit="+buf.limit()+",capacity="+buf.capacity());
		System.out.println("缓冲区中的内容");
		System.out.println();

		//第一种读取缓冲区的方法
		for(int i = 0; i < buf.limit();i++){
			System.out.print(buf.get(i)+",");
		}
		System.out.println();
		
		//第二张读取缓冲区的方法
		while(buf.hasRemaining()){
			System.out.print("当前的指针位置:"+buf.position());
			//读取数据，并将指针往下移
			int t = buf.get();		
			System.out.print(t+",");
		}
		
		//将指针移动到第二个，和重置缓冲器限制
		buf.position(1);
		buf.limit(8);
		System.out.println("缓冲区限制:"+buf.limit()+",缓冲区的大小"+buf.capacity());
		buf.put(new int[]{2,2,2,2,2,2,2});
		
		//将指针移动到第一个，获取缓冲区里的所有数据
		buf.position(0);
		while(buf.hasRemaining()){
			System.out.print(buf.get());
		}
	}
}



