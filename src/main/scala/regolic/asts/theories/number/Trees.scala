package regolic.asts.theories.number

import regolic.asts.core.Trees._
import regolic.asts.fol.Trees.Constant
import regolic.asts.fol.{Trees => FolT}
import regolic.asts.theories.int.{Trees => IntT}
import regolic.asts.theories.real.{Trees => RealT}
import regolic.algebra.Rational

/*
 * An attempt to make it easy to extract int or real
 * in a generic way, however cannot build any tree
 * from one or the other, so this makes the whole thing
 * pretty pointless
 */

object Trees {

  object NumberSort {
    def unapply(sort: Sort): Boolean = sort match {
      case Sort("Real", List()) => true
      case Sort("Int", List()) => true
      case _ => false
    }
  }

  object EqualsSymbol {
    def unapply(s: PredicateSymbol): Boolean = s match {
      case FolT.EqualsSymbol(NumberSort()) => true
      case _ => false
    }
  }
  object Equals {
    def unapply(pApply: PredicateApplication): Option[(Term, Term)] = pApply match {
      case PredicateApplication(EqualsSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object LessThanSymbol {
    def unapply(symb: PredicateSymbol): Boolean = symb match {
      case PredicateSymbol("<", List(NumberSort(), NumberSort())) => true
      case _ => false
    }
  }
  object LessThan {
    def unapply(appli: PredicateApplication): Option[(Term, Term)] = appli match {
      case PredicateApplication(LessThanSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object LessEqualSymbol {
    def unapply(symb: PredicateSymbol): Boolean = symb match {
      case PredicateSymbol("<=", List(NumberSort(), NumberSort())) => true
      case _ => false
    }
  }
  object LessEqual {
    def unapply(appli: PredicateApplication): Option[(Term, Term)] = appli match {
      case PredicateApplication(LessEqualSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object GreaterThanSymbol {
    def unapply(symb: PredicateSymbol): Boolean = symb match {
      case PredicateSymbol(">", List(NumberSort(), NumberSort())) => true
      case _ => false
    }
  }
  object GreaterThan {
    def unapply(appli: PredicateApplication): Option[(Term, Term)] = appli match {
      case PredicateApplication(GreaterThanSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object GreaterEqualSymbol {
    def unapply(symb: PredicateSymbol): Boolean = symb match {
      case PredicateSymbol(">=", List(NumberSort(), NumberSort())) => true
      case _ => false
    }
  }
  object GreaterEqual {
    def unapply(appli: PredicateApplication): Option[(Term, Term)] = appli match {
      case PredicateApplication(GreaterEqualSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object Var {
    def unapply(v: Variable): Option[String] = v match {
      case Variable(name, NumberSort()) => Some(name)
      case _ => None
    }
  }

  object Num {
    def unapply(appli: FunctionApplication): Option[Rational] = appli match {
      case IntT.Num(i) => Some(Rational(i))
      case RealT.Num(r) => Some(r)
      case _ => None
    }
  }

  object Zero {
    def unapply(appli: FunctionApplication): Boolean = appli match {
      case Num(r) => r.isZero
      case _ => false
    }
  }
  object One {
    def unapply(appli: FunctionApplication): Boolean = appli match {
      case Num(r) => r.isOne
      case _ => false
    }
  }

  object AddSymbol {
    def unapply(symb: FunctionSymbol): Option[Int] = symb match {
      case FunctionSymbol("+", argsSort, NumberSort()) if 
        argsSort.forall{
          case NumberSort() => true
          case _ => false
        } => Some(argsSort.size)
      case _ => None
    }
  }
  object Add {
    def unapply(appli: FunctionApplication): Option[List[Term]] = appli match {
      case FunctionApplication(AddSymbol(_), ts) => Some(ts)
      case _ => None
    }
  }

  object SubSymbol {
    def unapply(symb: FunctionSymbol): Boolean = symb match {
      case FunctionSymbol("-", List(NumberSort(), NumberSort()), NumberSort()) => true
      case _ => false
    }
  }
  object Sub {
    def unapply(appli: FunctionApplication): Option[(Term, Term)] = appli match {
      case FunctionApplication(SubSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object MulSymbol {
    def unapply(symb: FunctionSymbol): Option[Int] = symb match {
      case FunctionSymbol("*", argsSort, NumberSort()) if 
        argsSort.forall{
          case NumberSort() => true
          case _ => false
        } => Some(argsSort.size)
      case _ => None
    }
  }
  object Mul {
    def unapply(appli: FunctionApplication): Option[List[Term]] = appli match {
      case FunctionApplication(MulSymbol(_), ts) => Some(ts)
      case _ => None
    }
  }

  object DivSymbol {
    def unapply(symb: FunctionSymbol): Boolean = symb match {
      case FunctionSymbol("/", List(NumberSort(), NumberSort()), NumberSort()) => true
      case _ => false
    }
  }
  object Div {
    def unapply(appli: FunctionApplication): Option[(Term, Term)] = appli match {
      case FunctionApplication(DivSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }

  object NegSymbol {
    def unapply(symb: FunctionSymbol): Boolean = symb match {
        case FunctionSymbol("-", List(NumberSort()), NumberSort()) => true
        case _ => false
    }
  }
  object Neg {
    def unapply(appli: FunctionApplication): Option[Term] = appli match {
      case FunctionApplication(NegSymbol(), List(t)) => Some(t)
      case _ => None
    }
  }

  object PowSymbol {
    def unapply(symb: FunctionSymbol): Boolean = symb match {
      case FunctionSymbol("^", List(NumberSort(), NumberSort()), NumberSort()) => true
      case _ => false
    }
  }
  object Pow {
    def unapply(appli: FunctionApplication): Option[(Term, Term)] = appli match {
      case FunctionApplication(PowSymbol(), List(t1, t2)) => Some((t1, t2))
      case _ => None
    }
  }
}
