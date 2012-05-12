package regolic.smt

import regolic.asts.core.Trees._
import regolic.asts.core.Manip._
import regolic.asts.fol.Trees._
import regolic.asts.fol.Manip._
import regolic.parsers.SmtLib2.Trees._

import regolic.smt.qfeuf.CongruenceSolver
import regolic.smt.qflra.SimplexSolver

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

trait Solver {

  val logic: Logic
  def isSat(f: Formula): Option[Map[FunctionSymbol, Term]]

  def isValid(f: Formula): Option[Map[FunctionSymbol, Term]] = isSat(Not(f))

}

object Solver {

  val allSolvers: List[Solver] = List(CongruenceSolver, SimplexSolver)

  def execute(cmds: List[Command]) {
    println("Executing following script: " + cmds)
    var solver: Option[Solver] = None
    var asserts: List[Formula] = List(True())

    for(cmd <- cmds) {
      cmd match {
        case SetLogic(logic) => solver = getSolver(logic)
        case Pop(n) => {
          asserts = asserts.tail
        }
        case Push(n) => {
          asserts ::= True()
        }
        case Assert(f) => {
          asserts = And(f, asserts.head) :: asserts.tail
        }
        case CheckSat => {
          val formula = asserts.foldLeft(True(): Formula)((acc, f) => And(acc, f))
          println("Formula to check is: " + formula)
          println("isSat: " + solver.get.isSat(formula))
        }
      }
    }

  }


  def getSolver(logic: Logic): Option[Solver] = allSolvers.find(_.logic == logic)

}
