**bomba** - a prototype Answer Set Programming Scala DSL
=====================================================================

Bomba is an [Answer Set Programming](https://en.wikipedia.org/wiki/Answer_set_programming) dialect in the form of a Scala DSL. It is intended, currently, as a prototype to experiment with possible syntax and semantic variants, as well as language integration.

Usage
----------

 1. 
   - If your build system is m2-compatible:
     1. Add the Sonatype-OSS repo (full release coming soon) :
       - SBT:
	    ```scala
	    resolvers += "sonatype sapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
	    ``` 
        - Maven:
	     ```xml
	    <repository>
		    <id>oss.sonatype.org</id>
		    <name>sonatype sapshots</name>
		    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
	    </repository>
		```
     1. Add to your dependencies:
	      - SBT: 
	     ```scala
	     libraryDependencies += org.bomba-lang % bomba % 0.3.0-SNAPSHOT
	     ``` 
	      - Maven:
	     ```xml
		<dependency>
			<groupId>org.bomba-lang</groupId>
			<artifactId>bomba</artifactId>
			<version>0.3.0-SNAPSHOT</version>
		</dependency>	     
		```
   - Otherwise:
     1. `mvn clean package`
 	 1. Add the resulting JAR to the build path of your project.
 1. Add:
      ```scala

      import org.bomba_lang.proto._
      ``` 
    to your Scala import statements.
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
hP1 v ... v hPl :- bP1 & ... & bPm & ~nbP1 & ... & ~nbPn
hP1 ∨ ... ∨ hPl ⟵  bP1 ∧ ... ∧ bPm ∧ ~nbP1 ∧ ... ∧ ~nbPn
```

where `hP1,...,hPl, bP1,...,bPm, nbP1,...,nbPn` are literals. `~` means "default negation".

A special subtype of rules are constraints - rules with an empty head, i.e.:

```scala
Nil :- bP1 & ... & bPm & ~nbP1 & ... & ~nbPn
⊥ ⟵  bP1 ∧ ... ∧ bPm ∧ ~nbP1 ∧ ... ∧ ~nbPn
```

Their intuitive meaning is: "this should never happen"/"if this rule fires, then the answer is invalid".

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
 - the names of the literals "shadow" any nameds (`val`s, `def`s, etc.) in the program's local scope. An 
 exception is `Nil` for constraints (see above).

*Note: yes, this means you can use Unicode symbols for logical operators in your program!*

Semantics
------------
Semantics depend on the solver used. The current reference implementation, `org.bomba_lang.proto.NaiveSolver`, uses standard
disjunctive semantics, with the Ferrari and Lifschitz resolution variant for strong negation. To generate all answer sets, use:

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

Here's the source from the main class of the [demo project](https://github.com/mikkoz/bomba-demo), to give you an idea of the syntax and semantics:
```scala
package org.bomba_lang.bomba_demo

import org.bomba_lang.proto._

object Demo {

  def main(args: Array[String]): Unit = {

    @bomba
    val prog1 = { z(1, 2) }

    println(prog1)

    @bomba
    val prog2 = {
      y v z
    }

    println(prog2.solve)

    @bomba
    val prog3 = {
      rain :- wet
      wet
    }

    println(prog3.solve)

    println("canonical program extension")
    @bomba
    val r0 = {
      rain :- wet
    }
    println(r0.solve)
    @bomba
    val r1 = {
      wet
    }
    println(r0.solve(r1))
    println("-------------------")

    println("canonical program extension with default negation")
    @bomba
    val rn0 = {
      rain :- wet & ~sprinkler
    }
    println(r0.solve)
    @bomba
    val rn1 = {
      wet
    }
    println(rn0.solve(rn1))
    @bomba
    val rn2 = {
      wet
      sprinkler
    }
    println(rn0.solve(rn2))
    println("-------------------")

    println("variables")
    @bomba
    val varProg = {
      x(1)
      y(2)
      z('X, 'Y) :- x('X) & y('Y)
    }
    println("Ungrounded: " + varProg)

    val varProgGround = new GroundProgram(varProg)
    println("Grounded: " + varProgGround)
    println("Result: " + varProgGround.solve)

    println("-------------------")
    println("Constraints")
    @bomba
    val constProg = {
      x
      Nil :- x
    }
    println(constProg)
    println(constProg.solve) //empty set (*no* answer sets)
    
    println("-------------------")
    println("\"Formal\" notation")
    @bomba
    val formalProg = {
      a ∨ b ⟵ x ∧ y ∧ z
      ⊥ ⟵ z ∧ d
    }
    println(formalProg)
  }

}
```

Comments? Issues?
------------
All welcome.