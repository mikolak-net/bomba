package org.bomba_lang.proto

import org.scalatest.matchers.MustMatchers
import org.scalatest.FlatSpec
import org.bomba_lang.proto._;

class PackageSpec extends FlatSpec with MustMatchers {
  "Permutations " must "not accept length smaller than 0" in {

    for (length <- List(-10, -1)) {
      evaluating {
        permutations(Set(), length)
      } must produce[IllegalArgumentException]
    }
  }

  it must "not accept a degenerate item set case with a non-zero length" in {
    evaluating {
      permutations(Set(), 1)
    } must produce[IllegalArgumentException]
  }

  //trivial and simple cases
  it must "produce the same set for degenerate cases and valid params" in {
    permutations(Set(), 0) must be(Set(List()))

    permutations(Set("a"), 0) must be(Set(List()))
  }

  it must "produce simple lists for the trivial case" in {
    val testElem = "a"

    for (length <- List(1, 3)) {
      permutations(Set(testElem), length) must be(Set(List.fill(length)(testElem)))
    }
  }

  //non-trivial cases
  it must "produce the correct permutation set for a 2-size set and length 3" in {
    permutations(Set("a", "b"), 3) must be(Set(
    											List("a", "b", "b"), 
    											List("b", "b", "b"), 
    											List("a", "b", "a"), 
    											List("a", "a", "b"), 
    											List("b", "a", "a"), 
    											List("b", "b", "a"), 
    											List("a", "a", "a"), 
    											List("b", "a", "b")
    											)
    										)
  }
  
  
  it must "produce the correct permutation set for a 3-size set and length 2" in {
    		permutations(Set("a", "b","c"), 2) must be(Set(
    														List("b", "c"), 
    														List("a", "c"), 
    														List("b", "b"), 
    														List("c", "b"), 
    														List("b", "a"), 
    														List("a", "b"), 
    														List("c", "c"), 
    														List("c", "a"), 
    														List("a", "a")
    													  )
    												  )
  }
  
  it must "produce the correctly-sized permutation set for larger combinations" in {
    for((setSize,length) <- List( (5,6), (7,3) ) ) {
    	permutations((1 to setSize).toSet, length) must have size(scala.math.pow(setSize,length).asInstanceOf[Int]) 
    }
  }

}