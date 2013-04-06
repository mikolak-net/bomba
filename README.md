**schodoLog** *[s-HOD-o-LOG]* - a prototype Answer Set Programming Scala DSL
=====================================================================

SchodoLog is an [Answer Set Programming](https://en.wikipedia.org/wiki/Answer_set_programming) dialect in the form of a Scala DSL. It is intended, currently, as a prototype to experiment with possible syntax and semantic variants, as well as language integration.

Usage
----------

 1. 
   - If your build system is m2-compatible:
     1. `mvn clean install`
     1. Add org.schodoLog:schodoLog to your dependencies.
   - Otherwise:
     1. `mvn clean package`
 	   1. Add the resulting JAR to the build path of your project.
 1. Add: `import org.schodoLog.proto._` to your Scala import statements.


Syntax
-----------
The syntax is similar to other Answer Set Programming dialects.

A **literal** is of the form

    p"name"(x1,...,xn)
    -p"name"(x1,...,xn)
    
where `n >= 0` (if `n=0`, the parenthesis are optional), and `x1,...,xn` are of the type `Any`. `-` means "strong negation".

Currently, all literals are ground. 

A **rule** is of the form

     hP1 v ... v hPl :- (bP1,...,bPm,~nbP1,...,~nbPn)
     hP1 ∨ ... ∨ hPl ⟵  bP1 ∧ ... ∧ bPm ∧ ~nbP1 ∧ ... ∧ ~nbPn

where `hP1,...,hPl, bP1,...,bPm, nbP1,...,nbPn` are literals. `~` means "default negation".

A **program** is of the form

	Program(r1,...,rn)
  
where `r1,...,rn` are rules.

*Note: yes, this means you can use Unicode symbols for logical operators in your program!*

Semantics
------------
Semantics depend on the solver used. The current reference implementation, `org.schodoLog.proto.NaiveSolver`, uses standard
disjunctive ground semantics, with the Ferraris and Lifschitz resolution variant for strong negation. To generate all answer sets, use:

	NaiveSolver(Program(...))
	
	
Examples
-----------

    new NaiveSolver().solve(new Program(
   	 p"y" v p"x"
    ))                                             //>Set(Set(y), Set(x))
   
   
    new NaiveSolver().solve(Program(
   	 p"rain" :- p"wet",
   	 p"wet"
    ))                                             //>Set(Set(wet, rain))


Comments? Issues?
------------
All welcome.