package org.bomba_lang.proto

class NaiveSolverSpec extends SolverSpec {

  "A NaiveSolver" must behave like groundSolver(new NaiveSolver())
  
}