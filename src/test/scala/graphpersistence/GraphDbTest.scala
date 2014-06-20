package graphpersistence

import com.tinkerpop.gremlin.scala._
import graphpersistence._
import java.util.Date


import org.apache.log4j.Logger
import org.scalatest.{Matchers, ShouldMatchers, FunSuite}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class GraphDbTest extends FunSuite with Matchers {

  import GraphDbTest._

  test("correctly close of Db connection after delay") {
    GraphDb.withTx(TEST_DB_NAME) { g =>

      g.vWithLabel("SessionTestVertexA").getOrElse {
        g.addV().withLabelUpdated("SessionTestVertexA").withPropertyUpdated("time", new Date().toString)
        log.info("created test vertex")
      }
    }
    Thread.sleep(100)
    GraphDb.withTx(TEST_DB_NAME) { g =>
      g.vWithLabel("SessionTestVertexA") match {
        case None => fail("test vertex not saved")
        case Some(v) => {
          val timeOpt = v.property[String]("time")
          log info s"time: ${timeOpt}"
          //assert(timeOpt.isDefined)
          v.remove()
        }
      }
    }

    log.info("Starting to idle...")
    Thread.sleep(GraphDb.CONNECTION_RETENTION_TIME.toMillis +
                 GraphDb.TIMEOUT_TICKER_INTERVAL.toMillis + 100)
    log.info("Check for shutdown")
    GraphDb.isOpen(TEST_DB_NAME) should be (false)
  }
}

object GraphDbTest {
  lazy val log = Logger.getLogger(classOf[GraphDbTest])
  val TEST_DB_NAME = "graphdbtest"

  def listThreads = {
    val tg = Thread.currentThread.getThreadGroup
    val threads = Array.fill[Thread](100)(null)
    tg.enumerate(threads)
    println(Map() ++ threads.filter(_ != null).map( t => (t.getName, t.isDaemon)))
  }
}
