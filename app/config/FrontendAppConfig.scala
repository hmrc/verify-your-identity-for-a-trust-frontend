/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.i18n.Lang
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.{URI, URLEncoder}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  final val ENGLISH = "en"
  final val WELSH = "cy"

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trusts"

  lazy val serviceName: String = configuration.get[String]("serviceName")

  val analyticsToken: String = configuration.get[String](s"google-analytics.token")

  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val trustsContinueUrl: String = {
    configuration.get[String]("urls.maintainContinue")
  }

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val trustsStoreUrl: String = configuration.get[Service]("microservice.services.trusts-store").baseUrl + "/trusts-store"

  lazy val taxEnrolmentsUrl: String = configuration.get[Service]("microservice.services.tax-enrolments").baseUrl + "/tax-enrolments"

  lazy val relationshipEstablishmentUrl : String =
    configuration.get[Service]("microservice.services.relationship-establishment").baseUrl + "/relationship-establishment"

  lazy val relationshipName : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.name")

  lazy val relationshipTaxableIdentifier : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.taxable.identifier")

  lazy val relationshipNonTaxableIdentifier : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.nonTaxable.identifier")

  private def relationshipEstablishmentFrontendPath(identifier: String) : String =
    s"${configuration.get[String]("microservice.services.relationship-establishment-frontend.path")}/$identifier"

  private def relationshipEstablishmentFrontendHost : String =
    configuration.get[String]("microservice.services.relationship-establishment-frontend.host")

  private def stubbedRelationshipEstablishmentFrontendPath(identifier: String) : String =
    s"${configuration.get[String]("microservice.services.test.relationship-establishment-frontend.path")}/$identifier"

  private def stubbedRelationshipEstablishmentFrontendHost : String =
    configuration.get[String]("microservice.services.test.relationship-establishment-frontend.host")

  lazy val relationshipTTL: Int =
    configuration.get[Int]("microservice.services.test.relationship-establishment-frontend.mongo.ttl")

  lazy val relationshipEstablishmentStubbed: Boolean =
    configuration.get[Boolean]("microservice.services.features.stubRelationshipEstablishment")

  def relationshipEstablishmentFrontendtUrl(identifier: String) : String = {
    if(relationshipEstablishmentStubbed) {
      s"${stubbedRelationshipEstablishmentFrontendHost}/${stubbedRelationshipEstablishmentFrontendPath(identifier)}"
    } else {
      s"${relationshipEstablishmentFrontendHost}/${relationshipEstablishmentFrontendPath(identifier)}"
    }
  }

  def relationshipEstablishmentBaseUrl : String = servicesConfig.baseUrl("test.relationship-establishment")

  lazy val relationshipEstablishmentSuccessUrl : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.successUrl")

  lazy val relationshipEstablishmentFailureUrl : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.failureUrl")

  lazy val countdownLength: String = configuration.get[String]("timeout.countdown")
  lazy val timeoutLength: String = configuration.get[String]("timeout.length")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  private lazy val accessibilityBaseLinkUrl: String = configuration.get[String]("urls.accessibility")

  def accessibilityLinkUrl(implicit request: Request[_]): String = {
    val userAction = URLEncoder.encode(new URI(request.uri).getPath, "UTF-8")
    s"$accessibilityBaseLinkUrl?userAction=$userAction"
  }
}
