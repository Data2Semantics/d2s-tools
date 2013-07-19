d2s-tools
=========

This project provides tools for dealing with RDF data in a Machine Learning/Complexity Analysis context.
See also http://www.data2semantics.org/publications/ecmlpkdd-2013/.

## Running experiments for the DMoLD 2013 Submission

1. Download the d2s-tools, proppred and kernelexperiments source code from this GitHub. All of them are Maven projects. The kernelexperiments project depends on d2s-tools and proppred.
2. The affiliation prediction dataset is contained in the kernelexperiments project. 
3. The BGS dataset is available online at data.bgs.gov.uk. It does require some cleaning, since the files are not nicely saved with escapes for some double quotes. A cleaned version can be found here: https://www.dropbox.com/sh/k5wwq10n3cpkjfp/GGnti2YX-z
4. The data-mining challenge datasets are available from http://keg.vse.cz/dmold2013/data-description.html
5. The experiments and classifiers are in the exp.dmold package in kernelexperiments, some of the lines for the location of the data need to be changed.
6. Make sure the virtual machine has plenty of heap space.


## Running experiments for the ECMLPKDD 2013 Paper

1. Download the d2s-tools, proppred and kernelexperiments source code from this GitHub. All of them are Maven projects. The kernelexperiments project depends on d2s-tools and proppred.
2. The affiliation prediction dataset is contained in the kernelexperiments project. The BGS dataset is available online at data.bgs.gov.uk. It does require some cleaning, since the files are not nicely saved with escapes for some double quotes. A cleaned version can be found here: https://www.dropbox.com/sh/k5wwq10n3cpkjfp/GGnti2YX-z
3. The experiments can be run by running AffiliationCompareExperiment and GeoCompareExperiment, change the line for the geo experiments data "C:\\Users\\Gerben\\Dropbox\\D2S\\data_bgs_ac_uk_ALL" to your location of the data.
4. Make sure the virtual machine has plenty of heap space.



