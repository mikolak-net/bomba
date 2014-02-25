package org.bomba_lang.proto

/**
 * Main logic program entry point.
 *
 * The Atom2Rule implicit allows to use atoms as arguments (in this case, atoms represent basic rules).
 *
 * The program guarantees that a predicate has only one arity within it.
 */
class Program(val rules: Rule*) extends Validating {

  require(findMismatchedArities.isEmpty, "Predicates must have same arities, mismatches found in: \n" +
    findMismatchedArities.mkString("\n"))

  lazy val findMismatchedArities: Set[(String, Set[Int])] = {
    val allAtoms = rules.flatMap((r) => r.head ++ r.body).toSet

    //let's see how many brains we'll fry with this one-liner
    allAtoms.groupBy(_.name).iterator.map { case (name, atoms) => (name, atoms.map(_.terms.length)) }.filter(_._2.size > 1).toSet
  }

  //Note: this is not strictly needed currently (i.e. nothing will blow up if unsafe rules are allowed), 
  //this is just a bit of future-proofing
  require(unsafeRuleVariablePairs.isEmpty, "Unsafe rules in program: \n" +
    unsafeRuleVariablePairs.mkString("\n"))

  lazy val unsafeRuleVariablePairs: Seq[(Rule, Set[Variable])] = {
    //a rule is safe if every variable in the negative body and head is also in the positive body
    rules.map((r) => (r, (extractVars(r.negBody) ++ extractVars(r.head)) -- extractVars(r.posBody))).filterNot(_._2.isEmpty)
  }

  def modelOf(i: Interpretation) = rules.forall(_.modelOf(i))

  override def toString() = "Program" + rules.mkString("(", " ", ")")

  override def equals(obj: Any) = (obj.isInstanceOf[Program] && obj.asInstanceOf[Program].rules == rules)

  def solve(implicit solverGen: () => Solver): AnswerSets = solverGen().solve(this)

  /**
   * Allows for adding program snippets and data, idiomatic to interaction with "normal" code.
   */
  def solve(mergedProgram: Program)(implicit solverGen: () => Solver): AnswerSets = {
    merge(mergedProgram).solve(solverGen)
  }

  /**
   * Allows to add facts from an external source. For each <code>piece</code>, a new 
   * fact rule will be created and added to the program. If the <code>piece</code> is a 
   * <code>TupleN</code> (actually a <code>Product</code>), the fact's literal will be
   * N-ary. Otherwise, it will be unary.  
   */
  def feed(predicateName: String)(pieces: Any*) = {
    merge(pieces.map(piece => 
      new FactRule(Set(Literal(predicateName, false, (piece match {
													      case p: Product => p.productIterator.toSeq
													      case _ => List(piece) //since we need the "unravel" operator on the outside
												      }):_* ))))
    		  				   
    	)
  }

  /**
   * Utility method for splicing rules into an existing program.
   */
  private def merge(toMerge: Seq[Rule]): Program = new Program((rules.view ++ toMerge): _*)

}

class GroundProgram(val program: Program) extends Program({

  //set of all constants in the program
  val herbrandUniverse = program.rules.flatMap((r) => r.head ++ r.body).flatMap(_.terms).filter(!_.isInstanceOf[Variable]).toSet

  program.rules.flatMap((r) => {

    val variables = extractVars(r.head ++ r.body).asInstanceOf[Set[Any]].toList.zipWithIndex.toMap

    //all posible assignments
    val assignments = permutations(herbrandUniverse, variables.size)

    //helper function for generating grounded literal sets
    def groundLiterals(set: Set[Literal], assignment: List[Any]) = {
      set.map(_.ground(variables, assignment))
    }

    val l = for { assignment <- assignments }
      yield Rule(groundLiterals(r.head, assignment), groundLiterals(r.posBody, assignment), groundLiterals(r.negBody, assignment))
    l.toSet
  })
}: _*) //this is actually the end of the superclass override declaration - maybe it could be made nicer somehow?

/**
 * Base logic rule class. Can be also represented by an Atom, by implicit conversion in the package object.
 */
case class Rule(val head: Set[Literal], val posBody: Set[Literal], val negBody: Set[Literal])
  extends Validating with AtomContainer with Conjunctive {

  //require check for safety

  def this(head: Set[Literal], body: Seq[AtomContainer]) = this(head, body.flatMap(_.getPosAtoms).toSet, body.flatMap(_.getNegAtoms).toSet)

  val body = posBody ++ negBody

  def applicable(i: Interpretation) = posBody.forall(_.modelOf(i)) && !negBody.exists(_.modelOf(i))

  def applied(i: Interpretation) = head.exists(_.modelOf(i))

  def modelOf(i: Interpretation) = if (applicable(i)) applied(i) else true

  def getHead = head

  def getPosAtoms = posBody

  def getNegAtoms = negBody

  protected def and(bodyRule: Rule) = {
    new Rule(head ++ bodyRule.head, posBody ++ bodyRule.posBody, negBody ++ bodyRule.negBody)
  }

  override def toString() = {
    (if (head.isEmpty) "⊥" else head.mkString(" ∨ ")) + (if (body.isEmpty) "" else " ⟵  " + (posBody.map(_.toString) ++ negBody.map("not " + _.toString)).mkString(" ∧ ")) + "."
  }

}

/**
 * Special Rule variant, exists only to prevent rules of the form p"x" :- p"y" :- p"z" .
 */
protected class FactRule(override val head: Set[Literal]) extends Rule(head, Set(), Set()) with Implicative {

}

/**
 * Special Rule variant, same reason as Contraint Rule but allows for multiple head elements
 */
protected class BodilesRule(override val head: Set[Literal]) extends FactRule(head) with DisjunctiveImplicative {

  protected def or(addAtom: Literal) = new BodilesRule(head + addAtom)

}

/**
 * Basic Literal/Atom, building block of rules.
 *
 * Implicit conversion to rule available for convenience.
 */
case class Literal(val name: String, val strongNegation: Boolean, val terms: Any*)
  extends Validating with Conjunctive with DisjunctiveImplicative with AtomContainer {

  def this(name: String) = this(name, false)

  def apply(terms: Any*) = Literal(name, strongNegation, terms: _*)

  /**
   * Note: this is the same result as for getPosAtoms, take care when composing new rule syntax!
   */
  def getHead = Set(Literal.this)

  protected def and(bodyRule: Rule) = Rule(bodyRule.head, bodyRule.posBody + Literal.this, bodyRule.negBody)

  protected def or(otherAtom: Literal) = new BodilesRule(Set(Literal.this, otherAtom))

  def getPosAtoms = Set(Literal.this)

  def getNegAtoms = Set()

  def v(rule: Rule) = new Rule(rule.head + Literal.this, rule.posBody, rule.negBody)

  def modelOf(i: Interpretation) = i contains Literal.this

  /**
   * Default negation.
   */
  def unary_~ = new Rule(Set(), Set(), Set(Literal.this))

  /**
   * Strong negation. *Not* idempotent - applying again reverts the atom to the "positive" form.
   */
  def unary_- = Literal(name, !strongNegation, terms: _*)

  override def toString() = (if (strongNegation) "-" else "") + name + (if (terms.isEmpty) "" else terms.mkString("(", ",", ")"))

  /**
   * Grounds the given literal.
   */
  def ground(variables: Map[Any, Int], assignment: List[Any]) = {
    Literal(name, strongNegation, terms.map((t) => if (variables.contains(t)) assignment(variables(t)) else t): _*)
  }

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
 * "Worker" trait for head parts of a rule, specialized specifically to facilitate
 * constraints.
 */
trait Implicative {

  /**
   * All atoms in the head of this rule element.
   */
  def getHead: Set[Literal]

  /**
   * Parent method for head -> body append.
   */
  protected def implies(body: AtomContainer*): Rule = new Rule(getHead, body)

  def :-(body: AtomContainer*) = implies(body: _*)
  def ⟵(body: AtomContainer*) = implies(body: _*)
}

/**
 * "Worker" trait for head elements of rules, adding the necessary functional syntax
 * to compose a rule.
 */
trait DisjunctiveImplicative extends Implicative {

  /**
   * Parent method for head composition.
   */
  protected def or(newAtom: Literal): BodilesRule

  def v(newAtom: Literal) = or(newAtom)
  def ∨(newAtom: Literal) = or(newAtom) //this is the LOGICAL OR Unicode sign, for non-eagle-eyed
  def |(newAtom: Literal) = or(newAtom) //TODO: is this really a good idea for an alternative?

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
  protected def and(other: Literal): Rule = and(Rule(Set(), Set(other), Set()))

  def &(other: Rule) = and(other)
  def &(other: Literal) = and(other)

  def ∧(other: Literal) = and(other)
  def ∧(bodyRule: Rule) = and(bodyRule)
}

/**
 * Helper trait for rule composition.
 */
trait AtomContainer {
  def getPosAtoms: Set[Literal]

  def getNegAtoms: Set[Literal]
}