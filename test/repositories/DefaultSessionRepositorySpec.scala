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

package repositories

import base.SpecBase
import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class DefaultSessionRepositorySpec extends SpecBase with MongoSupport with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    Await.result(repository.collection.deleteMany(BsonDocument()).toFuture(), Duration.Inf)

  lazy val repository = new DefaultSessionRepository(mongoComponent, frontendAppConfig)

  private def checkUserAnswers(actual: UserAnswers, expected: UserAnswers): Unit = {
    actual.id mustBe expected.id
    actual.data mustBe expected.data
  }

  "DefaultSessionRepository" must {

    "return None when no data in db" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"

      repository.get(internalId).futureValue mustBe None
    }

    "must return a UserAnswers when one exists" in {

      val internalId = "Int-328969d0-557e-2559-96ba-074d0597107e"

      val userAnswers = UserAnswers(internalId, Json.obj("key" -> "123"))

      repository.set(userAnswers).futureValue mustBe true

      checkUserAnswers(repository.get(internalId).futureValue.value, userAnswers)
    }

    "must return the userAnswers after update" in {
      val internalId = "Int-328969d0-557e-2559-96ba-074d0597107e"
      val userAnswers: UserAnswers = UserAnswers(internalId)
      val userAnswers2 = userAnswers.copy(data = Json.obj("key" -> "321"))

      repository.get(internalId).futureValue mustBe None

      repository.set(userAnswers).futureValue mustBe true

      val dbUserAnswer = repository.get(internalId).futureValue.value
      checkUserAnswers(dbUserAnswer, userAnswers)

      //update

      repository.set(userAnswers2).futureValue mustBe true

      val dbUserAnswer2 = repository.get(internalId).futureValue.value
      checkUserAnswers(dbUserAnswer2, userAnswers2)
    }
  }
}
