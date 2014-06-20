package graphpersistence

import com.tinkerpop.blueprints.{Element, Edge, Vertex}
import com.tinkerpop.gremlin.scala._

import scala.collection.immutable.Stream
import scala.collection.convert.decorateAll._
import java.lang.{Iterable => JIterable}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
protected[graphpersistence] object DslExtensions {
  val LABEL_PROPERTY_KEY = "__label"
  val LABEL = LABEL_PROPERTY_KEY
}

protected[graphpersistence] trait GremlinScalaExtensions {

  import DslExtensions._

  implicit def vertexFunction2scalaVertexFunction[T](f: ScalaVertex => T): Vertex => T = { v: Vertex => f(v) }

  implicit def edgeFunction2scalaEdgeFunction[T](f: ScalaEdge => T): Edge => T = { v: Edge => f(v) }

  protected def fluentPropertyUpdate[E <: Element](e: E, key: String, value: Any) = {
    e.setProperty(key, value)
    e
  }


  implicit class RichVertex(val self: Vertex) extends ScalaVertex(self) {

    def setLabel(label: String) = fluentPropertyUpdate(self, LABEL, label)

    def withLabelUpdated(label: String) = setLabel(label)

    def withPropertyUpdated(key: String, value: Any) = fluentPropertyUpdate(self, key, value)
  }

  implicit class RichEdge(val self: Edge) extends ScalaEdge(self) {

    def setCustomLabel(label: String) = {
      self.setProperty(LABEL, label)
      self
    }

    def withCustomLabelUpdated(label: String) = setCustomLabel(label)

    def withPropertyUpdated(key: String, value: Any) = fluentPropertyUpdate(self, key, value)
  }
}

protected[graphpersistence] trait IndexGraphExtensions {
  import DslExtensions._

  protected def self: IndexGraph

  /** iterate all vertices */
  def allV: GremlinScalaPipeline[Vertex, Vertex] = new GremlinScalaPipeline[ScalaGraph, Vertex].V(self)

  /** iterate all edges */
  def allE: GremlinScalaPipeline[Edge, Edge] = new GremlinScalaPipeline[ScalaGraph, Edge].E(self)

  /** Returns the vertices with the specified label properties. */
  def labeledVs(labels: String*): Iterable[Vertex] = {
    labels flatMap { l => self.getVertices(LABEL, l).asScala}
  }

  /** Returns the edges with the specified label properties. */
  def labeledEs(labels: String*): Iterable[Edge] = labels flatMap { l => self.getEdges(LABEL, l).asScala}

  protected def uniqueLabelOption[T <: Element](fetcher: String => JIterable[T])(label: String) = {
    Stream() ++ fetcher(label).asScala match {
      case v #:: Stream.Empty => Some(v)
      case Stream.Empty => None
      case x => throw new IllegalStateException(s"multible vertices with label '$label'")
    }
  }

  /** Returns the vertex with the specified label property if unique. */
  def vWithLabel(label: String): Option[Vertex] = uniqueLabelOption(self.getVertices(LABEL, _))(label)

  /** Returns the edge with the specified ID. */
  def eWithLabel(label: String): Option[Edge] = uniqueLabelOption(self.getEdges(LABEL, _))(label)
}
