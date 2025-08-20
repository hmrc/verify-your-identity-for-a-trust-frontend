/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application

class FrontendAppConfigSpec extends AnyWordSpec {

  object TestCfg {
    val common: Map[String, Any] = Map(
      "appName" -> "verify-your-identity-for-a-trust-frontend",

      // contact-frontend (used by ContactFrontendConfig)
      "contact-frontend.base-url" -> "http://localhost:9250",
      "contact-frontend.serviceId" -> "trusts-frontend",

      // urls
      "urls.login" -> "http://login.url",
      "urls.loginContinue" -> "http://continue.url",
      "urls.logout" -> "http://logout.url",
      "urls.maintainContinue" -> "http://maintain-continue",
      "urls.trustsRegistration" -> "http://trusts-registration",
      "urls.trustsHelpline" -> "http://helpline/en",
      "urls.welshHelpline" -> "http://helpline/cy",
      "urls.registerTrustAsTrustee" -> "http://register-trustee",

      // timeouts
      "timeout.countdown" -> 120,
      "timeout.length" -> 900,

      // features
      "microservice.services.features.auditing.logout" -> true,
      "microservice.services.features.welsh-translation" -> true,
      "microservice.services.features.mongo.dropIndexes" -> true,

      // services base urls (ServicesConfig)
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> 8500,
      "microservice.services.auth.protocol" -> "http",

      "microservice.services.trusts-store.host" -> "localhost",
      "microservice.services.trusts-store.port" -> 9823,
      "microservice.services.trusts-store.protocol" -> "http",

      "microservice.services.relationship-establishment.host" -> "localhost",
      "microservice.services.relationship-establishment.port" -> 7710,
      "microservice.services.relationship-establishment.protocol" -> "http",

      "microservice.services.test.relationship-establishment.host" -> "localhost",
      "microservice.services.test.relationship-establishment.port" -> 7711,
      "microservice.services.test.relationship-establishment.protocol" -> "http",

      // relationship-establishment (self)
      "microservice.services.self.relationship-establishment.name" -> "TRUSTS",
      "microservice.services.self.relationship-establishment.taxable.identifier" -> "HMRC-TERS-ORG",
      "microservice.services.self.relationship-establishment.nonTaxable.identifier" -> "HMRC-TERSNT-ORG",
      "microservice.services.self.relationship-establishment.successUrl" -> "http://success.url",
      "microservice.services.self.relationship-establishment.failureUrl" -> "http://failure.url",

      // relationship-establishment-frontend (real + stubbed)
      "microservice.services.relationship-establishment-frontend.path" -> "real/path",
      "microservice.services.relationship-establishment-frontend.host" -> "http://real-host",
      "microservice.services.test.relationship-establishment-frontend.path" -> "stub/path",
      "microservice.services.test.relationship-establishment-frontend.host" -> "http://stub-host",
      "microservice.services.test.relationship-establishment-frontend.mongo.ttl" -> 3600,

      // mongo
      "mongodb.timeToLiveInSeconds" -> 604800L
    )

    def withStubbed(flag: Boolean): Map[String, Any] =
      common + ("microservice.services.features.stubRelationshipEstablishment" -> flag)
  }

  // One app where the stubbed flag is TRUE (uses stubbed FE host/path) …
  private val appStubbed: Application =
    new GuiceApplicationBuilder().configure(TestCfg.withStubbed(true)).build()
  // … and one where it’s FALSE (uses real FE host/path).
  private val appReal: Application =
    new GuiceApplicationBuilder().configure(TestCfg.withStubbed(false)).build()

  private val cfgStubbed = appStubbed.injector.instanceOf[FrontendAppConfig]
  private val cfgReal    = appReal.injector.instanceOf[FrontendAppConfig]

  private val messagesApiStubbed = appStubbed.injector.instanceOf[MessagesApi]

  // Helpers to get Messages for a given Lang code
  private def msgs(lang: String)(implicit api: MessagesApi): Messages =
    api.preferred(Seq(Lang(lang)))

  "FrontendAppConfig (common values)" should {

    "expose the app name" in {
      cfgStubbed.appName mustBe "verify-your-identity-for-a-trust-frontend"
    }

    "build betaFeedbackUrl from contact-frontend config" in {
      cfgStubbed.betaFeedbackUrl mustBe
        "http://localhost:9250/contact/beta-feedback?service=trusts-frontend"
    }

    "expose auth, login/continue/logout URLs and logoutAudit" in {
      cfgStubbed.authUrl mustBe "http://localhost:8500"
      cfgStubbed.loginUrl mustBe "http://login.url"
      cfgStubbed.loginContinueUrl mustBe "http://continue.url"
      cfgStubbed.logoutUrl mustBe "http://logout.url"
      cfgStubbed.logoutAudit mustBe true
    }

    "expose trusts continue & registration URLs" in {
      cfgStubbed.trustsContinueUrl mustBe "http://maintain-continue"
      cfgStubbed.trustsRegistration mustBe "http://trusts-registration"
    }

    "reflect languageTranslationEnabled feature flag" in {
      cfgStubbed.languageTranslationEnabled mustBe true
    }

    "expose base urls via ServicesConfig" in {
      cfgStubbed.trustsStoreUrl mustBe "http://localhost:9823"
      cfgStubbed.relationshipEstablishmentUrl mustBe "http://localhost:7710"
      cfgStubbed.relationshipEstablishmentBaseUrl mustBe "http://localhost:7711"
    }

    "expose relationship establishment settings" in {
      cfgStubbed.relationshipName mustBe "TRUSTS"
      cfgStubbed.relationshipTaxableIdentifier mustBe "HMRC-TERS-ORG"
      cfgStubbed.relationshipNonTaxableIdentifier mustBe "HMRC-TERSNT-ORG"
      cfgStubbed.relationshipTTL mustBe 3600
      cfgStubbed.relationshipEstablishmentSuccessUrl mustBe "http://success.url"
      cfgStubbed.relationshipEstablishmentFailureUrl mustBe "http://failure.url"
    }

    "expose timeout values" in {
      cfgStubbed.countdownLength mustBe 120
      cfgStubbed.timeoutLength mustBe 900
    }

    "return the English helpline URL when Messages.lang is en" in {
      implicit val m: Messages = msgs("en")(messagesApiStubbed)
      cfgStubbed.helplineUrl mustBe "http://helpline/en"
    }

    "return the Welsh helpline URL when Messages.lang is cy" in {
      implicit val m: Messages = msgs("cy")(messagesApiStubbed)
      cfgStubbed.helplineUrl mustBe "http://helpline/cy"
    }

    "expose the language map and routeToSwitchLanguage" in {
      cfgStubbed.languageMap mustBe Map(
        "english" -> Lang("en"),
        "cymraeg" -> Lang("cy")
      )

      cfgStubbed.routeToSwitchLanguage("en").url mustBe
        controllers.routes.LanguageSwitchController.switchToLanguage("en").url
      cfgStubbed.routeToSwitchLanguage("cy").url mustBe
        controllers.routes.LanguageSwitchController.switchToLanguage("cy").url
    }

    "expose registerTrustAsTrusteeUrl, cache ttl and dropIndexes flag" in {
      cfgStubbed.registerTrustAsTrusteeUrl mustBe "http://register-trustee"
      cfgStubbed.cachettl mustBe 604800L
      cfgStubbed.dropIndexes mustBe true
    }
  }

  "FrontendAppConfig.relationshipEstablishmentFrontendUrl" should {

    "return the STUBBED host/path when the feature flag is true" in {
      val url = cfgStubbed.relationshipEstablishmentFrontendUrl("ABC123")
      url mustBe "http://stub-host/stub/path/ABC123"
    }

    "return the REAL host/path when the feature flag is false" in {
      val url = cfgReal.relationshipEstablishmentFrontendUrl("ABC123")
      url mustBe "http://real-host/real/path/ABC123"
    }
  }
}

