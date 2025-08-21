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

import base.SpecBase
import play.api.i18n.{Lang, MessagesApi}

class FrontendAppConfigSpec extends SpecBase {


  private val appConfig = app.injector.instanceOf[FrontendAppConfig]


  "FrontendAppConfig" should {

    "expose the app name" in {
      appConfig.appName mustBe "verify-your-identity-for-a-trust-frontend"
    }

    "build betaFeedbackUrl from contact-frontend config" in {
      appConfig.betaFeedbackUrl mustBe
        "http://localhost:9250/contact/beta-feedback?service=trusts"
    }

    "expose auth, login/continue/logout URLs and logoutAudit" in {
      appConfig.authUrl mustBe "http://localhost:8500"
      appConfig.loginUrl mustBe "http://localhost:9949/auth-login-stub/gg-sign-in"
      appConfig.loginContinueUrl mustBe "http://localhost:9789/verify-your-identity-for-a-trust"
      appConfig.logoutUrl mustBe "http://localhost:9514/feedback/trusts"
      appConfig.logoutAudit mustBe false
    }

    "expose trusts continue & registration URLs" in {
      appConfig.trustsContinueUrl mustBe "http://localhost:9788/maintain-a-trust/status"
      appConfig.trustsRegistration mustBe "http://localhost:9781/trusts-registration"
    }

    "reflect languageTranslationEnabled feature flag" in {
      appConfig.languageTranslationEnabled mustBe true
    }

    "expose base urls via ServicesConfig" in {
      appConfig.trustsStoreUrl mustBe "http://localhost:9783"
      appConfig.relationshipEstablishmentUrl mustBe "http://localhost:9662"
      appConfig.relationshipEstablishmentBaseUrl mustBe "http://localhost:9662"
    }

    "expose relationship establishment settings" in {
      appConfig.relationshipName mustBe "Trusts"
      appConfig.relationshipTaxableIdentifier mustBe "utr"
      appConfig.relationshipNonTaxableIdentifier mustBe "urn"
      appConfig.relationshipTTL mustBe 1400
      appConfig.relationshipEstablishmentSuccessUrl mustBe "http://localhost:9789/verify-your-identity-for-a-trust/verified"
      appConfig.relationshipEstablishmentFailureUrl mustBe "http://localhost:9789/verify-your-identity-for-a-trust/callback-failure"
    }

    "expose timeout values" in {
      appConfig.countdownLength mustBe 120
      appConfig.timeoutLength mustBe 900
    }

    "return the English helpline URL when Messages.lang is en" in {
      appConfig.helplineUrl mustBe "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/trusts"
    }

    "return the Welsh helpline URL when Messages.lang is cy" in {
      appConfig.helplineUrl mustBe "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/trusts"
    }

    "expose the language map and routeToSwitchLanguage" in {
      appConfig.languageMap mustBe Map(
        "english" -> Lang("en"),
        "cymraeg" -> Lang("cy")
      )

      appConfig.routeToSwitchLanguage("en").url mustBe
        controllers.routes.LanguageSwitchController.switchToLanguage("en").url
      appConfig.routeToSwitchLanguage("cy").url mustBe
        controllers.routes.LanguageSwitchController.switchToLanguage("cy").url
    }

    "expose registerTrustAsTrusteeUrl, cache ttl and dropIndexes flag" in {
      appConfig.registerTrustAsTrusteeUrl mustBe "https://www.gov.uk/guidance/register-a-trust-as-a-trustee"
      appConfig.cachettl mustBe 7200
      appConfig.dropIndexes mustBe true
    }

    "return the STUBBED host/path when the feature flag is true" in {
      val url = appConfig.relationshipEstablishmentFrontendUrl("ABC123")
      url mustBe "http://localhost:9663/check-your-identity-for-trusts/relationships/ABC123"
    }

    "read the stubbed relationship-establishment-frontend.host from configuration" in {
      val confValue = app.configuration.get[String](
        "microservice.services.test.relationship-establishment-frontend.host"
      )
      confValue mustBe "http://localhost:9789"
    }

    "return the English helpline URL when Messages.lang is explicitly en" in {
      val messagesApi = app.injector.instanceOf[MessagesApi]
      implicit val messages = messagesApi.preferred(Seq(Lang("en")))
      appConfig.helplineUrl mustBe
        "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/trusts"
    }

    "return the Welsh helpline URL when Messages.lang is explicitly cy" in {
      val messagesApi = app.injector.instanceOf[MessagesApi]
      implicit val messages = messagesApi.preferred(Seq(Lang("cy")))
      appConfig.helplineUrl mustBe
        "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/welsh-language-helplines"
    }
  }


}

