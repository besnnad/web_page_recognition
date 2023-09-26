package org.example.kit;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassKit {

	public static ClassLoader getClassLoader(){
		return Thread.currentThread().getContextClassLoader();
	}

	public static Class<?> loadClass(String clazz, boolean isInitialized){
		try{
			return Class.forName(clazz, isInitialized, getClassLoader());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public static Set<Class<?>> getClassesInPackages(String pkt){
		Set<String> packages = new HashSet<>(2);
		packages.add(pkt);
		return getClassesInPackages(packages);
	}

	public static Set<Class<?>> getClassesInPackages(Set<String> packages){
		Assert.notNull(packages);
		Set<Class<?>> resultSet = new HashSet<>();
		try{
			ClassLoader cl = getClassLoader();
			for(String pkt : packages){
				Enumeration<URL> urls = cl.getResources(pkt.replace('.', '/'));
				while(urls.hasMoreElements()){
					URL url = urls.nextElement();
					String p = url.getProtocol();
					if("file".equals(p)){
						addClasses(resultSet, url.getPath().replaceAll("%20", " "), pkt);
					}else if("jar".equals(p)){
						JarURLConnection jc = (JarURLConnection)url.openConnection();
						JarFile jf = jc.getJarFile();
						Enumeration<JarEntry> jares = jf.entries();
						while(jares.hasMoreElements()){
							JarEntry je = jares.nextElement();
							String jen = je.getName();
							if(jen.endsWith(".class")){
								String clazz = jen.substring(0, jen.lastIndexOf('.')).replace('/', '.');
								resultSet.add(loadClass(clazz, false));
							}
						}
					}

				}
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return resultSet;
	}

	private static void addClasses(Set<Class<?>> set, String pktPath, String pkt){
		File[] files = new File(pktPath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
		for(File f : files){
			String fn = f.getName();
			if(f.isFile()){
				String className = fn.substring(0, fn.lastIndexOf('.'));
				if(StringKit.isNotEmpty(pkt)){
					className = pkt + "." + className;
				}
				set.add(loadClass(className, false));
			}else{
				String subPktPath = fn;
				if(StringKit.isNotEmpty(pktPath))
					subPktPath = pktPath + "/" + subPktPath;
				String subPktName = fn;
				if(StringKit.isNotEmpty(pkt))
					subPktName = pkt + "." + subPktName;
				addClasses(set, subPktPath, subPktName);
			}
		}
	}

	public static Set<Class<?>> getClassSetBySuper(Set<Class<?>> classSet, Class<?> superClass){
		Set<Class<?>> set = new HashSet<>();
		for(Class<?> aClass : classSet){
			if(superClass.isAssignableFrom(aClass) && !superClass.equals(aClass)){
				set.add(aClass);
			}
		}
		return set;
	}

	public static Set<Class<?>> getClassSetByAnnotation(Set<Class<?>> classSet, Class<? extends Annotation> ano){
		Set<Class<?>> set = new HashSet<>();
		for(Class<?> aClass : classSet){
			if(aClass.isAnnotationPresent(ano)){
				set.add(aClass);
			}
		}
		return set;
	}

	public static boolean isUsualClass(Class<?> clazz){
		return !(clazz.isAnnotation() || clazz.isInterface() || clazz.isEnum() || clazz.isArray() || clazz.isLocalClass() || clazz.isPrimitive() || clazz.isSynthetic());
	}
	public static boolean isFather(Class<?> father, Class<?> son){
		return father.isAssignableFrom(son) && !father.equals(son);
	}
}
