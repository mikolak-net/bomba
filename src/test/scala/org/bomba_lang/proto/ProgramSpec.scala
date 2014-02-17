package org.bomba_lang.proto

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.bomba_lang.proto._

class ProgSpec extends FlatSpec with Matchers {

  val testAtom = p.some("y", "z")

  //syntax
  "A literal" should "correctly display default negation" in {
    (-p.a).toString should startWith("-")
  }

  it should "correctly ground itself" in {
    val groundVal1 = "b"
    val groundVal2 = Set(3)

    val variables = Map[Any, Int]('X -> 0, 'Y -> 1)
    val assignment = List(groundVal1, groundVal2)

    (p.a('X).ground(variables, assignment)) should be(p.a(groundVal1))

    ((p.b('X, 2, 'Y).ground(variables, assignment))) should be(p.b(groundVal1, 2, groundVal2))
  }

  //semantics
  it should "validate an interpretation I it is a member of" in {
    testAtom.modelOf(Set(testAtom)) should be(true)
  }

  it should "not validate I that doesn't" in {

    testAtom.modelOf(Set()) should be(false)

    testAtom.modelOf(Set(-testAtom)) should be(false)

    testAtom.modelOf(Set(p.veyrDifferentAtom)) should be(false)

  }

  it should "recognize default negation" in {
    (~testAtom).negBody should contain(testAtom)
  }

  it should "recognize strong negation" in {
    testAtom.strongNegation should be(false)

    (-testAtom).strongNegation should be(true)

    (-(-testAtom)).strongNegation should be(false)
  }

  it should behave like implicativeRule(testAtom)

  it should behave like conjunctiveRule(testAtom)

  "A BodilesRule" should behave like implicativeRule(new BodilesRule(Set(testAtom)))

  "A Rule" should behave like conjunctiveRule(new Rule(Set(), List(p.r)))

  it should "accept safe combinations" in {
    new Program(p.x('X) :- p.x('X))
    new Program(p.x('X) :- -p.x('X))
    new Program(p.x('A, 'B) :- (p.a('A), p.b('B)))
  }

  it should "reject unsafe combinations" in {
    an[IllegalArgumentException] should be thrownBy {
      new Program(p.z('X) :- ~p.a('X))
    }
    
    an[IllegalArgumentException] should be thrownBy {
      new Program(p.x('X) :- p.y('Y))
    }

    an[IllegalArgumentException] should be thrownBy {
      new Program(p.z('X, 'Z) :- (p.a('X), p.b('Y)))
    }

    an[IllegalArgumentException] should be thrownBy {
      new Program(p.z('X, 'Y, 'Z) :- (p.a('X), p.b('Y)))
    }
  }

  it should "contain both positive and negative atoms in the body aggregate" in {
    val a1 = p.a

    val a2 = p.b

    :-(p.a, ~p.b).body should be(Set(a1, a2))
  }

  val testPosRule = p.m :- p.a

  it should "be applicable in the positive case iff its body is contained in I" in {

    testPosRule.applicable(testPosRule.body) should be(true)

    testPosRule.applicable(Set(p.b)) should be(false)
  }

  val testNegRule = p.m :- (p.a, ~p.b)

  it should "be applicable in the general case iff as above and its negative body is not contained in I" in {
    testNegRule.applicable(testNegRule.posBody) should be(true)

    testNegRule.applicable(testNegRule.body) should be(false)
  }

  val testDisjRule = p.m v p.n

  it should "be applied if a head atom is contained in I" in {
    testPosRule.applied(Set()) should be(false)
    testPosRule.applied(Set(p.n)) should be(false)

    testPosRule.applied(testPosRule.head) should be(true)

    testDisjRule.applied(Set(p.a)) should be(false)
    testDisjRule.applied(Set(p.m)) should be(true)
    testDisjRule.applied(testDisjRule.head) should be(true)
  }

  it should "accept I as a model iff it is not applied or applicable and applied" in {
    testPosRule.modelOf(testPosRule.head ++ testPosRule.posBody) should be(true)
    testPosRule.modelOf(testPosRule.head) should be(true)
    testPosRule.modelOf(testPosRule.posBody) should be(false)

    testNegRule.modelOf(testNegRule.head ++ testNegRule.posBody) should be(true)

    testDisjRule.modelOf(Set(p.m, p.blah)) should be(true)
  }

  "A Program" should "disallow predicates with different arities" in {
    an[IllegalArgumentException] should be thrownBy {
      new Program(p.m v p.n, p.m("x") :- p.a)
    }

    //this should pass
    new Program(-p.m, p.m)

  }

  it should "accept I as a model iff all rules accept I as a model" in {
    val testProgram = new Program(p.m v p.n, p.o v p.p)

    testProgram.modelOf(Set(p.m, p.o)) should be(true)
    testProgram.modelOf(Set(p.m, p.n)) should be(false)
  }

  

  
  "A ground Program" should "pass ground programs unmodified" in {
    //general rule
    def `should be same ground program`(r: Rule*) = {
      val orgProg = new Program(r: _*)
      new Program(new GroundProgram(orgProg).rules: _*) should be(orgProg)
    }
    
    //simple condition
    `should be same ground program`(p.x(), p.y())
    
    //disjunctive condition
    `should be same ground program`(p.x(),p.y v p.z())
    
    //default negation condition
    `should be same ground program`(p.a(),p.y :- ~p.x())
    
    
    //strong negation conditions
    `should be same ground program`(p.a(),p.y :- -p.x())
  }

  it should "ground non-ground programs correctly" in {
    //trivial grounding
    new GroundProgram(new Program(p.a("c"),p.a('X) :- p.a('X))) should be
    (new GroundProgram(new Program(p.a("c"),p.a("c") :- p.a("c"))))
    
    //two variables
    new GroundProgram(
        new Program(p.x(1),
        			p.y(2),
        			p.z('X,'Y) :- (p.x('X),p.y('Y)))) should be
    (new GroundProgram(
        new Program(p.x(1),
					p.y(2), 
					p.z(1,1) :- (p.x(1), p.y(1)),
					p.z(1,2) :- (p.x(1), p.y(2)),
					p.z(2,1) :- (p.x(2), p.y(1)),
					p.z(2,2) :- (p.x(2), p.y(2)))))
  }

  def implicativeRule(elem: DisjunctiveImplicative) = {
    val other = p.x
    it should "include added head elements" in {
      (elem v other).head should be(elem.getHead + other)
    }

    it should "include added body" in {
      (elem :- other).body should contain(other)
    }
  }

  def conjunctiveRule(elem: Conjunctive with AtomContainer) = {
    val otherAtom = p.x

    val otherRule = new Rule(Set(), List(otherAtom))

    val goodSet = elem.getPosAtoms + otherAtom

    it should "include body peers" in {
      elem.∧(otherAtom).posBody should be(goodSet)

      elem.∧(otherRule).posBody should be(goodSet)

      elem.∧(~otherAtom).negBody should contain(otherAtom)
    }
  }

}