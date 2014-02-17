package org.bomba_lang.proto

class NaiveSolverSpec extends SolverSpec {

  "A NaiveSolver" should behave like groundSolver(new NaiveSolver())
  
}