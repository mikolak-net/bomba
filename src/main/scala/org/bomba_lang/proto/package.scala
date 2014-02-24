package org.bomba_lang

import org.bomba_lang.proto.Literal;
import org.bomba_lang.proto.NaiveSolver;
import org.bomba_lang.proto.Rule;
import org.bomba_lang.proto.annotImpl;
import org.bomba_lang.proto.AtomContainer

import scala.language.dynamics
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

package object proto {
  
  
	type Interpretation = Set[Literal]
	
	type Variable = Symbol
	
	type AnswerSets = Set[Set[Literal]]
	
	/**
	 * Default solver needed for Program.solve defs.
	 */
	implicit val defaultSolver: () => Solver = {(() => new NaiveSolver())}
	 
	/**
	 * Implicit conversion method for Program's constructor, to allow for facts.
	 */
	implicit def Atom2Rule(atom: Literal) = atom:-()
	
	/**
	 * Implicit conversion for constraints.
	 */
	implicit def Nil2Rule(nil: Nil.type) = new ConstraintRule(Set())

	/**
	 * Use this annotation on a val to instantiate a program.
	 */
	class bomba extends StaticAnnotation {
		def macroTransform(annottees: Any*) = macro annotImpl.impl
	}

	
	/**
	 * Syntactic sugar for constraints.
	 */
	def :-(atoms: AtomContainer*) =  new Rule(Set(),atoms)
	
	def ⟵(atoms: AtomContainer*) =  :-(atoms: _*)
	
	def :-(rule: Rule) = rule

	def ⟵(rule: Rule) = :-(rule)
	
	val ⊥ = Nil
	
	/**
	 * Utility implicits
	 */
	implicit def RuleSeq2Program(rules: Seq[Rule]) = new Program(rules: _*)
	
	implicit def Program2RuleSeq(program: Program): Seq[Rule] = program.rules
	
	/**
	 * Generates all possible permutations (with replacement) of <code>items</code>
	 * of length <code>length</code>.
	 */
	private[bomba_lang] def permutations[T](items: Set[T],length: Int): Set[List[T]] = {
		
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
  private[bomba_lang] def extractVars(litSet: Set[Literal]): Set[Variable] = {
      litSet.flatMap(_.terms.filter(_.isInstanceOf[Variable])).toSet.asInstanceOf[Set[Variable]]
  }
  
  
  	/**
	 * This is now only used for tests and other invocation within this package.
	 * The reason why this is still necessary is the fact that macro annotations 
	 * can't be used within "their" project - the only alternative would be to split 
	 * the tests into a separate project (which might not be such a bad idea).
	 */
	private[bomba_lang] object p extends AnyRef with Dynamic {
	  
	    /**
	     * for preventing any2Ensuring and any2ArrowAssoc implicit conversions from Predef
	     */
		def x = applyDynamic("x")()
	  
		def selectDynamic(predName: String): Literal = new Literal(predName)
	  
		def applyDynamic(predName: String)(args: Any*): Literal = new Literal(predName)(args : _*)
	}
}