package stu.wl.twitter.rabbitmq.confirm;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send2 {
	public static final String QUEUE_NAME="test_queue_confirm4";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		Channel channel = conn.createChannel();
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		//选择confirm模式
		channel.confirmSelect();
		//同步有序集合，使用有序集合来标记
		final SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
		
		channel.addConfirmListener(new ConfirmListener(){
			//当消费者显示的发回ack确认之后就会调用该方法
			public void handleAck(long deliveryTag, boolean multiple) throws IOException {
				if(multiple){
					System.out.println("-------handleAck----multipe");
					confirmSet.headSet(deliveryTag + 1).clear();
				}else{
					System.out.println("-------handleAck----multipe-----false");
					confirmSet.remove(deliveryTag);
				}
			}
			//否定应答则调用该方法，即不使用应答方式
			public void handleNack(long deliveryTag, boolean multiple) throws IOException {
				System.out.println("Nack, SeqNo: " + deliveryTag + ", multiple: " + multiple);
                if (multiple) {
					System.out.println("-------handleAck----multipe");
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
					System.out.println("-------handleAck----multipe-----false");
                    confirmSet.remove(deliveryTag);
                }
			}
			
		});
		
		String message = "你好啊 宝贝";
		
		while (true) {
			long nextSeqNo = channel.getNextPublishSeqNo();
			//发送消息，并持久化消息
			channel.basicPublish("", QUEUE_NAME,  MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
			confirmSet.add(nextSeqNo);
	    }
	}
}
