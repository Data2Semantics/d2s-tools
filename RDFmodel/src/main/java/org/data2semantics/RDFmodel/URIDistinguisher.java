package org.data2semantics.RDFmodel;

public class URIDistinguisher {
	private Boundary   _boundary;
	private StringTree _root;
	
	public URIDistinguisher(Boundary boundary, StringTree root) {
		_boundary = boundary;
		_root     = root;
	}
	
	public StringTree get_node(String uri) {
		String path = uri;
		StringTree pos = _root;
		while (!_boundary.contains(pos) && !path.isEmpty()) {
			pos = pos.lookup(path);
			if (pos==null) break;
			path = path.substring(pos.getEdgeLabelFromParent().length());
		}
		assert pos!=null : "URI "+uri+" not encountered in the boundary :-( Boundary:\n"+_boundary;
		return pos;
	}
}
