package org.schodoLog.playground

import org.schodoLog.proto._

/**
 Some examples
*/
object playground {
    new NaiveSolver().solve(new Program(
   	p"y" v p"x"
   ))                                             //> res0: scala.collection.immutable.Set[scala.collection.immutable.Set[org.scho
                                                  //| doLog.proto.Literal]] = Set(Set(y), Set(x))
   
   
   new NaiveSolver().solve(Program(
   	p"rain" :- p"wet",
   	p"wet"
   ))                                             //> res1: scala.collection.immutable.Set[scala.collection.immutable.Set[org.scho
                                                  //| doLog.proto.Literal]] = Set(Set(wet, rain))
 
 
   :-(p"x")                                       //> res2: org.schodoLog.proto.Rule =  ⟵  x.
 
 
 
 

	new Program(p"x",p"p"("x","y") ∨ p"g"("z"))
                                                  //> res3: org.schodoLog.proto.Program = Program(x. p(x,y) ∨ g(z).)

	p"x" :- p"y"                              //> res4: org.schodoLog.proto.Rule = x ⟵  y.

	val prog = new Program(
	(p"p"("x","y") v p"g"("z")) :- (p"b"("z"),p"x"("y"))
	  
	)                                         //> prog  : org.schodoLog.proto.Program = Program(p(x,y) ∨ g(z) ⟵  b(z) ∧ 
                                                  //| x(y).)
	
	val r1 =  p"p"("x","y") v p"g"("z") :- (~p"b"("z"),p"x"("y"))
                                                  //> r1  : org.schodoLog.proto.Rule = g(z) ∨ p(x,y) ⟵  x(y) ∧ not b(z).
		
  val r2 = p"p"("x","y") ∨ p"g"("z") :- ~p"b"("z") ∧ p"x"("y")
                                                  //> r2  : org.schodoLog.proto.Rule = p(x,y) ∨ g(z) ⟵  x(y) ∧ not b(z).
  r1 == r2                                        //> res5: Boolean = true
   
 
   
   
    
   val i = Set(p"x"("y"),p"b"("z"))               //> i  : scala.collection.immutable.Set[org.schodoLog.proto.Literal] = Set(x(y),
                                                  //|  b(z))
   
   prog.modelOf(i)                                //> res6: Boolean = false
   
    
    
   val ps = new Program(
   	Rule(Set(p"x"),Set(),Set()),
		p"y" :- p"x"
   )                                              //> ps  : org.schodoLog.proto.Program = Program(x. y ⟵  x.)
   
   new NaiveSolver().solve(ps)                    //> res7: scala.collection.immutable.Set[scala.collection.immutable.Set[org.scho
                                                  //| doLog.proto.Literal]] = Set(Set(x, y))
   
   val ps1 = new Program(
   	p"y" v p"x"
   )                                              //> ps1  : org.schodoLog.proto.Program = Program(y ∨ x.)
    
   new NaiveSolver().solve(new Program(
   	p"y" v p"x"
   ))                                             //> res8: scala.collection.immutable.Set[scala.collection.immutable.Set[org.scho
                                                  //| doLog.proto.Literal]] = Set(Set(y), Set(x))
   
   
   new NaiveSolver().solve(Program(
   	p"rain" :- p"wet",
   	p"wet"
   ))                                             //> res9: scala.collection.immutable.Set[scala.collection.immutable.Set[org.scho
                                                  //| doLog.proto.Literal]] = Set(Set(wet, rain))
   new NaiveSolver().solve(Program(
   	p"rain"(Set("a")) :- p"wet",
   	p"wet"
   ))                                             //> res10: scala.collection.immutable.Set[scala.collection.immutable.Set[org.sc
                                                  //| hodoLog.proto.Literal]] = Set(Set(wet, rain(Set(a))))
   
}