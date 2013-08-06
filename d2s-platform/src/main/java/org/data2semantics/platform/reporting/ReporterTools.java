package org.data2semantics.platform.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.data2semantics.platform.Global;

public class ReporterTools
{
	/**
	 * Turns this string into one that is safe for media like css and filesystems. 
	 * 
	 * @param in
	 * @return
	 */
	public static String safe(String in) 
	{
		String out = in;
		
		out = out.toLowerCase();
	    out = out.replaceAll("\\s+", "-");
	    out = out.replaceAll("[^a-z0-9\\-]", ""); // remove weird characters
	    
	    // If the string starts with a digit, prepend a number 
	    if(out.length() == 0 || Character.isDigit(out.charAt(0)) )
	    	out = "n" + out;
	    
	    return out.trim();
	}
	
	/**
	 * Copies all files and directories in the given classpath directory to 
	 * the given target directory in the filesystem.
	 * 
	 * @param cpDir
	 * @param target
	 */
	public static void copy(String cpDir, File target)
	{
		URL sourcePath = ReporterTools.class.getClassLoader().getResource(cpDir);
		Global.log().info("Copying static files from path " + sourcePath);
		
		//* Copy static files (css, js, etc)
		try
		{
			copyResources(sourcePath, target);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		Global.log().info("Finished copying");				
	}
	
	public static void copyResources(URL originUrl, File destination) 
			throws IOException 
	{
		System.out.println(originUrl);
	    URLConnection urlConnection = originUrl.openConnection();
	    
	    File file = new File(originUrl.getPath());
	    if (file.exists()) 
	    {	
	    	if(file.isDirectory())
	    		FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
	    	else
	    		FileUtils.copyFile(file, new File(destination, file.getName()));
	    } else if (urlConnection instanceof JarURLConnection) 
	    {
	        copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
	    } else {
	        throw new RuntimeException("URLConnection[" + urlConnection.getClass().getSimpleName() +
	                "] is not a recognized/implemented connection type.");
	    }
	}

	public static void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) 
			throws IOException 
	{
	    JarFile jarFile = jarConnection.getJarFile();
	    
	    Enumeration<JarEntry> entries = jarFile.entries();
	    
	    while(entries.hasMoreElements()) {
	    	JarEntry entry = entries.nextElement();
	    	
	        if (entry.getName().startsWith(jarConnection.getEntryName())) 
	        {
	            String fileName = removeStart(entry.getName(), jarConnection.getEntryName());
	            if (! entry.isDirectory())
	            {
	                InputStream entryInputStream = null;
	                entryInputStream = jarFile.getInputStream(entry);
					copyStream(entryInputStream, new File(destination, fileName));
	               
	            } else
	            {
	                new File(destination, fileName).mkdirs();
	            }
	        }
	    }
	}

	private static void copyStream(InputStream in, File file) 
			throws IOException
	{
		OutputStream out = new FileOutputStream(file);
		int bt = in.read();
		while(bt != -1)
		{
			out.write(bt);
			bt = in.read();
		}
		out.flush();
		out.close();
	}
	
	private static String removeStart(String string, String prefix)
	{
		if(string.indexOf(prefix) != 0)
			return null;
		
		return string.substring(prefix.length());
	}

}
