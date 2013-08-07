package org.data2semantics.modules;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.platform.annotation.Factory;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;

@Module(name="ListModule", description="Outputs a list of numbers of a given length.")
public class ListModule {
	
	int nOutput;
	public ListModule(int nOutput) {
		this.nOutput = nOutput;
	}

	@Main
	public List<Integer> getList() 
	{
		List<Integer> result = new ArrayList<Integer>();
		for(int i=1;i<=nOutput;i++)
			result.add(i);
		
		return result;
	}
	
	@Factory
	public static ListModule getModule(
			@In(name="nOutput", description="The length of the list to output.") 
				int nOutput
		)
	{
		return new ListModule(nOutput);
	}
	
}
