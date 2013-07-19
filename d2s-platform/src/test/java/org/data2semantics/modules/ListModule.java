package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;

@Module(name="ListModule")
public class ListModule {
	
	@MainMethod
	public List<Integer> getList(@InputParameter(name="nOutput") int nOutput) {
		List<Integer> result = new ArrayList<Integer>();
		for(int i=1;i<=nOutput;i++)
			result.add(i);
		
		return result;
	}
}
