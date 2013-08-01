package org.data2semantics.platform.domain;

import java.util.List;

import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;

/**
 * A domain is a particular language, environment or method for executing code
 * (eg: Java, python, the UNIX command line, MATLAB etc).  
 * 
 * Domains can be plugged in dynamically: The Runner class scans the classpath 
 * for any class tagged @Domain(prefix="..."), where the prefix is used in the 
 * workflow description to indicate to which domain a module belongs
 * 
 * Domains which do not provide static typing must find some way to the workflow 
 * author to specify types in that domain. Something akin to java annotations is 
 * preferable, if available. Another approach would be to specify metadata files
 *  
 * 
 * 
 * @author Peter
 *
 */
public interface Domain
{
	
	/**
	 * This method checks whether two domain-specific typescan be matched. Ie.
	 * this method returns true if, under the logic of the domain, the value of
	 * the given output can be used as a value for the given input.
	 * 
	 * In some domains (like Java) this may involve complicated checking of type 
	 * hierarchies. In others (like the CLI) only strings may be allowed.
	 * 
	 * The module can indicate here that it can convert values from other 
	 * domains (ie. a python xml document to a java xml document). If so, this 
	 * must be implemented in the execute method, where the module will be 
	 * provided with the original value in the original domain. 
	 * 
	 * @param output
	 * @param input
	 * @return
	 */
	public boolean typeMatches(Output output, Input input);

	/**
	 * Checks whether the given module instance can be executed by this domain.
	 * This is a preflight-check that does not actually execute the model.
	 * 
	 * @param instance
	 * @param errors If the method returns false, the errors encountered will be 
	 * added to this list. 
	 * @return
	 */
	public boolean check(ModuleInstance instance, List<String> errors);
	
	/**
	 * If the inputs and outputs cannot be matched directly, the platform will
	 * attempt to perform some basic conversions in order to make the workflow
	 * fit. 
	 * 
	 * For example, if the workflow description connects a python int output to 
	 * a java int input, the converter can 
	 * 
	 * Domains should strive to provide conversions for basic datatypes to their
	 * raw equivalents, but not much more (to avoid blowing up the search space). 
	 * 
	 * It is also important that not too much interpretation of used here. For 
	 * instance, an int may easily be converted into a string, but doing so 
	 * automatically may hide a mistake in the workflow description. In such 
	 * cases a mechanism for explicit conversion may be better suited (although
	 * a module may be too verbose, we could consider a kind of anonymous module
	 * construct). 
	 * 
	 * @param type
	 * @return
	 */
	public List<DataType> conversions(DataType type);
	
	/**
	 * Executes the given module. 
	 * 
	 * @return
	 */
	public boolean execute(ModuleInstance instance, List<String> errors);
	
	/**
	 * 
	 * @param source The source description without the domain prefix
	 * @param name
	 * @return
	 */
	public DataType inputType(String source, String name);
	
	
	/**
	 * 
	 * @param source The source description without the domain prefix
	 * @param name
	 * @return
	 */
	public DataType outputType(String source, String name);

	/**
	 * Listing the annotated output names of the current module.
	 * 
	 * @param source The source description without the domain prefix
	 * @return
	 */
	
	public List<String> outputs(String source);
	
	public boolean valueMatches(Object value, DataType type);
}
