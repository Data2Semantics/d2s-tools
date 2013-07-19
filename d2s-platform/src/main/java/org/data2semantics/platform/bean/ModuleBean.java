package org.data2semantics.platform.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * Description of module which can be converted to YAML Bridge before the actual module
 * @author wibisono
 *
 */
public class ModuleBean {
	
	private String source;
	private String domain;
	private String description;
	private String name;
	
	
	private List<InputBean> inputs=new ArrayList<InputBean>();

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the inputs
	 */
	public List<InputBean> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(List<InputBean> inputs) {
		this.inputs = inputs;
	}

	public ModuleBean(String source, String name,
			List<InputBean> inputs) {
		super();
		this.source = source;
		this.name = name;
		this.inputs = inputs;
	}
	
	public ModuleBean() {
		
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
