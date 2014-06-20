import com.tinkerpop.blueprints._
import org.neo4j.graphdb.GraphDatabaseService
import graphpersistence.IndexGraphExtensions

import scala.util.{Failure, Success, Try}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
package object graphpersistence extends GremlinScalaExtensions {

  type IndexGraph = Object with KeyIndexableGraph with IndexableGraph

  type NeoLikeGraph = IndexGraph with MetaGraph[GraphDatabaseService] with TransactionalGraph

  protected trait NeoLikeGraphExtensions extends IndexGraphExtensions {

    protected def self: NeoLikeGraph

    def getEnsuredVertexIndex(indexName: String): Index[Vertex] = self.getIndex(indexName, classOf[Vertex]) match {
      case null => self.createIndex(indexName, classOf[Vertex])
      case index: Index[Vertex] => index
    }

    protected[graphpersistence] def withTx[T](thunk: NeoLikeGraph => T): T = {
      Try(thunk(self)) match {
        case Success(res) => {
          self.commit(); res
        }
        case Failure(throwable) => {
          self.rollback(); throw throwable
        }
      }
    }
  }

  implicit class RichNeoLikeGraph(protected val self : NeoLikeGraph) extends NeoLikeGraphExtensions
}
