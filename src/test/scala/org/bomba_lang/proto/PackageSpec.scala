package org.bomba_lang.proto

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.bomba_lang.proto._;

class PackageSpec extends FlatSpec with Matchers {
  "Permutations " should "not accept length smaller than 0" in {

    for (length <- List(-10, -1)) {
      an[IllegalArgumentException] should be thrownBy {
        permutations(Set(), length)
      }
    }
  }

  it should "not accept a degenerate item set case with a non-zero length" in {
    an[IllegalArgumentException] should be thrownBy {
      permutations(Set(), 1)
    }
  }

  //trivial and simple cases
  it should "produce the same set for degenerate cases and valid params" in {
    permutations(Set(), 0) should be(Set(List()))

    permutations(Set("a"), 0) should be(Set(List()))
  }

  it should "produce simple lists for the trivial case" in {
    val testElem = "a"

    for (length <- List(1, 3)) {
      permutations(Set(testElem), length) should be(Set(List.fill(length)(testElem)))
    }
  }

  //non-trivial cases
  it should "produce the correct permutation set for a 2-size set and length 3" in {
    permutations(Set("a", "b"), 3) should be(Set(
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
  
  
  it should "produce the correct permutation set for a 3-size set and length 2" in {
    		permutations(Set("a", "b","c"), 2) should be(Set(
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
  
  it should "produce the correctly-sized permutation set for larger combinations" in {
    for((setSize,length) <- List( (5,6), (7,3) ) ) {
    	permutations((1 to setSize).toSet, length) should have size(scala.math.pow(setSize,length).asInstanceOf[Int]) 
    }
  }

}