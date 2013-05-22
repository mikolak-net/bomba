package org.schodoLog.playground

import org.schodoLog.proto._

/**
 Some examples
*/
object playground {
    new NaiveSolver().solve(new Program(
   	p"y" v p"x"
   ))                                             //> res0: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(y), Set(x))
   
   
   new NaiveSolver().solve(new Program(
   	p"rain" :- p"wet",
   	p"wet"
   ))                                             //> res1: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(wet, rain))
 
 
   :-(p"x")                                       //> res2: org.schodoLog.proto.Rule =  ⟵  x.
 
 
 
 

	new Program(p"x",p"p"("x","y") ∨ p"g"("z"))
                                                  //> res3: org.schodoLog.proto.Program = Program(x. p(x,y) ∨ g(z).)

	p"x" :- p"y"                              //> res4: org.schodoLog.proto.Rule = x ⟵  y.

	val prog = new Program(
	(p"p"("x","y") v p"g"("z")) :- (p"b"("z"),p"x"("y"))
	  
	)                                         //> prog  : org.schodoLog.proto.Program = Program(p(x,y) ∨ g(z) ⟵  b(z) ∧ 
                                                  //| x(y).)
  prog.rules                                      //> res5: org.schodoLog.proto.Rule* = WrappedArray(p(x,y) ∨ g(z) ⟵  b(z) ∧
                                                  //|  x(y).)
	
	val r1 =  p"p"("x","y") v p"g"("z") :- (~p"b"("z"),p"x"("y"))
                                                  //> r1  : org.schodoLog.proto.Rule = g(z) ∨ p(x,y) ⟵  x(y) ∧ not b(z).
		
  val r2 = p"p"("x","y") ∨ p"g"("z") :- ~p"b"("z") ∧ p"x"("y")
                                                  //> r2  : org.schodoLog.proto.Rule = p(x,y) ∨ g(z) ⟵  x(y) ∧ not b(z).
  r1 == r2                                        //> res6: Boolean = true
   
 
   
   
    
   val i = Set(p"x"("y"),p"b"("z"))               //> i  : scala.collection.immutable.Set[org.schodoLog.proto.Literal] = Set(x(y),
                                                  //|  b(z))
   
   prog.modelOf(i)                                //> res7: Boolean = false
   
    
    
   val ps = new Program(
   	Rule(Set(p"x"),Set(),Set()),
		p"y" :- p"x"
   )                                              //> ps  : org.schodoLog.proto.Program = Program(x. y ⟵  x.)
   
   new NaiveSolver().solve(ps)                    //> res8: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(x, y))
   
   val ps1 = new Program(
   	p"y" v p"x"
   )                                              //> ps1  : org.schodoLog.proto.Program = Program(y ∨ x.)
    
   new NaiveSolver().solve(new Program(
   	p"y" v p"x"
   ))                                             //> res9: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(y), Set(x))
   
   
   new NaiveSolver().solve(new Program(
   	p"rain" :- p"wet",
   	p"wet"
   ))                                             //> res10: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(wet, rain))
   new NaiveSolver().solve(new Program(
   	p"rain"(Set("a")) :- p"wet",
   	p"wet"
   ))                                             //> res11: Set[Set[org.schodoLog.proto.Literal]] = Set(Set(wet, rain(Set(a))))
       
  //variables
	val varProg = new Program(
   	p"x"(1),
   	p"y"(2),
   	p"z"('X,'Y) :- (p"x"('X),p"y"('Y))
   )                                              //> varProg  : org.schodoLog.proto.Program = Program(x(1). y(2). z('X,'Y) ⟵  
                                                  //| x('X) ∧ y('Y).)
	
	val varProgGround = new GroundProgram(varProg)
                                                  //> varProgGround  : org.schodoLog.proto.GroundProgram = Program(x(1). y(2). z(
                                                  //| 1,1) ⟵  x(1) ∧ y(1). z(1,2) ⟵  x(1) ∧ y(2). z(2,1) ⟵  x(2) ∧ y(
                                                  //| 1). z(2,2) ⟵  x(2) ∧ y(2).)
  new NaiveSolver().solve(varProgGround)          //> res12: scala.collection.immutable.Set[scala.collection.immutable.Set[org.sc
                                                  //| hodoLog.proto.Literal]] = Set(Set(z(1,2), y(2), x(1)))
}