package org.schodoLog

import org.schodoLog.proto.AtomContainer
import org.schodoLog.proto.Literal
import org.schodoLog.proto.Rule
import scala.language.dynamics

package object proto {
  
  
	type Interpretation = Set[Literal]
	
	type Variable = Symbol
	
	type AnswerSet = Set[Set[Literal]]
	
	/**
	 * Default solver needed for Program.solve defs.
	 */
	implicit val defaultSolver: () => Solver = {(() => new NaiveSolver())}
	 
	/**
	 * Implicit conversion method for Program's constructor, to allow for facts.
	 */
	implicit def Atom2Rule(atom: Literal) = atom:-()
	
	object p extends AnyRef with Dynamic {
	  
	    /**
	     * for preventing any2Ensuring and any2ArrowAssoc implicit conversions from Predef
	     */
		def x = new Literal("x")
	  
		def selectDynamic(predName: String): Literal = new Literal(predName)
	  
		def applyDynamic(predName: String)(args: Any*): Literal = new Literal(predName)(args : _*)
	}
	
	/**
	 * Syntactic sugar for constraints.
	 */
	def :-(atoms: AtomContainer*) =  new Rule(Set(),atoms)
	
	def ⟵(atoms: AtomContainer*) =  :-(atoms: _*)
	
	def :-(rule: Rule) = rule

	def ⟵(rule: Rule) = :-(rule)
	
	/**
	 * Generates all possible permutations (with replacement) of <code>items</code>
	 * of length <code>length</code>.
	 */
	def permutations[T](items: Set[T],length: Int): Set[List[T]] = {
		
	  if(length < 0) {
	    throw new IllegalArgumentException("Length must be >= 0")
	  }
	  
	  if(items.isEmpty && length > 0) {
	    throw new IllegalArgumentException("Empty set cannot generate non-zero-length permutations!")
	  }
	  
	  def doPermutations[T](items: Set[T],length: Int): Set[List[T]] = length match {
	    case 0 => Set(List())
	    case length => items.flatMap(item => doPermutations(items,length-1).map(item::_))
	  }
	  
	  doPermutations(items,length)
	}
	
	
  /**
   * Helper function for variable set generation in a given part of a rule.
   */
  def extractVars(litSet: Set[Literal]): Set[Variable] = {
      litSet.flatMap(_.terms.filter(_.isInstanceOf[Variable])).toSet.asInstanceOf[Set[Variable]]
  }
}