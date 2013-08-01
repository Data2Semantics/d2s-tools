package org.data2semantics.modules;


import java.util.List;

import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="SumList")
public class SumListModule {
	
	List<Integer> list;
	
	SumListModule(@In(name="list") List<Integer> list){
		this.list = list;
	}
	
	@Main
	public Integer getSum() {
		int result = 0;
		for(int i : list) result += i;

		return result;
	}
}
