package org.schodoLog.proto

/**
 * Main logic program entry point.
 * 
 * The Atom2Rule implicit allows to use atoms as arguments (in this case, atoms represent basic rules).
 * 
 * The program guarantees that a predicate has only one arity within it.
 */
class Program(val rules: Rule*) extends Validating {
  
  require(findMismatchedArities.isEmpty,"Predicates must have same arities, mismatches found in: \n" +
  		findMismatchedArities.mkString("\n"))
  
  
  private def findMismatchedArities():Set[(String,Set[Int])] = {
    val allAtoms = getAllAtoms()
    
    //let's see how many brains we'll fry with this one-liner
    allAtoms.groupBy(_.name).iterator.map{case (name,atoms) => (name,atoms.map(_.terms.length))}.filter(_._2.size > 1).toSet
  }
  
  protected def getAllAtoms() = rules.flatMap((r) => r.head++r.body).toSet
  
  def modelOf(i: Interpretation) = rules.forall(_.modelOf(i))

  override def toString() = "Program"+rules.mkString("("," ",")")
}

class GroundProgram(val program: Program) extends Program({
  
  //set of all constants in the program
  val herbrandUniverse = program.rules.flatMap((r) => r.head ++ r.body).flatMap(_.terms).filter(!_.isInstanceOf[Symbol]).toSet

  program.rules.flatMap((r) => {
    var i = 0
    val variables = 
        (r.head ++ r.body).flatMap(_.terms).filter(_.isInstanceOf[Symbol]).toSet.toList.zipWithIndex.toMap

    
    //all posible assignments
    val assignments = herbrandUniverse.subsets(variables.size).flatMap(_.toList.permutations)
    
    
    def groundLiteral(l:Literal,assignment:List[Any]) = {
      Literal(l.name,l.strongNegation,l.terms.map((t) => if(variables.contains(t)) assignment(variables(t)) else t ))
    }
    
    val l = for{assignment <- assignments} 
      yield Rule(r.head.map(groundLiteral(_,assignment)),r.posBody.map(groundLiteral(_,assignment)),r.negBody.map(groundLiteral(_,assignment)))
    l
  })
}: _*)

/**
 * Base logic rule class. Can be also represented by an Atom, by implicit conversion in the package object.
 */
case class Rule(val head: Set[Literal],val posBody: Set[Literal],val negBody: Set[Literal]) 
	extends  Validating with AtomContainer with Conjunctive {
  
  def this(head: Set[Literal],body: Seq[AtomContainer]) = this(head,body.flatMap(_.getPosAtoms).toSet,body.flatMap(_.getNegAtoms).toSet)
  
  val body = posBody++negBody
  
  
  
  def applicable(i: Interpretation) = posBody.forall(_.modelOf(i)) && !negBody.exists(_.modelOf(i))
  
  def applied(i: Interpretation) = head.exists(_.modelOf(i))
  
  def modelOf(i: Interpretation) = if(applicable(i)) applied(i) else true
  
  
  
  def getHead = head
  
  def getPosAtoms = posBody
  
  def getNegAtoms = negBody
  
  
  
  protected def and(bodyRule: Rule) = {
    new Rule(head++bodyRule.head,posBody++bodyRule.posBody,negBody++bodyRule.negBody)
  }
  
  override def toString() = {
    head.mkString(" ∨ ") + (if(body.isEmpty) "" else " ⟵  "+(posBody.map(_.toString)++negBody.map("not "+_.toString) ).mkString(" ∧ ") )+"."
  }
  
}



/**
 * Special Rule variant, exists only to prevent rules of the form p"x" :- p"y" :- p"z" .
 */
class BodilesRule(override val head: Set[Literal]) extends Rule(head,Set(),Set()) with DisjunctiveImplicative {
  
  protected def or(addAtom: Literal) =  new BodilesRule(head + addAtom)

}


/**
 * Basic Literal/Atom, building block of rules.
 * 
 * Implicit conversion to rule available for convenience.
 */
case class Literal(val name: String, val strongNegation: Boolean, val terms: Any*) 
	extends Validating with Conjunctive with DisjunctiveImplicative with AtomContainer {
  
  def this(name:String) = this(name,false)
  
  def apply(terms: Any*) = Literal(name, strongNegation, terms: _*)
  
  /**
   * Note: this is the same result as for getPosAtoms, take care when composing new rule syntax!
   */
  def getHead = Set(Literal.this) 

  protected def and(bodyRule: Rule) = Rule(bodyRule.head, bodyRule.posBody+Literal.this, bodyRule.negBody)
  
  protected def or(otherAtom: Literal) = new BodilesRule(Set(Literal.this, otherAtom))
 
  
  def getPosAtoms = Set(Literal.this)
  
  def getNegAtoms = Set()
  
  def v(rule: Rule) = new Rule(rule.head+Literal.this, rule.posBody, rule.negBody)
  
  def modelOf(i: Interpretation) = i contains Literal.this
  
  /**
   * Default negation.
   */
  def unary_~ = new Rule(Set(), Set(), Set(Literal.this))
  
  /**
   * Strong negation. *Not* idempotent - applying again reverts the atom to the "positive" form.
   */
  def unary_- = Literal(name, !strongNegation, terms: _*)
  
  override def toString() =  (if(strongNegation) "-" else "") + name+(if(terms.isEmpty) "" else terms.mkString("(",",",")"))
  
  
}

/**
 * Represents objects that can verify the validity of interpretations.
 */
trait Validating {
  
  /**
   * @return <code>true</code> iff this interpretation is a model of this instance
   */
  def modelOf(i: Interpretation): Boolean
  
}


/**
 * "Worker" trait for head elements of rules, adding the necessary functional syntax
 * to compose a rule.
 */
trait DisjunctiveImplicative {
  
  /**
   * All atoms in the head of this rule element.
   */
  def getHead: Set[Literal]
  
  /**
   * Parent method for head composition.
   */
  protected def or(newAtom: Literal): BodilesRule

  /**
   * Parent method for head -> body append.
   */
  protected def implies(body: AtomContainer*): Rule = new Rule(getHead,body)
  
  def v(newAtom: Literal) = or(newAtom)
  def ∨(newAtom: Literal) = or(newAtom) //this is the LOGICAL OR Unicode sign, for non-eagle-eyed
  def |(newAtom: Literal) = or(newAtom) //TODO: is this really a good idea for an alternative?
  
  def :-(body: AtomContainer*) = implies(body: _*)
  def ⟵(body: AtomContainer*) = implies(body: _*)
}

/**
 * "Worker" trait for body elements of rules.
 */
trait Conjunctive {

  /**
   * Parent method for rule -> new body elem append
   */
  protected def and(bodyRule: Rule): Rule
  
  /**
   * Helper method for <code>and</code> operation variants.
   */
  protected def and(other: Literal): Rule = and(Rule(Set(),Set(other),Set()))

  
  def ∧(other: Literal) = and(other)
  def ∧(bodyRule: Rule) =  and(bodyRule)
}

/**
 * Helper trait for rule composition.
 */
trait AtomContainer {
  def getPosAtoms: Set[Literal]
  
  def getNegAtoms: Set[Literal]
}