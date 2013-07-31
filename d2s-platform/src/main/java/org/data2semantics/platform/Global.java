package org.data2semantics.platform;

import java.util.HashMap;
import java.util.Map;

import org.data2semantics.platform.domain.Domain;
import org.data2semantics.platform.domain.JavaDomain;
import org.data2semantics.platform.domain.PythonDomain;

public class Global
{
	private static Map<String, Domain> domains = new HashMap<String, Domain>();
	static {
		// Serach the classpath for classes tagged with @DomainDefinition
		
		
		// for now 
		domains.put("java", new JavaDomain());
		domains.put("python", new PythonDomain());
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
	
}
