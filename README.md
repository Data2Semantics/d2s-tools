d2s-tools
=========

This project provides tools for dealing with RDF data in a Machine Learning/Complexity Analysis context.

## Running experiments for the ECMLPKDD 2013 Submission

1. Download the d2s-tools, proppred and kernelexperiments source code from this GitHub. All of them are Maven projects. The kernelexperiments project depends on d2s-tools and proppred.
2. The affiliation prediction dataset is contained in the kernelexperiments project. The BGS dataset is available online at data.bgs.gov.uk. It does require some cleaning, since the files are not nicely saved with escapes for some double quotes. A cleaned version can be found here: https://www.dropbox.com/sh/k5wwq10n3cpkjfp/GGnti2YX-z
3. The experiments can be run by running AffiliationCompareExperiment and GeoCompareExperiment, change the line for the geo experiments data "C:\\Users\\Gerben\\Dropbox\\D2S\\data_bgs_ac_uk_ALL" to your location of the data.
4. Make sure the virtual machine has plenty of heap space.


## Running graph kernel experiments for the KDD 2013 Submission

1. Download the d2s-tools, proppred and kernelexperiments source code, all are maven projects, note that the kernelexperiments depends on d2s-tools and proppred
2. The datasets are in the datasets folder of the kernelexperiments project
3. The four experiment files are in kernelexperiments/src/main/java/org/data2semantics/exp/, change the path in the code of the four experiments (...Experiment.java) to the location of the data files.
4. Compile and run.
5. Make sure the virtual machine has several gigabytes of heap space.

