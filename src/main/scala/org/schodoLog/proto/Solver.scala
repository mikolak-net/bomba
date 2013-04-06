package org.schodoLog.proto

/**
 * General trait for logic programming solvers, base for any custom implementations.
 */
trait Solver {
  
  def solve(r: Rule*): Set[Set[Literal]] = solve(Program(r: _*))
  
  def solve(p: Program): Set[Set[Literal]]

}

/**
 * Reference solver implementation. All operations are written in a way
 * that attempts to emulate their formal definitions.
 */
class NaiveSolver extends Solver {
  
  override def solve(p: Program) = {
    
    //all possible interpretations
    val allInterpretations = p.rules.flatMap((r) => r.body++r.head).toSet.subsets
    
    //coherent interpretations, i.e.
    //such that no pair of the form (a,-a) exists in I
    val coherentInterpretations = allInterpretations.filter(
    									(i) => {!i.exists( (a) => i.contains(-a) )}
    									)
    
    //all models
    val allModels = coherentInterpretations.filter((i) => p.modelOf(i)).toSet
    //we need a toSet() for the subsequent filter() to check against the entire set
    
    //minimal models
    allModels.filter((i) => !allModels.filter(_ != i).exists(_.subsetOf(i)))
  }
  
}