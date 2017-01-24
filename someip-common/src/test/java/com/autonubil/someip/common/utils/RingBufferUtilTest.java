package com.autonubil.someip.common.utils;

import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.autonubil.someip.common.SomeIpMessage;

public class RingBufferUtilTest {

	
	@Test
	public void testHeadersExpectSuccess() throws Exception {
		SomeIpMessage m = new SomeIpMessage(9000);
		m.setMessageType(255);
		Assert.assertEquals(255, m.getMessageType());
		for(int i=0;i<300;i++) {
			m.setClientId(i);
			m.readMetaData();
			Assert.assertEquals(i, m.getClientId());
		}
		for(int i=65535;i<65635;i++) {
			m.setClientId(i);
			m.readMetaData();
			Assert.assertEquals(i%65536, m.getClientId());
		}
		for(int i=0;i<300;i++) {
			m.setProtocolVersion(i);
			m.readMetaData();
			Assert.assertEquals(i%256, m.getProtocolVersion());
		}
	}

	
	@Test
	public void testFillOverCapacityExpectSuccess() throws Exception {
		
		Pipe p = Pipe.open();
		p.source().configureBlocking(false);
		p.sink().configureBlocking(false);

		PseudoRingBuffer rbu = new PseudoRingBuffer(8000, (ReadableByteChannel)p.source(), (WritableByteChannel)p.sink());
		
		
		List<SomeIpMessage> msgsIn = new ArrayList<SomeIpMessage>();
		
		for(int i=0;i<16;i++) {
			SomeIpMessage m = new SomeIpMessage(9000);
			m.setMessageType(255);
			m.putInt(i);
			byte[] buff = new byte[880];
			m.put(buff);
			msgsIn.add(m);
		}
		
		for(int i=0; i < 16;i++) {
			msgsIn.get(i).position(0);
			Assert.assertEquals(i, msgsIn.get(i).getInt());
			msgsIn.get(i).position(0);
			Assert.assertEquals(i, msgsIn.get(i).getInt());
		}
		
		
		for(int i=0;i<8;i++) {
			rbu.put(msgsIn.remove(0));
		}
		rbu.write();
		
		List<SomeIpMessage> msgs = new ArrayList<SomeIpMessage>();
		
		for(int i=0;i<7;i++) {
			rbu.read();
			SomeIpMessage msg = rbu.get();
			if(msg!=null) {
				msg.position(0);
				Assert.assertEquals(msgs.size(), msg.getInt());
				msgs.add(msg);
			}
		}

		for(int i=0;i<8;i++) {
			rbu.put(msgsIn.remove(0));
			rbu.write();
		}

		rbu.write();
		
		for(int i=0;i<20;i++) {
			rbu.read();
			SomeIpMessage msg = rbu.get();
			if(msg!=null) {
				msg.position(0);
				Assert.assertEquals(msgs.size(), msg.getInt());
				msgs.add(msg);
			}
		}
		
		Assert.assertEquals(16, msgs.size());
		
	}

	
	
	@Test
	public void testOneMessageExpectSuccess() throws Exception {
		
		Pipe p = Pipe.open();

		PseudoRingBuffer rbu = new PseudoRingBuffer(8000, (ReadableByteChannel)p.source(), null);
		
		p.source().configureBlocking(false);
		p.sink().configureBlocking(false);
		
		SecureRandom sr = new SecureRandom();
		
		long total = 0;
		
		for(int i=0;i<1000;i++) {

			int length = (int)Math.round(Math.random()*6000d);
			
			System.err.println(i+" --- "+total+" - "+length);
			total = total + length;
			
			SomeIpMessage m = new SomeIpMessage(8092);
			
			m.setClientId(0xFFFF0000);
			m.setSessionId(0xFFFF);

			m.setMessageType(i);
			
			
			Assert.assertEquals(16, m.getTotalSize());
			Assert.assertEquals(i%256, m.getMessageType());

			// put a long
			m.putLong(System.currentTimeMillis());
			Assert.assertEquals(24, m.getTotalSize());
			
			// put an int
			m.putInt((int)Math.round(Math.random()*6000d));
			Assert.assertEquals(28, m.getTotalSize());
			
			// put an array of random bytes
			byte[] buff = new byte[length];
			sr.nextBytes(buff);
			m.put(buff);
			Assert.assertEquals(28+buff.length, m.getTotalSize());
			
			m.position(m.position()-buff.length);
			byte[] bX = m.get(buff.length);
			Assert.assertArrayEquals(buff, bX);
			
			m.writeTo(p.sink());
			
			List<SomeIpMessage> m2 = new ArrayList<SomeIpMessage>();
			
			rbu.read();
			m2.add(rbu.get());
			
			Assert.assertEquals(1, m2.size());
			m.position(0);
			m2.get(0).position(0);
			
			Assert.assertEquals(m.getLong(), m2.get(0).getLong());
			Assert.assertEquals(m.getInt(), m2.get(0).getInt());

			byte[] buff2 = m2.get(0).get(length);
			Assert.assertArrayEquals(buff, buff2);
			
			Assert.assertEquals(m.getTotalSize(), m2.get(0).getTotalSize());

			Assert.assertEquals(m.getMessageType(), m2.get(0).getMessageType());
			
		}
		
		
	}
	
}
