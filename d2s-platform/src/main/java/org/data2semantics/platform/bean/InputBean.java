package org.data2semantics.platform.bean;

/**
 * Not yet used
 * @author wibisono
 *
 */
public class InputBean {

		private String name;
		private Object value;
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
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(Object value) {
			this.value = value;
		}
		public InputBean(String name, Object value) {
			super();
			this.name = name;
			this.value = value;
		}
		public InputBean() {
			
		}
}
