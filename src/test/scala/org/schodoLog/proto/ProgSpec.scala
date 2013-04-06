package org.schodoLog.proto

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers

class ProgSpec extends FlatSpec with MustMatchers {
	
   val testAtom = p"some"("y","z")

   
   //syntax
   "A literal" must "correctly display default negation" in {
     (-p"a").toString must startWith ("-")
   }
   
   //semantics
   it must "validate an interpretation I it is a member of" in {
     testAtom.modelOf(Set(testAtom)) must be(true)
   } 
   
   it must "not validate I that doesn't" in {
     
     testAtom.modelOf(Set()) must be(false)
     
     testAtom.modelOf(Set(-testAtom)) must be(false)
     
     testAtom.modelOf(Set(p"veyrDifferentAtom")) must be(false)
     
   }
   
   it must "recognize default negation" in {
     (~testAtom).negBody must contain(testAtom)
   }
   
   it must "recognize strong negation" in {
     testAtom.strongNegation must be(false)
     
     (-testAtom).strongNegation must be(true)
   
     (-(-testAtom)).strongNegation must be(false)
   }
   
   it must behave like implicativeRule(testAtom)
   
   it must behave like conjunctiveRule(testAtom)
   
   "A BodilesRule" must behave like implicativeRule(new BodilesRule(Set(testAtom)))
   
   
   
   "A Rule" must behave like conjunctiveRule(new Rule(Set(),List(p"r")))
  
   it must "contain both positive and negative atoms in the body aggregate" in {
     val a1 = p"a"
     
     val a2 = p"b"
     
     :-(p"a",~p"b").body must be(Set(a1,a2))
   }
   
   val testPosRule = p"m" :- p"a"
   
   it must "be applicable in the positive case iff its body is contained in I" in {
      
	  testPosRule.applicable(testPosRule.body) must be(true)
   
	  testPosRule.applicable(Set(p"b")) must be(false)
   }
  
   val testNegRule = p"m" :- (p"a",~p"b")
   
   it must "be applicable in the general case iff as above and its negative body is not contained in I" in {
     testNegRule.applicable(testNegRule.posBody) must be(true)
     
     testNegRule.applicable(testNegRule.body) must be(false)
   }

  val testDisjRule = p"m" v p"n"
   
   it must "be applied if a head atom is contained in I" in {
     testPosRule.applied(Set()) must be(false)
     testPosRule.applied(Set(p"n")) must be(false)
     
     testPosRule.applied(testPosRule.head) must be(true)
     
     
     testDisjRule.applied(Set(p"a")) must be(false)
     testDisjRule.applied(Set(p"m")) must be(true)
     testDisjRule.applied(testDisjRule.head) must be(true)
   }
   
   it must "accept I as a model iff it is not applied or applicable and applied" in {
     testPosRule.modelOf(testPosRule.head++testPosRule.posBody) must be(true)
     testPosRule.modelOf(testPosRule.head) must be(true)
     testPosRule.modelOf(testPosRule.posBody) must be(false)
     
     testNegRule.modelOf(testNegRule.head++testNegRule.posBody) must be(true)
     
     testDisjRule.modelOf(Set(p"m",p"blah")) must be(true)
   }
   
   
   "A Program" must "disallow predicates with different arities" in {
     evaluating {
       new Program(p"m" v p"n",p"m"("x") :- p"a")
     } must produce [IllegalArgumentException]
     
     //this should pass
     new Program(-p"m",p"m")
     
   }
   
   it must "accept I as a model iff all rules accept I as a model" in {
      val testProgram = new Program(p"m" v p"n",p"o" v p"p")
      
      testProgram.modelOf(Set(p"m",p"o")) must be(true)
      testProgram.modelOf(Set(p"m",p"n")) must be(false)
   }
   
   
   
   
   def implicativeRule(elem: DisjunctiveImplicative) = {
	 val other = p"x"
     it must "include added head elements" in {
	     (elem v other).head must be(elem.getHead+other)
     }
     
     it must "include added body" in {
    	 (elem :- other).body must contain(other)
     }
   }
   
   
   def conjunctiveRule(elem: Conjunctive with AtomContainer) = {
	 val otherAtom = p"x"
     
	 val otherRule = new Rule(Set(),List(otherAtom))
	 
	 val goodSet = elem.getPosAtoms+otherAtom
	 
     it must "include body peers" in {
       elem.∧(otherAtom).posBody must be(goodSet)
       
       elem.∧(otherRule).posBody must be(goodSet)
       
       elem.∧(~otherAtom).negBody must contain(otherAtom)
     }
   }
   
}