package com.atinu.dvalidation

import scalaz._
import scalaz.NonEmptyList._
import scalaz.syntax.validation._

object Validator {

  import DomainErrors._

  def notEmpty(s: String): DValidation[String] =
    if (s.isEmpty) new IsEmptyStringError(s).invalid else s.valid

  def hasElements[T <: Traversable[_]](s: T): DValidation[T] =
    if (s.isEmpty) new IsEmptySeqError(s).invalid else s.valid

  def isSome[T <: Option[_]](s: T): DValidation[T] =
    if (s.isEmpty) new IsNoneError(s).invalid else s.valid

  def ensure[T](s: T)(key: String, args: Any*)(v: T => Boolean): DValidation[T] =
    if (v(s)) s.success else new CustomValidationError(s, key, args.map(_.toString)).invalid

  def validSequence[T](seq: Traversable[T], validator: DValidator[T]): IndexedSeq[DValidation[T]] = {
    seq.toIndexedSeq.zipWithIndex.map { case (value, idx) =>
      withPath(validator(value), s"[$idx]")
    }
  }

  def validate[T](value: T)(cond: T => Boolean)(error: => DomainError): DValidation[T] =
    if(cond(value)) value.valid else error.invalid

  def template[T](v: DValidator[T]):DValidator[T] = v
}

object DomainErrors {

  type DValidation[T] = Validation[DomainErrors, T]
  type DValidator[T] = T => DValidation[T]

  def invalid[T](value: Any, key: String) = new CustomValidationError(value, key).invalid[T]

  def invalid[T](value: Any, key: String, args: String*) = new CustomValidationError(value, key, args.toSeq).invalid[T]
  
  def valid[T](value: T): DValidation[T] = value.valid


  def doValidation[T](validations: Seq[DValidation[_]], value: T): DValidation[T] = {
    val validValue = valid(value)
    validateAll(validations, validValue)
  }

  def validateAll[T](validations: Seq[DValidation[_]], validValue: DValidation[T]): DValidation[T] = {
    import syntax.semigroup._
    validations.foldLeft(validValue) {
      case (Success(_), Success(_)) => validValue
      case (Success(_), e@Failure(_)) => e.asInstanceOf[DValidation[T]]
      case (Failure(e1), Failure(e2)) => (e1 |+| e2).fail
      case (e@Failure(_), Success(_)) => e.asInstanceOf[DValidation[T]]
    }
  }

  implicit class ErrorToErrors(val error: DomainError) extends AnyVal {
    def invalid[T] = new DomainErrors(NonEmptyList.apply(error)).fail[T]
  }

  implicit class tToSuccess[T](val value: T) extends AnyVal {
    def valid: DValidation[T] = value.success[DomainErrors]
  }

  implicit class tToValidation[T](val value: T) extends AnyVal {
    def validateWith(validations: DValidation[_]*): DValidation[T] = {
      doValidation(validations, value)
    }
  }

  implicit class dSeqValidation[T](val value: IndexedSeq[DValidation[T]]) extends AnyVal {
    def forAttribute(attr: Symbol): IndexedSeq[DValidation[T]] = {
       value.map(validation => withPath(validation, attr.name))
    }
  }

  def withPath[T](value: DValidation[T], path: String) = {
    value.leftMap(domainErrors => domainErrors.copy(errors =
      domainErrors.errors.map(e => e.nestPath(path))))
  }

  implicit class dValFirstSuccess[T](val value: DValidation[T]) extends AnyVal {

    def isValidOr[R <: T](next: => DValidation[R]) = value.findSuccess(next)

    def forAttribute(attr: Symbol): DValidation[T] = {
      val name = attr.name
      withPath(value, name)
    }

    def errorView = value.fold(Option.apply, _ => None)

    def withValidations(validations: IndexedSeq[DValidation[_]]) =
      validateAll(validations.toSeq, value)
  }

  implicit def errorsSemiGroup: Semigroup[DomainErrors] =
    new Semigroup[DomainErrors] {
      def append(f1: DomainErrors, f2: => DomainErrors): DomainErrors = {
        val errors = Semigroup[NonEmptyList[DomainError]].append(f1.errors, f2.errors)
        new DomainErrors(errors)
      }
    }
}

case class DomainErrors(errors: NonEmptyList[DomainError]) {
  override def toString = errors.list.mkString(",")
  def prettyPrint = errors.list.mkString("-->\n", "\n", "\n<--")
}

trait DomainError {
  def value: Any
  def msgKey: String
  def path: String
  def nestPath(segment: String): DomainError = {
    val newPath =
      if(path == "/") s"/$segment"
      else s"/$segment$path"
    copyWithPath(newPath)
  }
  def copyWithPath(path: String): DomainError
}

abstract class AbstractDomainError(valueP: Any, msgKeyP: String, pathP: String = "/", argsP: Seq[String] = Nil) extends DomainError {
  def value = valueP
  def msgKey = msgKeyP
  def path = pathP
  def args = argsP

  private def argsString = if(args.isEmpty) "" else s", args: ${args.mkString(",")}"

  override def toString = s"""DomainError(path: $path, value: $value, msgKey: $msgKey$argsString)"""
}

class IsEmptyStringError(value: String, path: String = "/") extends AbstractDomainError(value, "error.dvalidation.emptyString", path) {
   def copyWithPath(path: String): IsEmptyStringError = new IsEmptyStringError(value, path)
}

class IsEmptySeqError(value: Traversable[_], path: String = "/") extends AbstractDomainError(value, "error.dvalidation.emptySeq", path) {

   def copyWithPath(path: String): IsEmptySeqError = new IsEmptySeqError(value, path)
}

class IsNoneError(value: Option[_], path: String = "/") extends AbstractDomainError(value, "error.dvalidation.isNone", path) {

   def copyWithPath(path: String): IsNoneError = new IsNoneError(value, path)
}

class CustomValidationError(value: Any, key: String, args: Seq[String] = Nil, path: String = "/") extends AbstractDomainError(value, key, path, args) {

   def copyWithPath(path: String): CustomValidationError = new CustomValidationError(value, key, args, path)
}

