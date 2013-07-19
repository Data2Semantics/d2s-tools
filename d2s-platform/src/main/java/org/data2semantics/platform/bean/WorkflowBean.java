package org.data2semantics.platform.bean;

import java.util.ArrayList;
import java.util.List;

public class WorkflowBean {

		public WorkflowBean() {
			
		}
		List<ModuleBean> modules = new ArrayList<ModuleBean>();
		private String name;
		/**
		 * @return the modules
		 */
		public List<ModuleBean> getModules() {
			return modules;
		}
		/**
		 * @param modules the modules to set
		 */
		public void setModules(List<ModuleBean> modules) {
			this.modules = modules;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		public WorkflowBean(List<ModuleBean> modules, String name) {
			super();
			this.modules = modules;
			this.name = name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
}
