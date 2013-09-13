package org.data2semantics.RDFmodel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringTree {
	
	private boolean _isSorted;
	private boolean _isEndPoint;  // whether the path from the root to here represents a complete path
    private ArrayList<StringTree> _children; // set to null until a first child is added
    private StringTree _parent;
    private String     _edgeLabelFromParent;
    
    public StringTree() { this(null, null, false); }
    
	public StringTree(StringTree parent, String edgeLabelFromParent, boolean isEndPoint) {
		_edgeLabelFromParent = edgeLabelFromParent;
		_isEndPoint = isEndPoint;
		_isSorted = true; // zero or one children are always sorted
		_children = null;
		_parent = parent;
	}
		
	public StringTree(Collection<?> items) {
		this();
		for (Object o : items) add(o.toString());
	}
	
	public StringTree(String packed) {
		this();
		int processed = unpack(packed, 0);
		assert processed == packed.length() : "Error unpacking StringTree";
	}
	
	public boolean isLeaf() { return _children==null; }
	
	public StringTree lookup(String path) {
		for (StringTree ch : _children) {
			if (path.startsWith(ch._edgeLabelFromParent)) return ch;
		}
		return null;
	}
	
	public String getEdgeLabelFromParent() { return _edgeLabelFromParent; }
	
	public String pathFromRoot() {
		StringTree p = getParent();
		return p==null ? "" : p.pathFromRoot() + _edgeLabelFromParent;
	}
	
	public StringTree getParent() { return _parent; }
	public List<StringTree> getChildren() { return _children; }
	
	// returns index of first undefined bit in bitset
	private int keyRec(BitSet bs, int ix, Boundary B) {
		boolean leaf = B.contains(this);
		bs.set(ix++, leaf);
		if (!leaf) for (StringTree ch : _children) ix = ch.keyRec(bs, ix, B);
		return ix;
	}
	
	public BitSet key(Boundary B) {
		BitSet bs = new BitSet();
		keyRec(bs, 0, B);
		return bs;
	}
	
	public void add(String path) {
		if (path.isEmpty()) { _isEndPoint = true; return; }
		char c = path.charAt(0);
		StringTree child = null;
		if (_children!=null) {
			for (StringTree test_child : _children) {
				char cc = test_child._edgeLabelFromParent.charAt(0);
				if (c == cc) { child = test_child; break; }
			}
		}
		if (child == null) {
			addChild(new StringTree(this, path, true));
			return;
		}
		// we will follow the edge to 'child'.
		if (!path.startsWith(child._edgeLabelFromParent)) {
			// must split edge label somewhere!
			int split_at;
			if (child._edgeLabelFromParent.startsWith(path)) {
				// path matches, but is too short
				split_at = path.length();
			} else {
				split_at = 1;
				while (path.charAt(split_at) == child._edgeLabelFromParent.charAt(split_at)) split_at++;
			}
			child.splitLabel(split_at);
		}
		child.add(path.substring(child._edgeLabelFromParent.length()));
	}
	
	private void splitLabel(int depth) {
		StringTree new_child = new StringTree(this, _edgeLabelFromParent.substring(depth), _isEndPoint);
		_isEndPoint = false;
		_edgeLabelFromParent = _edgeLabelFromParent.substring(0,  depth);
		new_child._children = _children;
		_children = null;
		addChild(new_child);
	}
	
	private void addChild(StringTree T) { 
		if (_children == null) _children = new ArrayList<StringTree>(); else _isSorted = false;
		_children.add(T);
	}
	
	private void sortChildren() {
		if (!_isSorted) {
			Collections.sort(_children, new Comparator<StringTree>() {
				@Override
				public int compare(StringTree T1, StringTree T2) {
					return T1._edgeLabelFromParent.compareTo(T2._edgeLabelFromParent);
				}
			});
		}
		_isSorted = true;
	}
	
	
	/* Syntax:
	 * - output '\t' if this is an internal node AND an endpoint. Then,
	 * - for each child, in alphabetical order of their edge label:
	 *   - edge label to that child terminated by new line
	 *   - recursively output subtree for that child (depth first)
	 * - empty edge label to signal that there are no further children (i.e., double newline)
	 * 
	 * Note that this method avoids specifying the edge label for the root note, which is undefined
	 */
	private void pack(StringBuilder sb) {
		if (_children!=null) {
			if (_isEndPoint) sb.append('\t');
			sortChildren();
			for (StringTree child : _children) {
				sb.append(child._edgeLabelFromParent);
				sb.append('\n');
				child.pack(sb);
			}
		}
		sb.append('\n');
	}
	
	private int unpack(String packed, int ix) {
		assert ix < packed.length() : "Attempt to unpack empty string";
		// first check if this is an internal endpoint, signalled by '\t'.
		if (packed.charAt(ix) == '\t') { _isEndPoint = true; ix++; }
		while (true) {
			int nl = packed.indexOf('\n', ix);
			assert nl!=-1 : "Edge labels in packed StringTree representation should be NL terminated.";
			if (nl==ix) break; // this newline signals last child for this node
			StringTree child = new StringTree(this, packed.substring(ix, nl), false);
			ix = child.unpack(packed, nl+1);
			addChild(child);
		}
		if (_children==null) _isEndPoint = true; // leaves are endpoints
		return ix+1;
	}
	
	public String getPacked() {
		StringBuilder sb = new StringBuilder();
		pack(sb);
		return sb.toString();
	}
	
	private void getSetRec(Set<String> S, String prefix) {
		if (_isEndPoint) S.add(prefix);
		if (_children!=null) {
			for (StringTree child : _children) child.getSetRec(S, prefix + child._edgeLabelFromParent);
		}
	}
	
	public Set<String> getSet() {
		Set<String> S = new HashSet<String>();
		getSetRec(S, "");
		return S;
	}
	
	public static String set2packed(Collection<?> items) {
		return new StringTree(items).getPacked();
	}
	
	public static Set<String> packed2set(String packed) {
		return new StringTree(packed).getSet();
		
	}
	
}

