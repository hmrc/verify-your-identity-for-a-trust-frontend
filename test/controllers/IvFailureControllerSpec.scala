/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import base.SpecBase
import pages.UtrPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

class IvFailureControllerSpec extends SpecBase {

//  {"journeyId":"47a8a543-6961-4221-86e8-d22e2c3c91de","relationship":{"relationshipName":"Trusts","businessKeys":[{"name":"utr","value":"3000000001"}],"credId":"987459879458"},"errorKey":"TRUST_LOCKED"}

  "IvFailure Controller" must {

    "callback-failure route" when {

      "redirect to trust locked page when user fails Trusts IV after multiple attempts" in {

        val onIVFailureRoute = routes.IVFailureController.onTrustIVFailure().url

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, onIVFailureRoute)
          .withHeaders(
            ("JourneyFailure", "http://localhost:9662/relationship-establishment/journey-failure/47a8a543-6961-4221-86e8-d22e2c3c91de")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.IVFailureController.trustLocked().url

        application.stop()
      }
    }

    "locked route" when {

      "return OK and the correct view for a GET for locked route" in {

        val onLockedRoute = routes.IVFailureController.trustLocked().url

        val answers = emptyUserAnswers
          .set(UtrPage, "3000000001").success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, onLockedRoute)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) must include("Sorry, you cannot access this trust")

        application.stop()
      }
    }

  }
}
