package org.example.kit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A collection of commonly used reflection tools of java
 *
 * @author bonult
 */
public final class ReflectKit {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectKit.class);

	public static Object newInstance(Class<?> clazz){
		try{
			return clazz.newInstance();
		}catch(Exception e){
			LOGGER.error("An exception occurred while creating the object", e);
			throw new RuntimeException(e);
		}
	}

	public static Object invokeMethod(Object object, Method method, Object... args){
		try{
			method.setAccessible(true);
			return method.invoke(object, args);
		}catch(Exception e){
			LOGGER.error("An exception occurred while invoking the method", e);
			throw new RuntimeException(e);
		}
	}

	public static void setField(Object object, Field field, Object value){
		try{
			field.setAccessible(true);
			field.set(object, value);
		}catch(Exception e){
			LOGGER.error("An exception occurred while setting the field", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the value of the key pair in the map to the value of the class given in the generic parameter.
	 * And there are one-to-one matches between keys in  the map and fields in the class.
	 * If this class has super class, then the fields of the super class are also filled with some values.
	 * And a value of the map should has the same class as the value of the field.
	 *
	 * @param t      the class
	 * @param params the map
	 */
	public static <T> T fillObject(T t, Map<String,?> params){
		if(params == null || t == null)
			return t;
		Class<?> clazz = t.getClass();
		Field[] fields;
		int i;
		Object value;
		for(; clazz != Object.class; clazz = clazz.getSuperclass()){
			try{
				fields = clazz.getDeclaredFields();
				for(i = 0; i < fields.length; i++){
					value = params.get(fields[i].getName());
					if(value != null){
						fields[i].setAccessible(true);
						Class<?> fc = fields[i].getType();
						if(value instanceof String){
							if(fc.isArray())
								fields[i].set(t, ParseKit.parse(fc.getComponentType(), new String[]{(String)value}));
							else
								fields[i].set(t, ParseKit.parse(fc, (String)value));
						}else if(value instanceof String[]){
							if(fc.isArray())
								fields[i].set(t, ParseKit.parse(fc.getComponentType(), (String[])value));
							else
								fields[i].set(t, ParseKit.parse(fc, ArrayKit.getFirst((String[])value)));
						}
					}
				}
			}catch(Exception e){
			}
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	public static <T> T fillObject(Class<T> clazz, Map<String,?> params){
		return fillObject((T)ReflectKit.newInstance(clazz), params);
	}

}
