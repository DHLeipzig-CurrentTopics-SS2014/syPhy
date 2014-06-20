package graphpersistence

import java.io.File
import java.lang.{Long => JLong}
import java.nio.file.{FileAlreadyExistsException, Files, Path}
import java.util.concurrent.TimeUnit

import com.google.common.cache._
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph
import config.Locations
import grizzled.slf4j.Logging
import rx.lang.scala.Observable
import utils._

import scala.concurrent.duration.Duration


/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

object GraphDb extends Logging {

  val DEFAULT_DB_NAME = "main"
  val CONNECTION_RETENTION_TIME: Duration = Duration("4s")
  val TIMEOUT_TICKER_INTERVAL: Duration = Duration("1s")
  
  protected lazy val timeoutListener = new RemovalListener[String, JLong] {
    override def onRemoval(notification: RemovalNotification[String, JLong]): Unit = {
      val timeSinceLastTransaction = System.currentTimeMillis - notification.getValue
      debug(s"Invalidating Neo4j DB at directory ${notification.getKey} " +
          s"($timeSinceLastTransaction ms after last transaction)")
      connCache.invalidate(notification.getKey)
    }
  }

  protected lazy val shutdownListener = new RemovalListener[String, NeoLikeGraph] {
    override def onRemoval(notification: RemovalNotification[String, NeoLikeGraph]): Unit = {
      info(s"Shutting down Neo4j DB at directory ${notification.getKey}")
      notification.getValue.shutdown()
    }
  }

  lazy val ticker = Observable.interval(TIMEOUT_TICKER_INTERVAL).foreach { _ =>
    debug("clean up for connection cache")
    connTimeouts.cleanUp()
    connCache.cleanUp()
  }

  protected lazy val connCache: LoadingCache[String, NeoLikeGraph] = {
    ticker //ensure start of cleanup ticker
    CacheBuilder.newBuilder
        .removalListener(shutdownListener)
        .build(CacheLoader.from((dirPath: String) => {
      debug(s"(Re)opening Neo4j DB at directory: ${dirPath}")
      new Neo4j2Graph(dirPath)
    }))
  }
  protected lazy val connTimeouts: Cache[String, JLong] = {
    ticker //ensure start of cleanup ticker
    CacheBuilder.newBuilder
        .removalListener(timeoutListener)
        .expireAfterWrite(CONNECTION_RETENTION_TIME.length ,CONNECTION_RETENTION_TIME.unit)
        .build()
  }

  /**
   * Executes @code{thunk} as an transaction in the named graph database. A connection to this graph
   * database will be opened or the retained connection for the graph database will be reused. After
   * completion (or abort) of the transaction, the timout for eviction of of the retained db connection
   * will be reset.
   *
   * @param dbName
   * @param thunk
   * @tparam T
   * @return
   */
  def withTx[T](dbName: String = DEFAULT_DB_NAME)(thunk: NeoLikeGraph => T): T = {
    val (connKey, conn) = getConnectionForName(dbName)
    val result = conn withTx thunk
    debug(s"tx for $connKey completed")
    connTimeouts.put(connKey, System.currentTimeMillis)
    debug(s"timeout reset done for $connKey")
    result
  }

  def withTx[T](thunk: NeoLikeGraph => T): T = withTx(DEFAULT_DB_NAME)(thunk)

  def isOpen(dbName: String = DEFAULT_DB_NAME): Boolean =
    Option(connCache.getIfPresent(connKeyForName(dbName))).isDefined
  
  /**
   * Causes all retained open graph database connections to be shut down.
   */
  def shutdownAll() {
    List(connTimeouts, connCache).foreach { cache =>
      cache.invalidateAll()
      cache.cleanUp()
    }
  }

  protected def getConnectionForPath(dbDir: Path): (String, NeoLikeGraph) = {
    val connKey = ensureAndCanonicalizeDir(dbDir).toString
    (connKey, connCache.get(connKey))
  }

  protected def getConnectionForPath(dbDirStr: String): (String, NeoLikeGraph) =
    getConnectionForPath(new File(dbDirStr).toPath)

  protected def getConnectionForName(dbName: String): (String, NeoLikeGraph) =
    getConnectionForPath(Locations.dbDir(dbName))

  protected def connKeyForName(dbName: String) = 
    ensureAndCanonicalizeDir(Locations.dbDir(dbName))
  
  protected def ensureAndCanonicalizeDir(path: Path): Path = {
    try {
      Files.createDirectories(path)
    } catch {
      case fae: FileAlreadyExistsException => ()
    }
    assert(Files.isDirectory(path))
    path.toRealPath()
  }

  protected def ensureAndCanonicalizeDirStr(pathStr: String): Path = {
    ensureAndCanonicalizeDir(new File(pathStr).toPath)
  }
}
