# APRO
### Java Affinity Propagation Library // Parallelized
v1.0.6  
http://www.apro.u-psud.fr


Apro is a Java implementation of Affinity Propagation clustering algorithm. It is parallelized for easy and efficient use on multicore processors and NUMA architectures (using libnuma native library). Besides the basic (parallelized) algorithm, it also implements Hierarchical Affinity Propagation (HiAP), making it usable in practice even on huge datasets. Apro comes with a couple of ready to use datasource providers (for Delimiter Separated Values, MATLAB files, etc.). 

## 1. About Affinity Propagation

Affinity Propagation (AP) is a clustering algorithm based on the concept of "message passing" between data points. Unlike clustering algorithms such as k-means or k-medoids, AP does not require the number of clusters to be determined or estimated before running the algorithm. AP finds "exemplars", members of the input set that are representative of clusters.

More about Affinity Propagation:
- [Frey Lab Affinity Propagation Site](http://www.psi.toronto.edu/index.php?q=affinity%20propagation) - The original Affinity Propagation, FAQ, Datasets and more
- "Clustering by Passing Messages Between Data Points", Brendan J. Frey and Delbert Dueck, University of Toronto, Science 315, 972-976, February 2007 [PDF](http://www.psi.toronto.edu/affinitypropagation/FreyDueckScience07.pdf) [BibTeX](http://www.psi.toronto.edu/affinitypropagation/ap-science2007.bib)
- [Wikipedia Affinity Propagation Page](http://en.wikipedia.org/wiki/Affinity_propagation)

#### Hierarchical Affinity Propagation

Hierarchical AP (HiAP) solves large-scale clustering problem. It uses AP and Weighted AP (WAP) in the Divide-and-Conquer schema. It partitions the dataset, runs AP on each subset, and applies WAP to the collection of exemplars constructed from each subset. HiAP was shown to significantly decrease the computational cost (from quadratic to quasi-linear), with minimal distortion.

WAP integrates the neighboring points together and keeps spatial structure between them and other points. It makes sure that the clustering results of WAP on integrated points are equal to that of AP on non-integrated points.

- Scaling analysis of affinity propagation. Cyril Furtlehner, Michèle Sebag, and Xiangliang Zhang. Phys. Rev. E 81, 066102 – Published 1 June 2010
- Data Stream Clustering with Affinity Propagation. Xiangliang Zhang, Cyril Furtlehner, Cécile Germain-Renaud, Michèle Sebag. IEEE Transaction on Knowledge and Data Engineering


## 2. Apro Java Library

Apro is a Java implementation of Affinity Propagation clustering algorithm. It is efficiently parallelized for use on multicore processors and NUMA architectures (using `libnuma` native library), offering a simple API for easy use in your projects.

#### Main features of Apro

- Basic parallelized version of AP (`package fr.lri.tao.apro.ap`)
- Hierarchical Affinity Propagation (`package fr.lri.tao.apro.hiap`)
- Data providers for different input formats (`package fr.lri.tao.apro.data`)
- Builder classes for easy setting of running and input parameters (classes `AproBuilder` and `HiAPBuilder`). Comprehensive API of these classes allows you to set parallelization and NUMA parameters yourself, or let the builder set the parameters automatically. Builder's `.build()` method provides you a ready-to-use Apro class.

#### Used libraries

- [Colt](https://dst.lbl.gov/ACSSoftware/colt/) library for handling matrices
- [JMatIO](http://sourceforge.net/projects/jmatio/) library for reading MATLAB files (slightly modified)
- [Java Native Access (JNA)](https://github.com/twall/jna) library for calling native libnuma functions


## 3. Getting Started

Here are step-by-step instructions how to run your first Apro example.

1. Clone this project, and include `Apro.jar` and other required (and provided) `.jars` into your project.

2. Download a sample dataset. You can start with [FaceVideo dataset](http://www.psi.toronto.edu/affinitypropagation/vsh/FaceVideo.zip) (9.5 MB, .mat format). More datasets are available [here](http://www.psi.toronto.edu/affinitypropagation/vsh/).

3. Let's begin coding. First, we use `MATLABProvider` to load the similarity matrix and preferences from the MATLAB file. This particular example contains the structure with similarity matrix named S, but with zero main diagonal (preferences). Preferences should be set to the value that is contained separately in the structure, named `pref`:

```java
File faceVideoFile = new File("/path/to/FaceVideo.mat");
MATLABProvider provider = new MATLABProvider(faceVideoFile, "S", "pref");
```

4. Now that we have the input data, we need to create the object that will perform the Affinity Propagation algorithm. We can either construct an object of class `fr.lri.tao.apro.ap.Apro` directly, using its constructors, or use `AproBuilder` class from the same package as a helper class:

```java
AproBuilder builder = new AproBuilder();      
Apro apro = builder.build(provider);
```

  By default, `AproBuilder` will automatically set the number of threads to the number of available processors, and will not use NUMA unless you specify so. You can find more advanced examples in the next section.

5. Now we execute the desired number of iterations:

```java
apro.run(100);
```

6. Finally, we get the clustering results from the `Apro` object:

```java
int myExemplar = apro.getExemplar(42);
```

  Method int `Apro.getExemplar(int node)` returns the 'representative' node (exemplar) of the node specified as a parameter. Nodes having the same exemplar are considered to be in the same cluster. You can also get the whole set of clustering results using methods `Apro.getExemplars()`, which returns the array of exemplars for all nodes, or `Apro.getExemplarSet()` to get the `Set` of exemplars. These methods are actually members of `Apro`'s superclass `AbstractApro`, which provides some common code that can be used for other AP implementations if needed.

## 4. More Examples

## 5. Contact

Lovro Ilijašić  
lovro@lri.fr  
http://lovroforsale.com

## 6. Acknowledgements

Apro library was implemented by Apprentissage & Optimisation Team ([TAO](http://tao.lri.fr/)) at the Laboratoire de Recherche en Informatique ([LRI](http://www.lri.fr/)) at Paris, Saclay (France).

This work has been partially funded by the Région Île-de-France in the frame of the French cooperative project TIMCO, Pôle de Compétitivité Systematic (FUI 13).

For any information, comment or error report, you can contact [Lovro Ilijašić](lovro@lri.fr), who implemented this library in collaboration with Cécile Germain, Xiangliang Zhang and Joël Falcou.
