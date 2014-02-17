package org.bomba_lang.proto

import org.scalatest.FlatSpec
import org.scalatest.Matchers
 
abstract class SolverSpec extends FlatSpec with Matchers{

  def groundSolver(solver: Solver) = {
    it should "process normal programs" in {
    	solver.solve(new Program(p.x:-())) should be(Set(Set(p.x)))
    
    	solver.solve(new Program(p.x,p.y:- p.x)) should be(Set(Set(p.x,p.y)))
    	
    	solver.solve(new Program(p.y:-(),p.z :- p.y)) should be(Set(Set(p.y,p.z)))
    	
    }
    
    
    it should "process programs with default negation" in {
      solver.solve(new Program(p.x,p.y :- ~p.x)) should be(Set(Set(p.x)))
      
      solver.solve(new Program(p.x:- ~p.y,p.y :- ~p.x)) should be(Set(Set(p.x),Set(p.y)))
    }
    

    
    it should "process programs with disjunctive rules" in {
      solver.solve(new Program(p.x v p.y,p.z:-p.y)) should be(Set(Set(p.x),Set(p.y,p.z)))
    }
    
    it should "process programs with constraints" in {
      solver.solve(new Program(p.x,:-(p.x))) should be(Set())
      
      solver.solve(new Program(p.x v p.y,:-(p.x))) should be(Set(Set(p.y)))
    }
    
    it should "process programs with strong negation" in {
      solver.solve(new Program(p.x,-p.z :- p.x,p.z :- p.x)) should be(Set())
      
      solver.solve(new Program(-p.x,-p.z :- -p.x,p.z :- p.x)) should be(Set(Set(-p.x,-p.z)))
    }
    
    
  }
}