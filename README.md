VeriWeb
=======

VeriWeb is a web-based IDE for verification that decomposes the task
of writing verifiable specifications into manageable subproblems.

VeriWeb users write [Java Modeling Language
(JML)](http://www.eecs.ucf.edu/~leavens/JML//index.shtml)
specifications which are verified using the [Extended Static Checker
for Java version 2
(ESC/Java2)](http://kindsoftware.com/products/opensource/ESCJava2/).

## Publications

The VeriWeb interface is described in the paper [Reducing the barriers
to writing verified
specifications](http://homes.cs.washington.edu/~mernst/pubs/veriweb-oopsla2012-abstract.html)
which was presented at the OOPSLA 2012 conference.

The study results can be found on the [project
page](http://homes.cs.washington.edu/~tws/veriweb). These will
eventually be moved to this repository.

## Notes

This repository contains the latest snapshot of the VeriWeb
development source code from our local repository. As such, the code
may differ in certain places from the description in the
publication. Known differences are:

* Mechanical Turk doesn't work in this version of the code.
* Object invariant inference and handling 

## Running VeriWeb

If you'd like to try building and running VeriWeb, please contact
me. The basic instructions are:

 1. Deploy and run the LibVeriAsa server 
 2. Deploy VeriWeb via [Apache Tomcat](https://tomcat.apache.org/)

In practice, the server requires a very specific directory structure
which I haven't gotten around to documenting yet.

### Converting programs for use with VeriWeb

Because VeriWeb uses ESC/Java2 under the hood, it only supports Java
1.4 programs. I created an [Eclipse
plug-in](https://code.google.com/a/eclipselabs.org/p/java-downconvert/)
to automatically converting Java 1.5 and 1.6 projects to Java 1.4.

## License

This software is licensed under [Matt Might's](http://matt.might.net/)
wonderful [CRAPL academic
license](http://matt.might.net/articles/crapl/).



