package org.data2semantics.proppred.libsvm;
import de.bwaldvogel.liblinear.Model;


public class LibLINEARModel {
	private Model model;
	
	LibLINEARModel(Model model) {
		this.model = model;
	}
	
	Model getModel() {
		return model;
	}
	
}
