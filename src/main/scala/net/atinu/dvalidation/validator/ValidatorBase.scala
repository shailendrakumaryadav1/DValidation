package net.atinu.dvalidation.validator

import net.atinu.dvalidation.{ DValidation, ErrorMap, DomainError }

trait ValidatorBase {

  protected def failMapped[A, T <: DomainError](err: T)(implicit me: ErrorMap[T]): DValidation[A] = me(err).invalid

}
