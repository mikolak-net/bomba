package org.bomba_lang.playground

import org.bomba_lang.proto._

/**
 Some examples
*/
object playground {

		def blah(r: Rule*) = {
     val orgProg = new Program(r: _*)
     orgProg
    }                                             //> blah: (r: org.bomba_lang.proto.Rule*)org.bomba_lang.proto.Program
    
    blah(p.y,p.n)                                 //> res0: org.bomba_lang.proto.Program = Program(y. n.)

		 
		def test(l: Rule*) = l.mkString(",")
                                                  //> test: (l: org.bomba_lang.proto.Rule*)String
		
		test(p.y,p.g)                     //> res1: String = y.,g.
		 
		p.z(1,2)                          //> res2: org.bomba_lang.proto.Literal = z(1,2)
		
    new Program(
   	p.y() v p.z()
   ).solve                                        //> res3: org.bomba_lang.proto.AnswerSets = Set(Set(y), Set(z))
   
   
   new Program(
   	p.rain :- p.wet,
   	p.wet
   ).solve                                        //> res4: org.bomba_lang.proto.AnswerSets = Set(Set(wet, rain))
 
 
   :-(p.x)                                        //> res5: org.bomba_lang.proto.Rule =  ⟵  x.
 
 
	new Program(p.x,p.p("x","y") ∨ p.g("z"))  //> res6: org.bomba_lang.proto.Program = Program(x. p(x,y) ∨ g(z).)
 
	p.x :- p.y                                //> res7: org.bomba_lang.proto.Rule = x ⟵  y.

	val prog = new Program(
	(p.p("x","y") v p.g("z")) :- (p.b("z"),p.x("y"))
	  
	)                                         //> prog  : org.bomba_lang.proto.Program = Program(p(x,y) ∨ g(z) ⟵  b(z) ∧
                                                  //|  x(y).)
  prog.rules                                      //> res8: org.bomba_lang.proto.Rule* = WrappedArray(p(x,y) ∨ g(z) ⟵  b(z) �
                                                  //| � x(y).)
	
	val r1 =  p.p("x","y") v p.g("z") :- (~p.b("z"),p.x("y"))
                                                  //> r1  : org.bomba_lang.proto.Rule = g(z) ∨ p(x,y) ⟵  x(y) ∧ not b(z).
		
  val r2 = p.p("x","y") ∨ p.g("z") :- ~p.b("z") ∧ p.x("y")
                                                  //> r2  : org.bomba_lang.proto.Rule = p(x,y) ∨ g(z) ⟵  x(y) ∧ not b(z).
  r1 == r2                                        //> res9: Boolean = true
   
 
   
   
    
   val i = Set(p.x("y"),p.b("z"))                 //> i  : scala.collection.immutable.Set[org.bomba_lang.proto.Literal] = Set(x(y)
                                                  //| , b(z))
   
   prog.modelOf(i)                                //> res10: Boolean = false
   
    
    
   val ps = new Program(
   	Rule(Set(p.x),Set(),Set()),
		p.y :- p.x
   )                                              //> ps  : org.bomba_lang.proto.Program = Program(x. y ⟵  x.)
   
   ps.solve                                       //> res11: org.bomba_lang.proto.AnswerSets = Set(Set(x, y))
   
   val ps1 = new Program(
   	p.y v p.x
   )                                              //> ps1  : org.bomba_lang.proto.Program = Program(y ∨ x.)
    
   new Program(
   	p.y v p.x
   ).solve                                        //> res12: org.bomba_lang.proto.AnswerSets = Set(Set(y), Set(x))
   
   
   new Program(
   	p.rain :- p.wet,
   	p.wet
   ).solve                                        //> res13: org.bomba_lang.proto.AnswerSets = Set(Set(wet, rain))
   
   val r0 = new Program(
     p.rain :- p.wet
   )                                              //> r0  : org.bomba_lang.proto.Program = Program(rain ⟵  wet.)
   r0.solve                                       //> res14: org.bomba_lang.proto.AnswerSets = Set(Set())
   r0.solve(p.wet)                                //> res15: org.bomba_lang.proto.AnswerSets = Set(Set(wet, rain))
   
   new Program(
   	p.rain(Set("a")) :- p.wet,
   	p.wet
   ).solve                                        //> res16: org.bomba_lang.proto.AnswerSets = Set(Set(wet, rain(Set(a))))
       
  //variables
	val varProg = new Program(
   	p.x(1),
   	p.y(2),
   	p.z('X,'Y) :- (p.x('X),p.y('Y))
   )                                              //> varProg  : org.bomba_lang.proto.Program = Program(x(1). y(2). z('X,'Y) ⟵ 
                                                  //|  x('X) ∧ y('Y).)
  //alternative notation ommiting parens
  p.z('X,'Y) :-  p.x('X) & ~p.y('Y)               //> res17: org.bomba_lang.proto.Rule = z('X,'Y) ⟵  x('X) ∧ not y('Y).
	
	val varProgGround = new GroundProgram(varProg)
                                                  //> varProgGround  : org.bomba_lang.proto.GroundProgram = Program(x(1). y(2). z
                                                  //| (1,1) ⟵  x(1) ∧ y(1). z(1,2) ⟵  x(1) ∧ y(2). z(2,1) ⟵  x(2) ∧ y
                                                  //| (1). z(2,2) ⟵  x(2) ∧ y(2).)
  varProgGround.solve                             //> res18: org.bomba_lang.proto.AnswerSets = Set(Set(z(1,2), y(2), x(1)))
}