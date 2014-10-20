package net.atinu.dvalidation

import net.atinu.dvalidation.util.ValidationSuite
import net.atinu.dvalidation.Validator._
import scalaz._
import scalaz.syntax.validation._

class ConverterSpec extends ValidationSuite {

  test("Option can be seen as valid validation") {
    Some(1).asValidation should beValidResult(1)
  }

  test("Option can be seen as invalid validation") {
    val opt: Option[Int] = None
    opt.asValidation should beInvalidWithError(new IsNoneError())
  }

  test("Try success can be seen as valid validation") {
    scala.util.Success(1).asValidation should beValidResult(1)
  }

  test("Try failure can be seen as invalid validation") {
    val exception = new IllegalArgumentException
    scala.util.Failure(exception).asValidation should beInvalidWithError(new IsTryFailureError(exception))
  }

  test("DValidation can be seen as scalaz.ValidationNel") {
    val error = DomainErrors.withSingleError(new IsNoneError()).fail
    error.asValidationNel should equal(Failure(NonEmptyList.apply(new IsNoneError())))
  }

}