package org.data2semantics.platform.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.jexl2.parser.JexlNode.Literal;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.util.Functions;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;


/**
 * Class to report the provenance of a workflow in PROV RDF
 * 
 * 
 * @author Gerben
 *
 */
public class PROVReporter implements Reporter {
	private static final String NAMESPACE = "http://www.data2semantics.org/d2s-platform/";
	private static final String PROV_NAMESPACE =  "http://www.w3.org/ns/prov#";
	
	private static final String PROV_FILE = "prov-o.ttl"; 
	
	
	
	private Workflow workflow;
	private File root;

	
	public PROVReporter(Workflow workflow, File root) {
		super();
		this.workflow = workflow;
		this.root = root;
		root.mkdirs();
	}

	@Override
	public void report() throws IOException {
		writePROV();
	}

	@Override
	public Workflow workflow() {
		return workflow;
	}
	
	private void writePROV() throws IOException {
		ValueFactory factory = ValueFactoryImpl.getInstance();		
		Model stmts = new LinkedHashModel();
		
		URI eURI = factory.createURI(PROV_NAMESPACE, "Entity");
		URI acURI = factory.createURI(PROV_NAMESPACE, "Activity");
		URI usedURI = factory.createURI(PROV_NAMESPACE, "used");
		URI wgbURI  = factory.createURI(PROV_NAMESPACE, "wasGeneratedBy");
		
		URI valueURI = factory.createURI(NAMESPACE, "value");
		
		/* agent uri's
		URI agURI = factory.createURI(PROV_NAMESPACE, "Agent");
		URI watURI  = factory.createURI(PROV_NAMESPACE, "wasAttributedTo");
		URI wawURI  = factory.createURI(PROV_NAMESPACE, "wasAssociatedWith");
		*/	
		
		for (Module module : workflow.modules()) {
			//URI moduleURI = factory.createURI(NAMESPACE, module.name());
			//stmts.add(factory.createStatement(moduleURI, RDF.TYPE, agURI));
			
			for (ModuleInstance mi : module.instances()) {
				URI miURI = factory.createURI(NAMESPACE + "module/", module.name() + mi.moduleID());
				stmts.add(factory.createStatement(miURI, RDF.TYPE, acURI)); // Activity
				//stmts.add(factory.createStatement(miURI, wawURI, moduleURI)); // wasAssociatedWith
				
				for (InstanceOutput io : mi.outputs()) {
					URI ioURI = factory.createURI(NAMESPACE + "module/", module.name() + mi.moduleID() + "/output/" + io.name());
					stmts.add(factory.createStatement(ioURI, RDF.TYPE, eURI)); // entity
					stmts.add(factory.createStatement(ioURI, wgbURI, miURI)); // wasGeneratedBy
					//stmts.add(factory.createStatement(ioURI, watURI, moduleURI)); // wasAttributedTo
					
					// If we can create a literal
					if (Literals.canCreateLiteral(io.value())) {
						stmts.add(factory.createStatement(ioURI, valueURI, Literals.createLiteral(factory, io.value())));
						stmts.add(factory.createStatement(ioURI, RDFS.LABEL, Literals.createLiteral(factory, io)));
					}
				}
				
				for (InstanceInput ii : mi.inputs()) {
					URI iiURI = null;
					
					if (ii.instanceOutput() != null) {
						iiURI = factory.createURI(NAMESPACE + "module/", ii.instanceOutput().module().name() 
								+ ii.instanceOutput().instance().moduleID() + "/output/" + ii.name());
					} else {
						iiURI = factory.createURI(NAMESPACE + "module/", module.name() + mi.moduleID()
								+ "/input/" + ii.name());
						
						// If we can create a literal
						if (Literals.canCreateLiteral(ii.value())) {
							stmts.add(factory.createStatement(iiURI, valueURI, Literals.createLiteral(factory, ii.value())));
							stmts.add(factory.createStatement(iiURI, RDFS.LABEL, Literals.createLiteral(factory, ii)));
						}			
					}
							
					stmts.add(factory.createStatement(iiURI, RDF.TYPE, eURI)); // entity
					stmts.add(factory.createStatement(miURI, usedURI, iiURI)); // used					
				}
			}
		}
		
		File file = new File(root, PROV_FILE);
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileWriter(file));
		
		try {
			writer.startRDF();
			for (Statement stmt : stmts) {
				writer.handleStatement(stmt);
			}
			writer.endRDF();
			
		} catch (RDFHandlerException e) {
			throw new RuntimeException(e);
		}
	}

}
