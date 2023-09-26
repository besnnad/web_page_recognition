package org.example.kit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Some tools for operating .properties files
 *
 * @author bonult
 */
public class PropsKit {

	/**
	 * You can't call the constructor.
	 */
	private PropsKit(){
	}

	/**
	 * Load the property file
	 *
	 * @param fileName name of the file
	 * @return properties in the file
	 */
	public static Properties loadProps(String fileName) throws IOException{
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		if(in == null){
			throw new FileNotFoundException("File \"" + fileName + "\" is not found!");
		}
		Properties p = new Properties();
		p.load(in);
		if(in != null){
			in.close();
		}
		return p;
	}

	public static String getString(Properties props, String key, String defaultValue){
		return props.getProperty(key, defaultValue);
	}

	public static String getString(Properties props, String key){
		return props.getProperty(key, "");
	}

	public static int getInt(Properties props, String key, int defaultValue){
		if(props.containsKey(key))
			return ParseKit.parseInt(props.getProperty(key));
		return defaultValue;
	}

	public static int getInt(Properties props, String key){
		return getInt(props, key, 0);
	}

	public static boolean getBoolean(Properties props, String key, boolean defaultValue){
		if(props.containsKey(key))
			return ParseKit.parseBoolean(props.getProperty(key));
		return defaultValue;
	}

	public static boolean getBoolean(Properties props, String key){
		return getBoolean(props, key, false);
	}

	public static long getLong(Properties props, String key, long defaultValue){
		if(props.containsKey(key))
			return ParseKit.parseLong(props.getProperty(key));
		return defaultValue;
	}

	public static long getLong(Properties props, String key){
		return getLong(props, key, 0);
	}

	public static double getDouble(Properties props, String key, double defaultValue){
		if(props.containsKey(key))
			return ParseKit.parseDouble(props.getProperty(key));
		return defaultValue;
	}

	public static double getDouble(Properties props, String key){
		return getDouble(props, key, 0);
	}


}
