package net.atinu.dvalidation

import net.atinu.dvalidation.Path._

package object errors {

  /**
   * A base class which can be used to define a custom [[DomainError]]
   */
  abstract class AbstractDomainError(valueP: Any, msgKeyP: String, pathP: PathString = Path.SingleSlash, argsP: Seq[String] = Nil) extends DomainError {
    def value = valueP

    def msgKey = msgKeyP

    def path = pathP

    def args = argsP

    def nest(path: PathString): DomainError = {
      nestIntern(Path.unwrap(path).tail)
    }

    def nestIndex(index: Int): DomainError = {
      nestIntern(s"[$index]")
    }

    def nestAttribute(segment: Symbol): DomainError = {
      nestIntern(segment.name)
    }

    private def nestIntern(seg: String): DomainError = {
      val newPath = Path.unwrap(path) match {
        case "/" => s"/$seg"
        case _ => s"/$seg$path"
      }
      copyWithPath(Path.wrapInternal(newPath))
    }

    private def argsString = if (args.isEmpty) "" else s", args: ${args.mkString(",")}"

    override def toString = s"""DomainError(path: $path, value: $value, msgKey: $msgKey$argsString)"""

    override def equals(value: Any) = value match {
      case v: AbstractDomainError if v.getClass == this.getClass =>
        v.value == this.value &&
          v.msgKey == this.msgKey &&
          v.path == this.path &&
          v.args == this.args
      case _ => false
    }

    override def hashCode(): Int =
      41 * (
        41 * (
          41 * (
            41 + value.hashCode) + msgKey.hashCode) + Path.unwrap(path).hashCode) + args.hashCode
  }

  class IsNotEqualError(valueExpected: Any, value: Any, path: PathString = Path.SingleSlash) extends AbstractDomainError(value, "error.dvalidation.notEqual", path, Seq(valueExpected.toString)) {
    def copyWithPath(path: PathString) = new IsNotEqualError(valueExpected, value, path)
  }

  class IsEmptyStringError(path: PathString = Path.SingleSlash) extends AbstractDomainError("", "error.dvalidation.emptyString", path) {
    def copyWithPath(path: PathString) = new IsEmptyStringError(path)
  }

  class IsZeroError(value: Any, path: PathString = Path.SingleSlash) extends AbstractDomainError(value, "error.dvalidation.notEqual", path) {
    def copyWithPath(path: PathString) = new IsZeroError(value, path)
  }

  class IsEmptySeqError(path: PathString = Path.SingleSlash) extends AbstractDomainError(Nil, "error.dvalidation.emptySeq", path) {

    def copyWithPath(path: PathString) = new IsEmptySeqError(path)
  }

  class IsNoneError(path: PathString = Path.SingleSlash) extends AbstractDomainError(None, "error.dvalidation.isNone", path) {

    def copyWithPath(path: PathString) = new IsNoneError(path)
  }

  class IsTryFailureError(value: Throwable, path: PathString = Path.SingleSlash) extends AbstractDomainError(value, "error.dvalidation.isTryFailue", path) {

    def copyWithPath(path: PathString) = new IsTryFailureError(value, path)
  }

  class IsNotGreaterThenError(valueMin: Any, value: Any, isInclusive: Boolean, path: PathString = Path.SingleSlash)
    extends AbstractDomainError(value, "error.dvalidation.notGreaterThen", path, Seq(valueMin.toString, isInclusive.toString)) {

    def copyWithPath(path: PathString) = new IsNotGreaterThenError(valueMin, value, isInclusive, path)
  }

  class IsNotLowerThenError(valueMax: Any, value: Any, isInclusive: Boolean, path: PathString = Path.SingleSlash)
    extends AbstractDomainError(value, "error.dvalidation.notSmallerThen", path, Seq(valueMax.toString, isInclusive.toString)) {

    def copyWithPath(path: PathString) = new IsNotLowerThenError(valueMax, value, isInclusive, path)
  }

  sealed trait WrongSizeError extends DomainError

  class IsToSmallError(valueMin: Any, value: Any, path: PathString = Path.SingleSlash)
    extends AbstractDomainError(value, "error.dvalidation.tooSmallError", path, Seq(valueMin.toString)) with WrongSizeError {

    def copyWithPath(path: PathString) = new IsToSmallError(valueMin, value, path)
  }

  class IsToBigError(valueMax: Any, value: Any, path: PathString = Path.SingleSlash)
    extends AbstractDomainError(value, "error.dvalidation.tooBigError", path, Seq(valueMax.toString)) with WrongSizeError {

    def copyWithPath(path: PathString) = new IsToBigError(valueMax, value, path)
  }

  object CustomValidationError {
    def apply(value: Any, key: String, args: String*) = new CustomValidationError(value, key, args.toSeq)

    def withKey(e: DomainError, msgKey: String) = new CustomValidationError(e.value, msgKey, e.args, e.path)
  }

  class CustomValidationError(value: Any, key: String, args: Seq[String] = Nil, path: PathString = Path.SingleSlash) extends AbstractDomainError(value, key, path, args) {

    def copyWithPath(path: PathString) = new CustomValidationError(value, key, args, path)
  }

}
