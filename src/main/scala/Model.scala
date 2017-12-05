package org.apache.spark.mllib.classification


import org.template.classification.AlgorithmParams

import org.apache.predictionio.controller.IPersistentModel
import org.apache.predictionio.controller.IPersistentModelLoader

import org.apache.spark.SparkContext

import edu.stanford.nlp.classify.Classifier
import edu.stanford.nlp.classify.ColumnDataClassifier

class Model(val cl: Classifier[String, String])
  extends Serializable
  {
  def save(id: String, params: AlgorithmParams,
    sc: SparkContext): Boolean = {
    sc.parallelize(Seq(cl)).saveAsObjectFile(s"/tmp/${id}/cl")
    true
  }

  override def toString = {
    s"empty"
  }
}

object Model
  extends IPersistentModelLoader[AlgorithmParams, Model] 
  with Serializable {
  def apply(id: String, params: AlgorithmParams,
    sc: Option[SparkContext]) = {
    new Model(
      cl = sc.get
        .objectFile[Classifier[String, String]](s"/tmp/${id}/cl").first
    )
  }
}
