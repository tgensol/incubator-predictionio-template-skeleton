package org.template.classification

import org.apache.predictionio.controller.PPreparator

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(texts = trainingData.texts)
  }
}

class PreparedData(
   val texts: RDD[TextClass]
) extends Serializable
