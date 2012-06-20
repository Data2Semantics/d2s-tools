package org.data2semantics.tools.graphs;

/**
 * Lightweight wrapper object for vertices. Two Vertex objects are equal if the 
 * objects they wrap around are equal. Note that this may not always be the 
 * desired behavior.
 * 
 * @author Peter
 *
 * @param <Label>
 */
public class Vertex<Label> 
{
	private Label label;

	public Vertex(Label label) {
		this.label = label;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}
	
	public String toString() {
		return label.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (label == null)
		{
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
}
