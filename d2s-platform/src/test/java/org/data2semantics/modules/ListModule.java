package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="ListModule")
public class ListModule {
	
	@Main
	@Factory
	public List<Integer> getList(@In(name="nOutput") int nOutput) {
		List<Integer> result = new ArrayList<Integer>();
		for(int i=1;i<=nOutput;i++)
			result.add(i);
		
		return result;
	}
}
