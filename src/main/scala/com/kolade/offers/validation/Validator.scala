package com.kolade.offers.validation

import com.kolade.offers.model.FieldErrorInfo

trait Validator[T] extends (T => Seq[FieldErrorInfo]) {

  protected def validation(isValid: Boolean, fieldName: String, error: String): Option[FieldErrorInfo] = {
    if (isValid) None else Option(FieldErrorInfo(fieldName, error))
  }

}
