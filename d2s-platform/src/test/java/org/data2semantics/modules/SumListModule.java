package org.data2semantics.modules;


import java.util.List;

import org.data2semantics.platform.annotation.InputParameter;
import org.data2semantics.platform.annotation.MainMethod;
import org.data2semantics.platform.annotation.Module;

@Module(name="SumList")
public class SumListModule {
	
	@MainMethod
	public Integer getList(@InputParameter(name="list") List<Integer> list) {
		int result = 0;
		for(int i : list) result += i;

		return result;
	}
}
