package org.data2semantics.platform.core.data;

public class CommandLineType implements DataType {

	public static enum Types {
		INTEGER("integer"), STRING("string");

		String v;

		private Types(String v) {
			this.v = v;
		}

		public String toString() {
			return v;
		}
	}

	Types type;

	public CommandLineType(Types type) {
		this.type = type;
	}

	@Override
	public String name() {
		return type.toString();
	}

	@Override
	public String domain() {
		return "cli";
	}

	// Getting java equivalent
	public Class<?> clazz() {
		switch (type) {
		case INTEGER:
			return Integer.class;

		}

		return String.class;
	}

	public String toString() {
		return clazz().toString();
	}

	public Object valueOf(String stringValue) {

		switch (type) {
			case INTEGER:
				return Integer.valueOf(stringValue);

		}

		return String.class;
	}
}
