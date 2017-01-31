package com.autonubil.someip.core.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autonubil.someip.core.Message;
import com.autonubil.someip.core.annotations.SomeIpField;
import com.autonubil.someip.core.annotations.SomeIpMessage;
import com.autonubil.someip.core.enums.ByteOrder;
import com.autonubil.someip.core.enums.DataType;

public class AnnotationBaseDeserializer implements Deserializer {

	
	private Map<Class<?>,List<FieldAccessor>> maps = new HashMap<Class<?>, List<FieldAccessor>>();
	
	public boolean supports(Class clazz) {
		return clazz.getAnnotation(SomeIpMessage.class)!=null;
	}

	public <T> T deserialize(Message m, Class<T> clazz) throws SerializationException, InstantiationException, IllegalAccessException {
		if(!supports(clazz)) {
			throw new IllegalArgumentException("unsupported type: "+clazz);
		}
		T t = clazz.newInstance();
		try {
			List<FieldAccessor> a = maps.get(clazz);
			if(a==null) {
				a = getAccessors(clazz);
				maps.put(clazz, a);
			}
			for(FieldAccessor fa : a) {
					fa.get(m, t);
			}
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		return t;
	}
	
	
	public List<FieldAccessor> getAccessors(Class<?> clazz) {
		List<FieldAccessor> out = new ArrayList<AnnotationBaseDeserializer.FieldAccessor>();
		for(Field f : listFields(clazz)) {
			FieldAccessor fa = new FieldAccessor(f);
			out.add(fa);
			
		}
		Collections.sort(out);
		return out;
	}
	
	
	public List<Field> listFields(Class c) {
		List<Field> out = new ArrayList<Field>();
		if(c==null) {
			return out;
		}
		for(Field f : c.getDeclaredFields()) {
			if(f.getAnnotation(SomeIpField.class)!=null) {
				out.add(f);
			}
		}
		for(Class ci : c.getInterfaces()) {
			out.addAll(listFields(ci));
		}
		
		out.addAll(listFields(c.getSuperclass()));
		return out;
	}
	

	public class FieldAccessor implements Comparable<FieldAccessor> {
		
		private int order = 0;
		private Field f;
		private int align = 0;
		private DataType dataType;
		private ByteOrder byteOrder; 
		
		public FieldAccessor(Field f) {
			this.f = f;
			this.f.setAccessible(true);
			SomeIpField sif = f.getAnnotation(SomeIpField.class);
			this.align = sif.align();
			this.dataType = sif.as();
			this.byteOrder = sif.byteOrder();
			this.order = sif.order();
		}
		
		public void get(Message m, Object o) throws IllegalArgumentException, IllegalAccessException {
			if(f.getType() == Integer.class || f.getType() == Integer.TYPE) {
				f.set(o, new Long(m.get(dataType,byteOrder)).intValue());
			} else {
				throw new IllegalArgumentException("unknown type: "+f.getType());
			}
		}

		public int compareTo(FieldAccessor o) {
			int x = new Integer(order).compareTo(o.order);
			if(x==0) {
				throw new RuntimeException("two fields with same order: "+f.getName()+" ./. "+o.f.getName());
			}
			return x;
		}
		
		
		
	}
	

}
