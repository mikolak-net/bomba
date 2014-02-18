package org.bomba_lang.proto

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.annotation.StaticAnnotation
import scala.collection.mutable.ListBuffer

object annotImpl {
  
	/**
	 * Contains all symbol strings required for analysis.
	 */
	private val OPERATORS = Set(
								"v","$u2228"/* == ∨ */,"$bar","$amp","$colon$minus","$u27F5"/* == ⟵ */,
								"unary_$tilde","unary_$minus"
								)
	/**
	 * Contains all restricted literal names - will not be shadowed with Literal definitions.
	 */							
	private val RESTRICTED_NAMES = Set("Nil","$u22A5"/* == ⊥ */)
  
  
	def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
	  	import c.universe._
	  	
	  	def extractPreds(mainList: List[c.universe.Tree]): Set[(String,Int)] = {
	  	  
	  	  
	  	  def structureError(cause: Tree) {
	  	    c.abort(cause.pos,"Unrecognized bomba program structure!")
	  	  }
	  	  
	  	  
	  	  def doExtract(treeList: List[c.universe.Tree]): Set[(String,Int)] = {
	  	    var ret = Set[(String,Int)]()
	  	    
	  	    for(elem <- treeList) {
	  	      elem match {
	  	        case i: Ident => {
	  	          //a simple case, e.g. for facts
	  	          val identName = i.name.decoded
	  	          if(!RESTRICTED_NAMES.contains(identName)) {
	  	        	  ret += ((identName,0))
	  	          }
	  	        }
	  	        case a: Apply => {
	  	           //Apply is *never* used for operators, only Select 
	  	          
  	              a.fun match {
  	                case n:Ident => {
  	                  //this is a predicate - any child Applies and Selects are actual function calls/field refs
  	                  ret += ((n.name.toString(),a.args.size))
  	                }
  	                case s:Select => {
  	                  if(a.args.size != 1) {
  	                    structureError(a)
  	                  }
  	                  ret ++= doExtract(List(a.args(0)))
  	                  ret ++= doExtract(List(s))
  	                }
  	                case _ => structureError(a.fun)
  	              }
	  	        }
	  	        case s: Select => {
	  	          if(OPERATORS.contains(s.name.toString())) {
	  	            ret ++= doExtract(s.children)
	  	          } else {
	  	            ret += ((s.name.toString(),0))
	  	            ret ++= (s.qualifier match {
	  	              case a: Apply => doExtract(List(a))
	  	              case _ => Set()
	  	            })
	  	          }
	  	        }
	  	        case _ => structureError(elem)
	  	      }
	  	    }
	  	    
	  	    ret
	  	  }
	  	  
	  	  doExtract(mainList)
	  	}
	  	
	  	if(annottees.size != 1) {
	  	  c.error(c.enclosingPosition,"bomba programs should be annotated separately (1 def per annotation).")
	  	  c.Expr[Any](null)
	  	} else {
	  	  val toAnnotate = annottees(0)
	  	  
	  	  val valDef = toAnnotate.tree match {
	  	    case ValDef(mods, name, tpt, rhs) => {
	  	      
	  	      //obtain the program to be rewritten
	  	      val toProcess = rhs match {
	  	        case b: Block => b.children
	  	        case a: Apply => List(a)
	  	        case i: Ident => List(i)
	  	        case _ =>  {
	  	          c.abort(rhs.pos, "A bomba program must be either a single rule or enclosed in a block!")
	  	        }
	  	      }  
	  	      
  	  	      val preds = extractPreds(toProcess)

  	  	      //checking for duplicate arities
  	  	      val predCheck = preds.groupBy(_._1)

  	  	      for((pred,arities) <- predCheck) {
  	  	        if(arities.size > 1) {
  	  	          c.abort(toAnnotate.tree.pos, s"Predicate '$pred' has inconsistent arities: ${arities.map(_._2).mkString("(", ",", ")")}")
  	  	        }
  	  	      }
  	  	      
  	  	      var outList = List[Tree]()
  	  	      val lits = for(p <- preds) yield {
  	  	        genLitDef(c)(p)
  	  	      }
	  	      outList ++= lits
  	  	      outList ++= List(genProgDef(c)(toProcess.toList))
  	  	      
	  	      c.Expr[Any](ValDef(mods,name,tpt,Block(outList:_*)))
	  	    } 
	  	    case _ => {
	  	      c.abort(toAnnotate.tree.pos, "only vals can be bomba programs")
	  	    }
	  	  }

	  	  valDef
	  	}
	}
	
	private def genProgDef(c: Context)(ruleDefs: List[c.universe.Tree]): c.universe.Tree = {
	  import c.universe._
	  
		Apply(
		 Select(
		  New(
		   Ident(
		    newTypeName(
		     classOf[Program].getSimpleName())
		    )
		   )
		  , nme.CONSTRUCTOR)
		 , ruleDefs
		 )
	}
	
	private def genLitDef(c: Context)(predData: (String,Int)) = {
	  import c.universe._
	  
	  val (name,arity) = predData
	  
      DefDef(
		  Modifiers(
		   )
		  , newTermName(
		   name)
		  , List(
		   )
		  , if(arity == 0) {
			  List()
		    } else { //wildcard arg definition
		      List(
			   List(
			    ValDef(
			     Modifiers(
			      Flag.PARAM)
			     , newTermName(
			      "args")
			     , AppliedTypeTree(
			      Select(
			       Select(
			        Ident(
			         nme.ROOTPKG)
			        ,c.mirror.staticPackage("scala"))
			       , newTypeName(
			        "<repeated>")
			       )
			      , List(
			       Ident(
			        newTypeName(
			         "Any")
			        )
			       )
			      )
			     , EmptyTree)
			    )
			   )
		    } 
		  , TypeTree(
		   )
		  , Apply(
		   Ident(
		    newTermName(
		     classOf[org.bomba_lang.proto.Literal].getSimpleName())
		    )
		  , {
				 var argList = ListBuffer[Tree](
								    Literal(
								     Constant(
								      name)
								     )
								    , Literal(
								     Constant(
								      false)
								     )
								    )
			     //wildcard arg indirection to bomba Literal construction method
			     if(arity > 0) {
			       argList += Typed(
							     Ident(
							      newTermName(
							       "args")
							      )
							     , Ident(
							      tpnme.WILDCARD_STAR)
							     )
			     }
				 argList.toList
		   }
		  )
      )
	}
	
}

private[proto] object macroUtils {
   val LPAR = '('
	  
   val RPAR = ')'
  
   val PARENS = Set(LPAR,RPAR)

   /**
    * Just adds spacing to parens. Use with showRaw for more readability.
    */
   def showPrint(str: String) {
	    var expLevel = 0
	
	    for (char <- str) {
	      val mod = if (PARENS.contains(char)) {
	        if (char == LPAR) {
	          1
	        } else { //RPAR
	          -1
	        }
	      } else { 0 }
	      expLevel += mod
	      print(char)
	      if (mod != 0) {
	        println()
	        print((1 to expLevel).map(_ => " ").mkString(""))
	      }
	    }
	    println()
	}
}