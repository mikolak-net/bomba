package org.schodoLog

/**
 * schodoLog prototype package
 * 
 * Contains the following core elements:
 * -Program - core type for creating program
 * -Rule - type that represents a (not necessarily completely constructed) logic rule
 * -Atom - type that represents a logic atom, can be implicitly converted into a basic rule
 * -implicits for syntactic sugar allowing to define logic programs in a more concise way
 * -object methods that do the same, specifically allowing to create constraints
 */
package object proto {
  
  
	type Interpretation = Set[Literal]
	
	/**
	 * Implicit conversion method for Program's constructor, to allow for facts.
	 */
	implicit def Atom2Rule(atom: Literal) = atom:-()
	
	implicit class AtomConverter(val sc: StringContext) extends AnyVal {
	  def p(args: Any*) = {
	    Literal(sc.raw(),false)
	  }
    }
	
	/**
	 * Syntactic sugar for constraints.
	 */
	def :-(atoms: AtomContainer*) =  new Rule(Set(),atoms)
	
	def ⟵(atoms: AtomContainer*) =  :-(atoms: _*)
	
	def :-(rule: Rule) = rule

	def ⟵(rule: Rule) = :-(rule)
}