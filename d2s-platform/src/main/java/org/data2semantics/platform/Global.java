package org.data2semantics.platform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.data2semantics.platform.domain.CommandLineDomain;
import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.domain.JavaDomain;
import org.data2semantics.platform.domain.PythonDomain;

public class Global implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6847885952040544661L;

	private static Map<String, Domain> domains = new HashMap<String, Domain>();
	static {
		// Search the classpath for classes tagged with @DomainDefinition
		// TODO
		
		// for now 
		domains.put("java", new JavaDomain());
		domains.put("python", new PythonDomain());
		domains.put("cli", new CommandLineDomain());
	}

	public static boolean domainExists(String name)
	{
		return domains.containsKey(name);
	}
	
	public static Domain domain(String name)
	{
		if(! domains.containsKey(name))
			throw new IllegalArgumentException("Domain "+name+" is not known.");
		
		return domains.get(name);
	}
	
	public static Domain defaultDomain()
	{
		return domain("java");
	}

	/**
	 * Shorthand for the global logger
	 * @return
	 */
	public static Logger log() { return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); }
	
	/**
	 * The default random seed. May be changed during runtime.
	 */
	public static final int RANDOM_SEED = 42;
	
	private static Random random = new Random(RANDOM_SEED);
	
	public static Random random()
	{
		return random;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
