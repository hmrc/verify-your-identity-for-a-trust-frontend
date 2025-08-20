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

package handlers

import base.SpecBase
import play.api.i18n.MessagesApi
import play.api.test.Helpers.{await, defaultAwaitTimeout} // <-- brings in await
import play.twirl.api.Html
import views.html.{ErrorTemplate, PageNotFoundView}

import scala.concurrent.ExecutionContext

class ErrorHandlerSpec extends SpecBase {

  // Provide an implicit ExecutionContext for the handler's constructor
  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val messageApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private val errorTemplate: ErrorTemplate   = app.injector.instanceOf[ErrorTemplate]
  private val notFoundView: PageNotFoundView = app.injector.instanceOf[PageNotFoundView]

  private val handler = new ErrorHandler(messageApi, errorTemplate, notFoundView)

  "ErrorHandler" must {

    "render the standard error template with the given title, heading and message" in {
      val pageTitle = "pageTitle"
      val heading   = ""
      val message   = "message"

      val result: Html   = await(handler.standardErrorTemplate(pageTitle, heading, message)(fakeRequest))
      val expected: Html = errorTemplate(pageTitle, heading, message)

      result.body mustBe expected.body
      result.body must include(pageTitle)
      result.body must include(heading)
      result.body must include(message)
    }

    "render the not found template" in {
      val result: Html   = await(handler.notFoundTemplate(fakeRequest))
      val expected: Html = notFoundView()

      result.body mustBe expected.body
      result.body must not be empty
    }
  }
}
