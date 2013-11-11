package regolic.smt.qfeuf

import regolic.asts.fol.Trees._
import regolic.asts.core.Trees._
import regolic.asts.core.Manip._

import org.scalatest.FunSuite

class FastCongruenceClosureSuite extends FunSuite {

  import FastCongruenceClosure._

  private val sort = Sort("A", List())
  private val f1Sym = FunctionSymbol("f1", List(sort), sort)
  private val f2Sym = FunctionSymbol("f2", List(sort, sort), sort)
  private val f3Sym = FunctionSymbol("f3", List(sort, sort, sort), sort)
  private val g1Sym = FunctionSymbol("g1", List(sort), sort)
  private val g2Sym = FunctionSymbol("g2", List(sort, sort), sort)
  private val g3Sym = FunctionSymbol("g3", List(sort, sort, sort), sort)
  private def f1(t: Term): Term = FunctionApplication(f1Sym, List(t))
  private def f2(t1: Term, t2: Term): Term = FunctionApplication(f2Sym, List(t1, t2))
  private def f3(t1: Term, t2: Term, t3: Term): Term = FunctionApplication(f3Sym, List(t1, t2, t3))
  private def g1(t: Term): Term = FunctionApplication(g1Sym, List(t))
  private def g2(t1: Term, t2: Term): Term = FunctionApplication(g2Sym, List(t1, t2))
  private def g3(t1: Term, t2: Term, t3: Term): Term = FunctionApplication(g3Sym, List(t1, t2, t3))

  private val x = Variable("v", sort)
  private val y = Variable("v", sort)
  private val z = Variable("v", sort)

  private val aSym = FunctionSymbol("a", List(), sort)
  private val bSym = FunctionSymbol("b", List(), sort)
  private val cSym = FunctionSymbol("c", List(), sort)
  private val a = FunctionApplication(aSym, List())
  private val b = FunctionApplication(bSym, List())
  private val c = FunctionApplication(cSym, List())

  test("basic merge") {
    val cc1 = new FastCongruenceClosure
    cc1.initialize(3)
    assert(!cc1.areCongruent(Constant(0), Constant(1)))
    assert(!cc1.areCongruent(Constant(1), Constant(2)))
    assert(!cc1.areCongruent(Constant(0), Constant(2)))
    cc1.merge(0, 1)
    assert(cc1.areCongruent(Constant(0), Constant(1)))
    cc1.merge(1, 2)
    assert(cc1.areCongruent(Constant(1), Constant(2)))
    assert(cc1.areCongruent(Constant(0), Constant(2)))
    assert(cc1.areCongruent(Constant(2), Constant(0)))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(5)
    assert(!cc2.areCongruent(Constant(0), Constant(1)))
    assert(!cc2.areCongruent(Constant(1), Constant(2)))
    assert(!cc2.areCongruent(Constant(0), Constant(2)))
    assert(!cc2.areCongruent(Constant(2), Constant(4)))
    cc2.merge(0, 1)
    assert(cc2.areCongruent(Constant(0), Constant(1)))
    cc2.merge(3, 2)
    assert(!cc2.areCongruent(Constant(1), Constant(2)))
    assert(!cc2.areCongruent(Constant(0), Constant(2)))
    assert(!cc2.areCongruent(Constant(2), Constant(4)))
    assert(cc2.areCongruent(Constant(2), Constant(3)))

    cc2.merge(0, 4)
    assert(cc2.areCongruent(Constant(0), Constant(4)))
    assert(cc2.areCongruent(Constant(1), Constant(4)))
    assert(!cc2.areCongruent(Constant(0), Constant(2)))
    assert(!cc2.areCongruent(Constant(2), Constant(4)))

    cc2.merge(3, 4)
    assert(cc2.areCongruent(Constant(0), Constant(4)))
    assert(cc2.areCongruent(Constant(1), Constant(4)))
    assert(cc2.areCongruent(Constant(0), Constant(2)))
    assert(cc2.areCongruent(Constant(2), Constant(4)))
    assert(cc2.areCongruent(Constant(3), Constant(1)))
    assert(cc2.areCongruent(Constant(3), Constant(4)))
  }

  test("merge with apply") {
    val cc1 = new FastCongruenceClosure
    cc1.initialize(4)
    cc1.merge(0, 1, 2) //g(a) = b
    assert(!cc1.areCongruent(Constant(0), Constant(1)))
    assert(!cc1.areCongruent(Constant(0), Constant(2)))
    assert(cc1.areCongruent(Apply(Constant(0), Constant(1)), Constant(2))) //assert g(a) = b
    cc1.merge(2, 3) // b = c
    assert(cc1.areCongruent(Apply(Constant(0), Constant(1)), Constant(3))) //assert g(a) = c
    assert(!cc1.areCongruent(Constant(0), Constant(1)))
    assert(!cc1.areCongruent(Constant(0), Constant(2)))
    assert(!cc1.areCongruent(Constant(0), Constant(3)))
    assert(!cc1.areCongruent(Constant(1), Constant(2)))
    assert(!cc1.areCongruent(Constant(1), Constant(3)))
    cc1.merge(0, 1, 3) //g(a) = c
    assert(!cc1.areCongruent(Constant(0), Constant(1)))
    assert(!cc1.areCongruent(Constant(0), Constant(2)))
    assert(!cc1.areCongruent(Constant(0), Constant(3)))
    assert(!cc1.areCongruent(Constant(1), Constant(2)))
    assert(!cc1.areCongruent(Constant(1), Constant(3)))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(4) //f, a, b, c
    cc2.merge(0, 1, 2) //f(a) = b
    assert(!cc2.areCongruent(Constant(2), Constant(3))) // b != c
    cc2.merge(0, 1, 3) //f(a) = c
    assert(cc2.areCongruent(Constant(2), Constant(3))) // b = c

    val cc3 = new FastCongruenceClosure
    cc3.initialize(5) //f, a, b, c, d
    cc3.merge(0, 1, 2) //f(a) = b
    cc3.merge(0, 2, 3) //f(f(a)) = c
    cc3.merge(0, 3, 1) //f(f(f(a))) = a
    assert(cc3.areCongruent(Apply(Constant(0), Apply(Constant(0), Apply(Constant(0), Constant(1)))), Constant(1)))
    assert(!cc3.areCongruent(Apply(Constant(0), Apply(Constant(0), Constant(1))), Constant(1)))
  }

  test("simple explain") {
    val cc1 = new FastCongruenceClosure
    cc1.initialize(3)
    cc1.merge(0, 1)
    val ex1 = cc1.explain(0, 1)
    assert(ex1.size === 1)
    assert(ex1.head === Left((0, 1)))
    cc1.merge(1,2)
    val ex2 = cc1.explain(1, 2)
    assert(ex2.size === 1)
    assert(ex2.head === Left((1, 2)))
    val ex3 = cc1.explain(0, 2)
    assert(ex3.size === 2)
    assert(ex3.contains(Left((1, 2))))
    assert(ex3.contains(Left((0, 1))))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(3)
    cc2.merge(1, 0)
    val ex4 = cc2.explain(0, 1)
    assert(ex4.size === 1)
    assert(ex4.head === Left((1, 0)))
    cc2.merge(1,2)
    val ex5 = cc2.explain(1, 2)
    assert(ex5.size === 1)
    assert(ex5.head === Left((1, 2)))
    val ex6 = cc2.explain(0, 2)
    assert(ex6.size === 2)
    assert(ex6.contains(Left((1, 2))))
    assert(ex6.contains(Left((1, 0))))

    val cc3 = new FastCongruenceClosure
    cc3.initialize(4)
    cc3.merge(1, 0)
    cc3.merge(2, 3)
    val ex7 = cc3.explain(3, 2)
    assert(ex7.size === 1)
    assert(ex7.head === Left((2, 3)))
    cc3.merge(1, 2)
    val ex8 = cc3.explain(0, 2)
    assert(ex8.size === 2)
    assert(ex8.contains(Left((1, 2))))
    assert(ex8.contains(Left((1, 0))))
    val ex9 = cc3.explain(1, 3)
    assert(ex9.size === 2)
    assert(ex9.contains(Left((1, 2))))
    assert(ex9.contains(Left((2, 3))))
    val ex10 = cc3.explain(0, 3)
    assert(ex10.size === 3)
    assert(ex10.contains(Left((1, 0))))
    assert(ex10.contains(Left((2, 3))))
    assert(ex10.contains(Left((1, 2))))
  }

  test("explain with apply") {
    val cc1 = new FastCongruenceClosure
    cc1.initialize(4)
    cc1.merge(0, 1, 2) //f(a) = b
    cc1.merge(0, 1, 3) //f(a) = c
    val ex1 = cc1.explain(2, 3)
    assert(ex1.size == 2)
    assert(ex1.contains(Right((0, 1, 2))))
    assert(ex1.contains(Right((0, 1, 3))))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(5)
    cc2.merge(0, 1, 3) //f(a) = c
    cc2.merge(0, 2, 4) //f(b) = d
    cc2.merge(1, 2) //a = b
    val ex2 = cc2.explain(3, 4)
    assert(ex2.size == 3)
    assert(ex2.contains(Left((1, 2))))
    assert(ex2.contains(Right((0, 1, 3))))
    assert(ex2.contains(Right((0, 2, 4))))

  }

  test("positive setTrue") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, true, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    assert(!cc1.isTrue(lit1))
    cc1.setTrue(lit1)
    assert(cc1.isTrue(lit1))
    assert(!cc1.isTrue(lit2))
    cc1.setTrue(lit2)
    assert(cc1.isTrue(lit1))
    assert(cc1.isTrue(lit2))
    assert(cc1.isTrue(lit3))
  }

  test("negative setTrue") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, true, null)
    val lit4 = Literal(Left(0, 1), 0, false, null)
    val lit5 = Literal(Left(1, 2), 0, false, null)
    val lit6 = Literal(Left(0, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc1.setTrue(lit1)
    assert(cc1.isTrue(lit1))
    assert(!cc1.isTrue(lit2))
    assert(!cc1.isTrue(lit4))
    cc1.setTrue(lit2)
    assert(cc1.isTrue(lit1))
    assert(cc1.isTrue(lit2))
    assert(cc1.isTrue(lit3))
    assert(!cc1.isTrue(lit4))
    assert(!cc1.isTrue(lit5))
    assert(!cc1.isTrue(lit6))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(3, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc2.setTrue(lit4)
    assert(cc2.isTrue(lit4))
    assert(!cc2.isTrue(lit1))
    cc2.setTrue(lit2)
    assert(cc2.isTrue(lit2))
    assert(cc2.isTrue(lit4))
    assert(cc2.isTrue(lit6))
    assert(!cc2.isTrue(lit3))
  }

  test("setTrue InconsistencyException") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    cc1.setTrue(lit3)
    cc1.setTrue(lit1)
    intercept[InconsistencyException]{cc1.setTrue(lit2)}

    val cc2 = new FastCongruenceClosure
    cc2.initialize(3, Set(lit1, lit2, lit3))
    cc2.setTrue(lit1)
    cc2.setTrue(lit3)
    intercept[InconsistencyException]{cc2.setTrue(lit2)}

    val cc3 = new FastCongruenceClosure
    cc3.initialize(3, Set(lit1, lit2, lit3))
    cc3.setTrue(lit1)
    cc3.setTrue(lit2)
    intercept[InconsistencyException]{cc3.setTrue(lit3)}

    val lit4 = Literal(Left(2, 3), 0, true, null)
    val lit5 = Literal(Left(0, 1), 0, false, null)

    val cc4 = new FastCongruenceClosure
    cc4.initialize(5, Set(lit1, lit2, lit3, lit4, lit5))
    cc4.merge(4, 2, 0) //f(c) = a
    cc4.merge(4, 3, 1) //f(d) = b
    cc4.setTrue(lit4)
    intercept[InconsistencyException]{cc4.setTrue(lit5)}
    
    val cc5 = new FastCongruenceClosure
    cc5.initialize(5, Set(lit1, lit2, lit3, lit4, lit5))
    cc5.merge(4, 2, 0) //f(c) = a
    cc5.merge(4, 3, 1) //f(d) = b
    cc5.setTrue(lit5)
    intercept[InconsistencyException]{cc5.setTrue(lit4)}
  }

  test("advanced setTrue") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(2, 3), 0, true, null)
    val lit3 = Literal(Left(0, 3), 0, false, null)
    val lit4 = Literal(Left(1, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(4, Set(lit1, lit2, lit3, lit4))
    cc1.setTrue(lit1)
    cc1.setTrue(lit2)
    assert(!cc1.isTrue(lit4))
    assert(!cc1.isTrue(lit3))
    cc1.setTrue(lit3)
    assert(cc1.isTrue(lit4))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(4, Set(lit1, lit2, lit3, lit4))
    cc2.setTrue(lit3)
    assert(cc2.isTrue(lit3))
    assert(!cc2.isTrue(lit1))
    assert(!cc2.isTrue(lit2))
    cc2.setTrue(lit1)
    cc2.setTrue(lit2)
    assert(cc2.isTrue(lit3))
    assert(cc2.isTrue(lit4))

    val lit5 = Literal(Left(1, 3), 0, true, null)
    val lit6 = Literal(Left(0, 2), 0, true, null)

    val cc3 = new FastCongruenceClosure
    cc3.initialize(4, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc3.setTrue(lit1)
    cc3.setTrue(lit3)
    cc3.setTrue(lit4)
    intercept[InconsistencyException]{ cc3.setTrue(lit5) }
    intercept[InconsistencyException]{ cc3.setTrue(lit6) }
  }

  test("setTrue basic theory propagation") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, true, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    val csq1 = cc1.setTrue(lit1)
    assert(csq1.isEmpty)
    val csq2 = cc1.setTrue(lit2)
    assert(csq2.size === 1)
    assert(csq2.contains(lit3))

    val lit4 = Literal(Left(2, 3), 0, true, null)
    val lit5 = Literal(Left(0, 3), 0, true, null)
    val lit6 = Literal(Left(1, 3), 0, true, null)
    val cc2 = new FastCongruenceClosure
    cc2.initialize(4, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    val csq3 = cc2.setTrue(lit1)
    assert(csq3.isEmpty)
    val csq4 = cc2.setTrue(lit4)
    assert(csq4.isEmpty)
    val csq5 = cc2.setTrue(lit2)
    assert(csq5.size === 3)
    assert(csq5.contains(lit5))
    assert(csq5.contains(lit3))
    assert(csq5.contains(lit6))
  }

  test("setTrue theory propagation of negative literals") {
    val lit1 = Literal(Left(0, 1), 0, false, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    val csq1 = cc1.setTrue(lit1)
    assert(csq1.isEmpty)
    val csq2 = cc1.setTrue(lit2)
    assert(csq2.size === 1)
    assert(csq2.contains(lit3))

    val lit4 = Literal(Left(2, 3), 0, true, null)
    val lit5 = Literal(Left(1, 3), 0, true, null)
    val lit6 = Literal(Left(0, 3), 0, false, null)
    val cc2 = new FastCongruenceClosure
    cc2.initialize(4, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    val csq3 = cc2.setTrue(lit1)
    assert(csq3.isEmpty)
    val csq4 = cc2.setTrue(lit4)
    assert(csq4.isEmpty)
    val csq5 = cc2.setTrue(lit2)
    assert(csq5.size === 3)
    assert(csq5.contains(lit3))
    assert(csq5.contains(lit5))
    assert(csq5.contains(lit6))
  }

  test("negative setTrue theory propagation") {
    val lit1 = Literal(Left(1, 2), 0, true, null)
    val lit2 = Literal(Left(0, 1), 0, false, null)
    val lit3 = Literal(Left(0, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    val csq1 = cc1.setTrue(lit1)
    assert(csq1.isEmpty)
    val csq2 = cc1.setTrue(lit2)
    assert(csq2.size === 1)
    assert(csq2.contains(lit3))
  }

  test("setTrue with apply") {
    val lit1 = Literal(Left(1, 2), 0, true, null)
    val lit2 = Literal(Left(3, 4), 0, true, null)
    val lit3 = Literal(Left(1, 3), 0, true, null)
    val lit4 = Literal(Left(2, 4), 0, true, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(5, Set(lit1, lit2, lit3, lit4))
    cc1.merge(0, 1, 3) //f(a) = b
    cc1.merge(0, 2, 4) //f(c) = d

    val csq1 = cc1.setTrue(lit1)
    assert(csq1.size === 1)
    assert(csq1.contains(lit2))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(5, Set(lit1, lit2, lit3, lit4))
    cc2.merge(0, 1, 3) //f(a) = b
    cc2.merge(0, 2, 4) //f(c) = d

    val csq2 = cc2.setTrue(lit2)
    assert(csq2.size === 0)
  }

  test("basic explanation") {
    val lit1 = Literal(Left(1, 2), 0, true, null)
    val lit2 = Literal(Left(0, 1), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, true, null)
    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    cc1.setTrue(lit1)
    cc1.setTrue(lit2)
    val expl1 = cc1.explanation(lit3)
    assert(expl1.size === 2)
    assert(expl1.contains(lit1))
    assert(expl1.contains(lit2))

    val lit4 = Literal(Left(2, 3), 0, true, null)
    val lit5 = Literal(Left(0, 3), 0, true, null)
    val cc2 = new FastCongruenceClosure
    cc2.initialize(4, Set(lit1, lit2, lit3, lit4, lit5))
    cc2.setTrue(lit2)
    cc2.setTrue(lit4)
    cc2.setTrue(lit1)
    val expl2 = cc2.explanation(lit5)
    assert(expl2.size === 3)
    assert(expl2.contains(lit2))
    assert(expl2.contains(lit4))
    assert(expl2.contains(lit1))

    val lit6 = Literal(Left(0, 4), 0, true, null)
    val cc3 = new FastCongruenceClosure
    cc3.initialize(5, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc3.setTrue(lit2)
    cc3.setTrue(lit6) //add irrelevant literal in explanation
    cc3.setTrue(lit4)
    cc3.setTrue(lit1)
    val expl3 = cc3.explanation(lit5)
    assert(expl3.size === 3)
    assert(expl3.contains(lit2))
    assert(expl3.contains(lit4))
    assert(expl3.contains(lit1))
    assert(!expl3.contains(lit6)) //explanation should not contains lit6
  }

  test("explanation with apply basic") {

    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(2, 3), 0, true, null)
    val lit3 = Literal(Left(1, 2), 0, true, null)
    val lit4 = Literal(Left(0, 3), 0, true, null)
    val lit5 = Literal(Right(4, 0, 2), 0, true, null)
    val lit6 = Literal(Right(4, 1, 3), 0, true, null)
    val cc1 = new FastCongruenceClosure
    cc1.initialize(5, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc1.merge(4, 0, 2) //TODO: should be passed via setTrue maybe ?
    cc1.merge(4, 1, 3)
    cc1.setTrue(lit1)
    val expl1 = cc1.explanation(lit2)
    assert(expl1.size === 3)
    assert(expl1.contains(lit1))
    assert(expl1.contains(lit5))
    assert(expl1.contains(lit6))

    val lit7 = Literal(Left(0,5), 0, true, null)
    val cc2 = new FastCongruenceClosure
    cc2.initialize(6, Set(lit1, lit2, lit3, lit4, lit5, lit6, lit7))
    cc2.merge(4, 0, 2)
    cc2.merge(4, 1, 3)
    cc2.setTrue(lit7)
    cc2.setTrue(lit1)
    val expl2 = cc2.explanation(lit2)
    assert(expl2.size === 3)
    assert(expl2.contains(lit1))
    assert(expl2.contains(lit5))
    assert(expl2.contains(lit6))

  }

  test("explanation with apply advanced") {
    val lit1 = Literal(Left(2, 3), 0, true, null) //c = d
    val lit2 = Literal(Left(4, 2), 0, true, null) //e = c
    val lit3 = Literal(Left(4, 1), 0, true, null) //e = b
    val lit4 = Literal(Left(1, 5), 0, true, null) //b = f
    val lit5 = Literal(Left(0, 1), 0, true, null) //a = b
    val lit6 = Literal(Right(6, 5, 3), 0, true, null) //g(f) = d
    val lit7 = Literal(Right(6, 5, 3), 0, true, null) //g(d) = a

    val cc1 = new FastCongruenceClosure
    cc1.initialize(7, Set(lit1, lit2, lit3, lit4, lit5))
    cc1.merge(6, 5, 3) //g(f) = d
    cc1.merge(6, 3, 0) //g(d) = a
    cc1.setTrue(lit1)
    cc1.setTrue(lit2)
    cc1.setTrue(lit3)
    cc1.setTrue(lit4)
    val expl1 = cc1.explanation(lit5)
    assert(expl1.size == 6)
    assert(expl1.contains(lit1))
    assert(expl1.contains(lit2))
    assert(expl1.contains(lit3))
    assert(expl1.contains(lit4))
    assert(expl1.contains(lit6))
    assert(expl1.contains(lit7))
  }

  test("explanation of negative setTrue") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, false, null)
    val lit3 = Literal(Left(0, 2), 0, false, null)
    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3))
    cc1.setTrue(lit1)
    cc1.setTrue(lit2)
    val expl1 = cc1.explanation(lit3)
    assert(expl1.size === 2)
    assert(expl1.contains(lit1))
    assert(expl1.contains(lit2))

    val lit4 = Literal(Left(2, 3), 0, true, null)
    val lit5 = Literal(Left(0, 3), 0, false, null)
    val cc2 = new FastCongruenceClosure
    cc2.initialize(4, Set(lit1, lit2, lit3, lit4, lit5))
    cc2.setTrue(lit1)
    cc2.setTrue(lit4)
    cc2.setTrue(lit2)
    val expl2 = cc2.explanation(lit5)
    assert(expl2.size === 3)
    assert(expl2.contains(lit1))
    assert(expl2.contains(lit4))
    assert(expl2.contains(lit2))

    val lit6 = Literal(Left(0, 4), 0, true, null)
    val cc3 = new FastCongruenceClosure
    cc3.initialize(5, Set(lit1, lit2, lit3, lit4, lit5, lit6))
    cc3.setTrue(lit1)
    cc3.setTrue(lit6) //add irrelevant literal in explanation
    cc3.setTrue(lit4)
    cc3.setTrue(lit2)
    val expl3 = cc3.explanation(lit5)
    assert(expl3.size === 3)
    assert(expl3.contains(lit1))
    assert(expl3.contains(lit4))
    assert(expl3.contains(lit2))
    assert(!expl3.contains(lit6)) //explanation should not contains lit6
  }

  test("backtrack basic") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(0, 2), 0, true, null)
    val lit4 = Literal(Left(0, 1), 0, false, null)
    val lit5 = Literal(Left(0, 2), 0, false, null)

    val cc1 = new FastCongruenceClosure
    cc1.initialize(3, Set(lit1, lit2, lit3, lit4, lit5))
    cc1.setTrue(lit1)
    cc1.backtrack(1)
    cc1.setTrue(lit4)
    val csq1 = cc1.setTrue(lit2)
    assert(csq1.size === 1)
    assert(csq1.contains(lit5))
    assert(cc1.isTrue(lit5))
    assert(cc1.isTrue(lit4))
    assert(!cc1.isTrue(lit1))

    val cc2 = new FastCongruenceClosure
    cc2.initialize(3, Set(lit1, lit2, lit3, lit4, lit5))
    cc2.setTrue(lit1)
    cc2.setTrue(lit2)
    cc2.backtrack(2)
    cc2.setTrue(lit4)
    val csq2 = cc2.setTrue(lit2)
    assert(csq2.size === 1)
    assert(csq2.contains(lit5))
    assert(cc2.isTrue(lit5))
    assert(!cc2.isTrue(lit1))
    assert(cc2.isTrue(lit2))

    val lit6 = Literal(Left(2, 3), 0, true, null)
    val lit7 = Literal(Left(1, 3), 0, false, null)
    val cc3 = new FastCongruenceClosure
    cc3.initialize(4, Set(lit1, lit2, lit3, lit4, lit5, lit6, lit7))
    cc3.setTrue(lit1)
    cc3.setTrue(lit6)
    cc3.setTrue(lit2)
    cc3.backtrack(1)
    cc3.setTrue(lit5)
    cc3.isTrue(lit7)

    val lit8 = Literal(Left(3, 4), 0, true, null)
    val cc4 = new FastCongruenceClosure
    cc4.initialize(5, Set(lit1, lit2, lit3, lit4, lit5, lit6, lit7, lit8))
    cc4.setTrue(lit1)
    cc4.setTrue(lit5)
    intercept[InconsistencyException]{ cc4.setTrue(lit2) }
    cc4.backtrack(2)
    cc4.setTrue(lit8)
    cc4.setTrue(lit2)
    assert(cc4.isTrue(lit3))

    val cc5 = new FastCongruenceClosure
    cc5.initialize(2, Set(lit1))
    assert(!cc5.isTrue(lit1))
    cc5.setTrue(lit1)
    assert(cc5.isTrue(lit1))
    cc5.backtrack(1)
    assert(!cc5.isTrue(lit1))

  }

  test("backtrack with apply") {
    val lit1 = Literal(Left(0, 1), 0, true, null)
    val lit2 = Literal(Left(1, 2), 0, true, null)
    val lit3 = Literal(Left(2, 3), 0, true, null)
    val lit4 = Literal(Left(0, 3), 0, true, null) //a = d
    val lit9 = Literal(Left(5, 2), 0, false, null) //f != c
    val cc5 = new FastCongruenceClosure
    cc5.initialize(7)
    cc5.merge(6, 4, 1) //g(e) = b
    cc5.merge(6, 4, 5) //g(e) = f
    cc5.merge(6, 5, 1) //g(f) = b
    cc5.merge(6, 5, 2) //g(f) = c
    intercept[InconsistencyException]{ cc5.setTrue(lit9) }
    cc5.backtrack(1)
    cc5.setTrue(lit4)
    assert(cc5.isTrue(lit4))
    assert(!cc5.isTrue(lit9))
  }

  //test("backtrack 4") {
  //  val x0 = freshVariable("x", IntSort());
  //  val x1 = freshVariable("x", IntSort());
  //  val y0 = freshVariable("y", IntSort());

  //  val diamond = List[Formula](
  //    Not(Equals(x0, x1)),
  //    Equals(x0, y0),
  //    Equals(y0, x1)
  //  )

  //  val afterBacktracking = List[Formula](
  //    Equals(x0, x1)
  //  )

  //  val cc = new CongruenceClosure
  //  val dSet = diamond.toSet
  //  cc.initialize(dSet)
  //  val results = diamond.map(eq => cc.setTrue(eq))
  //  assert(results.reverse.tail.forall(_ != None))
  //  assert(results.reverse.head == None)

  //  cc.backtrack(2)

  //  val resultsAfterBacktracking = afterBacktracking.map(eq => cc.setTrue(eq))
  //  assert(resultsAfterBacktracking.exists(_ == None))
  //          
  //}
}