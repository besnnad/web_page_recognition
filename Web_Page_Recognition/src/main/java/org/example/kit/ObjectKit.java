package org.example.kit;

/**
 * A collection of commonly used tools of java object
 *
 * @author bonult
 */
public class ObjectKit {

	/**
	 * generic strong type conversion
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj){
		if(obj == null)
			return null;
		return (T)obj;
	}

	/**
	 * 不允许为null
	 */
	public static <T> T notNull(T t) throws NullPointerException{
		if(t == null){
			throw new NullPointerException();
		}
		return t;
	}
}
