package org.template.classification

import org.apache.predictionio.controller.PDataSource
import org.apache.predictionio.controller.EmptyEvaluationInfo

import org.apache.predictionio.controller.Params
import org.apache.predictionio.data.storage.Event
import org.apache.predictionio.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(val appName: String) extends Params

class DataSource(val dsp: DataSourceParams)
   extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, ActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
  
    val eventsRDD: RDD[Event] = PEventStore.find(
      appName = dsp.appName,
      entityType = Some("choose"),
      eventNames = Some(List("contact"))
    )(sc).cache()

    val labeledPoints: RDD[TextClass] = eventsRDD
      .filter {event => event.event == "contact"}
      .map { event =>

      try {
        TextClass(
          text_type = event.entityId,
          text = event.properties.get[String]("text"),
          replyTo = event.properties.getOpt[String]("replyTo"),
          gender = event.properties.getOpt[Number]("gender"),
          bdate = event.properties.getOpt[Number]("bdate"),
          lang = event.properties.getOpt[String]("lang"),
          platform = event.properties.getOpt[String]("platform")
        ) 
      } catch {
        case e: Exception =>
          logger.error(s"Cannot convert ${event} to TextClass." +
            s" Exception: ${e}.")
          throw e
      }
    }
    
    new TrainingData(labeledPoints)
  }
  override
  def readEval(sc: SparkContext)
  : Seq[(TrainingData, EmptyEvaluationInfo, RDD[(Query, ActualResult)])] = {
    val eventsRDD: RDD[Event] = PEventStore.find(
      appName = dsp.appName,
      entityType = Some("choose"),
      eventNames = Some(List("contact"))
    )(sc).cache()

    val labeledPoints: RDD[TextClass] = eventsRDD
      .filter {event => event.event == "contact"}
      .map { event =>

      try {
        TextClass(
          text_type = event.entityId,
          text = event.properties.get[String]("text"),
          replyTo = event.properties.getOpt[String]("replyTo"),
          gender = event.properties.getOpt[Number]("gender"),
          bdate = event.properties.getOpt[Number]("bdate"),
          lang = event.properties.getOpt[String]("lang"),
          platform = event.properties.getOpt[String]("platform")
        ) 
      } catch {
        case e: Exception =>
          logger.error(s"Cannot convert ${event} to TextClass." +
            s" Exception: ${e}.")
          throw e
      }
    }.cache()
    
    (0 until 1).map { idx =>
      val random = idx
      (
        new TrainingData(labeledPoints),
        new EmptyEvaluationInfo(),
        labeledPoints.map {
          p => (new Query(p.text, p.replyTo, p.gender, p.bdate, p.lang, p.platform), 
            new ActualResult(p.text_type))
        }
      )
    }
  }
}


case class TextClass(
  val text_type: String,
  val text: String,
  val replyTo: Option[String],
  val gender: Option[Number],
  val bdate: Option[Number],
  val lang: Option[String],
  val platform: Option[String]
)

class TrainingData(
  val texts: RDD[TextClass]
) extends Serializable {
  override def toString = {
    s"queries: [${texts.count()}] (${texts.take(1).toList}...)"
  }
}
