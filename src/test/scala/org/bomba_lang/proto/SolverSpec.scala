package org.bomba_lang.proto

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers
 
abstract class SolverSpec extends FlatSpec with MustMatchers{

  def groundSolver(solver: Solver) = {
    it must "process normal programs" in {
    	solver.solve(new Program(p.x:-())) must be(Set(Set(p.x)))
    
    	solver.solve(new Program(p.x,p.y:- p.x)) must be(Set(Set(p.x,p.y)))
    	
    	solver.solve(new Program(p.y:-(),p.z :- p.y)) must be(Set(Set(p.y,p.z)))
    	
    }
    
    
    it must "process programs with default negation" in {
      solver.solve(new Program(p.x,p.y :- ~p.x)) must be(Set(Set(p.x)))
      
      solver.solve(new Program(p.x:- ~p.y,p.y :- ~p.x)) must be(Set(Set(p.x),Set(p.y)))
    }
    

    
    it must "process programs with disjunctive rules" in {
      solver.solve(new Program(p.x v p.y,p.z:-p.y)) must be(Set(Set(p.x),Set(p.y,p.z)))
    }
    
    it must "process programs with constraints" in {
      solver.solve(new Program(p.x,:-(p.x))) must be(Set())
      
      solver.solve(new Program(p.x v p.y,:-(p.x))) must be(Set(Set(p.y)))
    }
    
    it must "process programs with strong negation" in {
      solver.solve(new Program(p.x,-p.z :- p.x,p.z :- p.x)) must be(Set())
      
      solver.solve(new Program(-p.x,-p.z :- -p.x,p.z :- p.x)) must be(Set(Set(-p.x,-p.z)))
    }
    
    
  }
}