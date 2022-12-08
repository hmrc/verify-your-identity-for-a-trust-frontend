/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call
import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration,
                                  servicesConfig: ServicesConfig,
                                  contactFrontendConfig: ContactFrontendConfig) {

  final val ENGLISH = "en"
  final val WELSH = "cy"

  lazy val appName: String = configuration.get[String]("appName")

  val betaFeedbackUrl = s"${contactFrontendConfig.baseUrl.get}/contact/beta-feedback?service=${contactFrontendConfig.serviceId.get}"

  lazy val authUrl: String = servicesConfig.baseUrl("auth")
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val trustsContinueUrl: String = configuration.get[String]("urls.maintainContinue")

  lazy val trustsRegistration: String = configuration.get[String]("urls.trustsRegistration")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val trustsStoreUrl: String = servicesConfig.baseUrl("trusts-store")

  lazy val relationshipEstablishmentUrl: String = servicesConfig.baseUrl("relationship-establishment")

  lazy val relationshipName: String =
    configuration.get[String]("microservice.services.self.relationship-establishment.name")

  lazy val relationshipTaxableIdentifier: String =
    configuration.get[String]("microservice.services.self.relationship-establishment.taxable.identifier")

  lazy val relationshipNonTaxableIdentifier: String =
    configuration.get[String]("microservice.services.self.relationship-establishment.nonTaxable.identifier")

  private def relationshipEstablishmentFrontendPath(identifier: String): String =
    s"${configuration.get[String]("microservice.services.relationship-establishment-frontend.path")}/$identifier"

  private def relationshipEstablishmentFrontendHost: String =
    configuration.get[String]("microservice.services.relationship-establishment-frontend.host")

  private def stubbedRelationshipEstablishmentFrontendPath(identifier: String): String =
    s"${configuration.get[String]("microservice.services.test.relationship-establishment-frontend.path")}/$identifier"

  private def stubbedRelationshipEstablishmentFrontendHost: String =
    configuration.get[String]("microservice.services.test.relationship-establishment-frontend.host")

  lazy val relationshipTTL: Int =
    configuration.get[Int]("microservice.services.test.relationship-establishment-frontend.mongo.ttl")

  lazy val relationshipEstablishmentStubbed: Boolean =
    configuration.get[Boolean]("microservice.services.features.stubRelationshipEstablishment")

  def relationshipEstablishmentFrontendUrl(identifier: String): String = {
    if(relationshipEstablishmentStubbed) {
      s"$stubbedRelationshipEstablishmentFrontendHost/${stubbedRelationshipEstablishmentFrontendPath(identifier)}"
    } else {
      s"$relationshipEstablishmentFrontendHost/${relationshipEstablishmentFrontendPath(identifier)}"
    }
  }

  def relationshipEstablishmentBaseUrl: String = servicesConfig.baseUrl("test.relationship-establishment")

  lazy val relationshipEstablishmentSuccessUrl: String =
    configuration.get[String]("microservice.services.self.relationship-establishment.successUrl")

  lazy val relationshipEstablishmentFailureUrl: String =
    configuration.get[String]("microservice.services.self.relationship-establishment.failureUrl")

  lazy val countdownLength: Int = configuration.get[Int]("timeout.countdown")
  lazy val timeoutLength: Int = configuration.get[Int]("timeout.length")

  def helplineUrl(implicit messages: Messages): String = {
    val path = messages.lang.code match {
      case "cy" => "urls.welshHelpline"
      case _ => "urls.trustsHelpline"
    }
    configuration.get[String](path)
  }

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  def registerTrustAsTrusteeUrl: String = configuration.get[String]("urls.registerTrustAsTrustee")

  val cachettl: Long = configuration.get[Long]("mongodb.timeToLiveInSeconds")

  val dropIndexes: Boolean = configuration.getOptional[Boolean]("microservice.services.features.mongo.dropIndexes").getOrElse(false)
}
