package config

import java.io.{IOException, FileNotFoundException, File}
import java.nio.file.{Files, Path}

import grizzled.file.GrizzledFile._
import grizzled.slf4j.Logging

import scala.util.Try

object Locations extends Logging with EnvironmentAware {

  lazy val projectRoot: Path = {
    def findRoot(dir: File = new File(".").getCanonicalFile): File = {
      def isProjectRoot(dir: File) = {
        trace(s"looking for buildfile in ${dir.listFiles().map(_.basename).sorted.mkString(";")}")
        !(dir.listFiles().filter( f => f.basename.getName == "build.gradle" ).isEmpty)
      }

      debug(s"testing if '${dir}' is project root")
      if(isProjectRoot(dir)) dir else Option(dir.getParentFile) match {
        case Some(parentDir) => findRoot(parentDir)
        case None => sys.error("Unable to locate project root")
      }
    }
    findRoot().toPath
  }

  def dbDir(name: String, env: Environment = activeEnvironment): Path = {
    val virtPath = projectRoot.resolve("data").resolve(env.name).resolve("dbs").resolve(name).normalize()
    Files.createDirectories(virtPath)
    virtPath.toRealPath()
  }

  lazy val corpusDataRoot: Path = {
    val fromConfig = new File(ConfigValues.CORPUS_DATA_DIR).toPath
    val resolved = if (fromConfig.isAbsolute) fromConfig else projectRoot.resolve(fromConfig)
    Try(resolved.toRealPath()).recover({ case ioe: IOException =>
      throw new RuntimeException(s"corpus data root dir not accesible ($resolved)", ioe)
    }).get
  }
}
