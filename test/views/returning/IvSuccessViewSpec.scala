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

package views.returning

import views.behaviours.ViewBehaviours
import views.html.returning

class IvSuccessViewSpec extends ViewBehaviours {

  val utr = "0987654321"

  "Returning IvSuccess view with Agent" must {

    "display the register link when config.playbackEnabled is true" when {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("microservice.services.features.playback.enabled" -> true)
        .build()

      val view = application.injector.instanceOf[returning.IvSuccessView]

      val applyView = view.apply(isAgent = true, utr)(fakeRequest, messages)

      behave like normalPage(applyView, "ivSuccess.returning.agent", "paragraph1", "continueLink.a", "continueLink.b",
        "paragraph2")
    }

    "display the correct subheading" in {

      val view = viewFor[returning.IvSuccessView](Some(emptyUserAnswers))

      val applyView = view.apply(isAgent = true, utr)(fakeRequest, messages)

      val doc = asDocument(applyView)
      assertContainsText(doc, messages("ivSuccess.subheading", utr))
    }

    "do not display the register link when config.playbackEnabled is false" when {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("microservice.services.features.playback.enabled" -> false)
        .build()

      val view = application.injector.instanceOf[returning.IvSuccessView]

      val applyView = view.apply(isAgent = true, utr)(fakeRequest, messages)

      behave like normalPage(applyView, "ivSuccess.returning.agent","paragraph1", "paragraph2")
    }

  }

  "IvSuccess view with no Agent" must {

    val view = viewFor[returning.IvSuccessView](Some(emptyUserAnswers))

    val applyView = view.apply(isAgent = false, utr)(fakeRequest, messages)

    behave like normalPage(applyView, "ivSuccess.returning.no.agent","paragraph1", "paragraph2",
      "paragraph3")

    "display the correct subheading" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, messages("ivSuccess.subheading", utr))
    }

  }

}
