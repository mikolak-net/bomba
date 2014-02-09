**bomba** - a prototype Answer Set Programming Scala DSL
=====================================================================

Bomba is an [Answer Set Programming](https://en.wikipedia.org/wiki/Answer_set_programming) dialect in the form of a Scala DSL. It is intended, currently, as a prototype to experiment with possible syntax and semantic variants, as well as language integration.

Usage
----------

 1. 
   - If your build system is m2-compatible:
     1. `mvn clean install`
     1. Add org.bomba_lang:bomba_lang to your dependencies.
   - Otherwise:
     1. `mvn clean package`
 	   1. Add the resulting JAR to the build path of your project.
 1. Add: `import org.bomba_lang.proto._` to your Scala import statements.
 1. Add the `macro-paradise` compiler plugin to your Maven/SBT build - as described in [the macro documentation](http://docs.scala-lang.org/overviews/macros/paradise.html). 


Syntax
-----------
The syntax is similar to other Answer Set Programming dialects.

A **literal** is of the form

```scala
name(x1,...,xn)
-name(x1,...,xn)
```
    
where `n >= 0` (if `n=0`, the parenthesis are optional), and `x1,...,xn` are of the type `Any`. `-` means "strong negation".

An `xm` (`1 <= m <= n`) of type Symbol (e.g. 'X ), is a **variable**.

A **rule** is of the form

```scala
hP1 v ... v hPl :- (bP1,...,bPm,~nbP1,...,~nbPn)
hP1 ∨ ... ∨ hPl ⟵  bP1 ∧ ... ∧ bPm ∧ ~nbP1 ∧ ... ∧ ~nbPn
```

where `hP1,...,hPl, bP1,...,bPm, nbP1,...,nbPn` are literals. `~` means "default negation".

A **program** is a Scala code block of the form

```scala
@bomba
val progName = {
	r1
	...
	rn
}
```
  
where `r1,...,rn` are rules. Note that:
 - the rules are treated as standard Scala expressions, i.e. they must be either in 
separate lines, or separated with `;` when on the same line.
 - the names of the literals "shadow" any same-named `val`s and/or `def`s in the program's local scope.

*Note: yes, this means you can use Unicode symbols for logical operators in your program!*

Semantics
------------
Semantics depend on the solver used. The current reference implementation, `org.bomba_lang.proto.NaiveSolver`, uses standard
disjunctive semantics, with the Ferraris and Lifschitz resolution variant for strong negation. To generate all answer sets, use:

```scala
program.solve
```
	
The idiomatic way of interfacing your logic programs with the rest of your code is to define your "oracle" program, and then inject
the data you wish to provide in the solve method, e.g.:

```scala
@bomba
val oracle = {...}
@bomba
val data = {...}
oracle.solve(data)
```
	
Examples
-----------

The [demo project](https://github.com/mikkoz/bomba-demo) contains a number of simple examples.

Comments? Issues?
------------
All welcome.