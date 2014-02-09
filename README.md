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


Syntax
-----------
The syntax is similar to other Answer Set Programming dialects.

A **literal** is of the form

```scala
p.name(x1,...,xn)
-p.name(x1,...,xn)
```
    
where `n >= 0` (if `n=0`, the parenthesis are optional), and `x1,...,xn` are of the type `Any`. `-` means "strong negation".

An `xm` (`1 <= m <= n`) of type Symbol (e.g. 'X ), is a **variable**.

A **rule** is of the form

```scala
hP1 v ... v hPl :- (bP1,...,bPm,~nbP1,...,~nbPn)
hP1 ∨ ... ∨ hPl ⟵  bP1 ∧ ... ∧ bPm ∧ ~nbP1 ∧ ... ∧ ~nbPn
```

where `hP1,...,hPl, bP1,...,bPm, nbP1,...,nbPn` are literals. `~` means "default negation".

A **program** is of the form

```scala
Program(r1,...,rn)
```
  
where `r1,...,rn` are rules.

*Note: yes, this means you can use Unicode symbols for logical operators in your program!*

Semantics
------------
Semantics depend on the solver used. The current reference implementation, `org.bomba_lang.proto.NaiveSolver`, uses standard
disjunctive semantics, with the Ferraris and Lifschitz resolution variant for strong negation. To generate all answer sets, use:

```scala
new Program(...).solve
```
	
The idiomatic way of interfacing your logic programs with the rest of your code is to define your "oracle" program, and then inject
the data you wish to provide in the solve method, e.g.:

```scala
val oracle = new Program(...)
oracle.solve(p.somePredicate(someData),...)
```
	
Examples
-----------

```scala
import org.bomba_lang.proto._   //imports the required implicits and the "magic" p object

new Program(
   p.y v p.x
).solve                                           //>Set(Set(y), Set(x))
   
   
new Program(
   p.rain :- p.wet,
   p.wet
).solve                                           //>Set(Set(wet, rain))

//same result as above, but with idiomatic merging
val r0 = new Program(
   p.rain :- p.wet
)                                                 //>Program(rain ⟵  wet.)
r0.solve                                          //>Set(Set())
r0.solve(p.wet)                                   //>Set(Set(wet, rain))
   

new Program(
   p.p1("a"),
   p.p1("b"),
   p.p2('X) :- p.p1('X)
).solve                                           //>Set(Set(p1(a), p1(b), p2(a), p2(b)))
```

For more examples, help yourself to the Worksheet `playground.sc` in the `org.bomba_lang.playground` package. 

Comments? Issues?
------------
All welcome.