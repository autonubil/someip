package com.autonubil.someip.core;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.autonubil.someip.core.enums.DataType;

public class PseudoRingBufferTest {

	
	@Test
	public void testHeadersExpectSuccess() throws Exception {
		Message m = new Message(9001);
		m.setMessageType(255);
		Assert.assertEquals(255, m.getMessageType());
		for(int i=0;i<300;i++) {
			m.setClientId(i);
			Assert.assertEquals(i, m.getClientId());
		}
	}

	
	@Test
	public void testFillOverCapacityExpectSuccess() throws Exception {
		
		for(int k=1000;k<1050;k++) {
		
			for(int m=90;m<100; m++) {
				Pipe p = Pipe.open();
				p.source().configureBlocking(false);
				p.sink().configureBlocking(false);
		
				
				
				PseudoRingBuffer rbu = new PseudoRingBuffer(k, (ReadableByteChannel)p.source(), (WritableByteChannel)p.sink());
				
				List<Message> msgsOut = new ArrayList<Message>();
		
				long count = 0;
				long rcv = 0;

				{
					Message msg = new Message(1000);
					msg.setMessageType((int)(count%255));
					msg.put(count, DataType.SINT32);
					byte[] buff = new byte[m];
					msg.put(buff);
					rbu.put(msg);
					rbu.write();
					count++;
				}
		
				for(int i=1;i<1000;i++) {
					{
						Message msg = new Message(1000);
						msg.setMessageType((int)(count%255));
						msg.put(count, DataType.SINT32);
						byte[] buff = new byte[m];
						msg.put(buff);
						rbu.put(msg);
						rbu.write();
						count++;
					}
					rbu.read();
					Message mIn = rbu.get();
					Assert.assertNotNull(mIn);
					Assert.assertEquals(m+20, mIn.getTotalSize());
					Assert.assertEquals(m+12, mIn.getLength());
					Assert.assertEquals(rcv, mIn.get(DataType.SINT32));
					rcv++;
					msgsOut.add(mIn);
				}
			}
		}
	}
	
	
	
	@Test
	public void testWriteFillOverCapacityExpectSuccess() throws Exception {
		
		Pipe p = Pipe.open();
		p.source().configureBlocking(false);
		p.sink().configureBlocking(false);
		
		PseudoRingBuffer rbu = new PseudoRingBuffer(2000, (ReadableByteChannel)p.source(), (WritableByteChannel)p.sink());
		
		List<Message> msgsOut = new ArrayList<Message>();

		long count = 0;
		long rcv = 0;

		{
			Message msg = new Message(1000);
			msg.setMessageType((int)(count%255));
			msg.put(count, DataType.SINT32);
			byte[] buff = new byte[880];
			msg.put(buff);
			rbu.put(msg);
			count++;
		}
		rbu.write();

		{
			Message msg = new Message(1000);
			msg.setMessageType((int)(count%255));
			msg.put(count, DataType.SINT32);
			byte[] buff = new byte[880];
			msg.put(buff);
			rbu.put(msg);
			count++;
		}

		for(int i=1;i<1000;i++) {
			rbu.read();
			{
				Message mIn = rbu.get();
				Assert.assertNotNull(mIn);
				Assert.assertEquals(900, mIn.getTotalSize());
				Assert.assertEquals(892, mIn.getLength());
				Assert.assertEquals(rcv, mIn.get(DataType.SINT32));
				rcv++;
			}
	
			{
				Message msg = new Message(1000);
				msg.setMessageType((int)(count%255));
				msg.put(count, DataType.SINT32);
				byte[] buff = new byte[880];
				msg.put(buff);
				rbu.put(msg);
				count++;
			}
			rbu.write();
		}
	}
	
	@Test
	public void testReadPartialExpectSuccess() throws Exception {
		
		Pipe p = Pipe.open();
		p.source().configureBlocking(false);
		p.sink().configureBlocking(false);
		
		PseudoRingBuffer rbu = new PseudoRingBuffer(4000, (ReadableByteChannel)p.source(), (WritableByteChannel)p.sink());
		
		long count = 0;
		long rcv = 0;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WritableByteChannel out = Channels.newChannel(baos);
		
		for(int i=0;i<5000;i++) {
			Message msg = new Message(1000);
			msg.setMessageType((int)(count%255));
			
			msg.put(count, DataType.SINT32);

			int x = (int)((Math.random()*900)+76);
			msg.put(x+16, DataType.UINT32);
			
			byte[] buff = new byte[x];
			msg.put(buff);
			
			msg.writeTo(out);
			count++;
		}

		out.close();
		baos.flush();
		baos.close();
		
		ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
		bb.limit(100);
		
		while(true) {

			int step = 1+(int)(Math.random()*30d);
			
			p.sink().write(bb);
			
			
			{
				rbu.read();
				Message mIn = rbu.get();
				if(mIn != null) {
					System.err.println(rcv+": "+mIn.readPosition()+": "+mIn.getTotalSize()+" ("+mIn.getLength()+")");
					long lt = mIn.getTotalSize();
					long lp = mIn.getLength();
					long c = mIn.get(DataType.SINT32);
					long l = mIn.get(DataType.UINT32);
					Assert.assertEquals(rcv, c);
					Assert.assertEquals(lt, l+8);
					Assert.assertEquals(lp, l);
					rcv++;
				}
			}
			bb.limit(Math.min(bb.capacity(), bb.limit()+step));
			if(bb.remaining()==0) {
				break;
			}
		}
	}

	
}