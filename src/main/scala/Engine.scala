package org.template.classification

import org.apache.predictionio.controller.EngineFactory
import org.apache.predictionio.controller.Engine

case class Query(
  val text: String,
  val replyTo: Option[String],
  val gender: Option[Number],
  val bdate: Option[Number],
  val lang: Option[String],
  val platform: Option[String]
) extends Serializable


case class PredictedResult(
  val queryResults: String
) extends Serializable


object ClassificationEngine extends EngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
     Map("als" -> classOf[NLPAlgorithm]),
      classOf[Serving])
  }
}
