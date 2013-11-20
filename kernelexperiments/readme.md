# Kernel Experiments

NOTE (9-10-2013): A bug has been fixed in the F1 computation, which results in higher F1 scores, however the ordering between scores has not changed.
Originally the F1 was erroneously computed as: TP / (TP + FN + FP), now it is correctly computed as: 2*TP / ((2*TP) + FN + FP).

This project contains classes that run experiments with the kernels and classfiers in the proppred project.

- `exp` contains different experiments, all of them with a main method to execute them.
- `exp.utils` contains utility classes to create experiments, such as single experiments and result objects.
- `exp.ecml2013` contains the experiments for the ECML/PKDD 2013 paper: de Vries, G.K.D. "A Fast Approximation of the Weisfeiler-Lehman Graph Kernel for RDF Data".
- `exp.dmold` contains experiments for the submission to the Data-Mining on Linked Data Workshop.
- `exp.old` contains old code for experiments, which will be removed at some point.


### Acknowledgments
This software was supported by the Dutch national program COMMIT. 


