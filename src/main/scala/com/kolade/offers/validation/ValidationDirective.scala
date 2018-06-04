package com.kolade.offers.validation

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.kolade.offers.CustomJsonSupport
import com.kolade.offers.model.{FieldErrorInfo, ValidationRejection}
import spray.json.DefaultJsonProtocol

object ValidationDirective extends DefaultJsonProtocol with SprayJsonSupport with CustomJsonSupport {

  def validateModel[T](model: T)(implicit validator: Validator[T]): Directive1[T] = {
    validator(model) match {
      case Nil => provide(model)
      case errors: Seq[FieldErrorInfo] => reject(ValidationRejection(errors))
    }
  }

}
