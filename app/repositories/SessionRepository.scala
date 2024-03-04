/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Singleton

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import config.FrontendAppConfig

import javax.inject.Inject
import models.UserAnswers
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, ReplaceOptions, Updates}
import org.mongodb.scala.model.Indexes.ascending
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultSessionRepository @Inject()(
                                          val mongoComponent: MongoComponent,
                                          val config: FrontendAppConfig
                                        )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongoComponent,
    domainFormat = Format(UserAnswers.reads,UserAnswers.writes),
    indexes = Seq(
      IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .unique(false)
          .name("user-answers-last-updated-index")
          .expireAfter(config.cachettl, TimeUnit.SECONDS)
      )
    ), replaceIndexes = config.dropIndexes

  )  with SessionRepository {

  override def get(id: String): Future[Option[UserAnswers]] = {
     val selector = equal("_id", id)
     val modifier = Updates.set("updatedAt", LocalDateTime.now())
     val updateOption = new FindOneAndUpdateOptions().upsert(false)

     collection.findOneAndUpdate(selector, modifier, updateOption).toFutureOption()
}

  override def set(userAnswers: UserAnswers): Future[Boolean] = {
    val selector = equal("_id" , userAnswers.id)
    val modifier = userAnswers.copy(lastUpdated = LocalDateTime.now)
    val options = ReplaceOptions().upsert(true)

    collection.replaceOne(selector,modifier,options).headOption().map(_.exists(_.wasAcknowledged()))
  }
}

trait SessionRepository {

  def get(id: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]
}
