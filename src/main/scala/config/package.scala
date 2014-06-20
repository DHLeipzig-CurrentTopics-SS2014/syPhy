import config.Environment._

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
package object config {

  trait EnvironmentAware {
    def activeEnvironment = config.activeEnvironment
  }

  protected[config] lazy val activeEnvironment: Environment = {

    val env = sys.env.get("ENV")
    val prop = sys.props.get("env")
    val combEnvs = (env.toList ++ prop.toList).map(x => Environment.byString(x))

    combEnvs.toSet.toList match {
      case Nil => Environment.DEFAULT_ENVIRONMENT
      case List(env) => env
      case List(names @ _*) => throw new Environment.AmbiguousEnvironmentException(names.map(_.name) : _*)
    }
  }

  protected[config] def envByString(str: String): Environment = {
    prefix2Env.filter( e => str.startsWith(e._1)).map(_._2).toList match {
      case Nil => throw new Environment.UnrecognizedEnvironmentException(str)
      case List(env) => env
      case List(_*) => throw new Environment.AmbiguousEnvironmentException(str)
    }
  }

  protected[config] val prefix2Env = Map("dev" -> DEVELOPMENT, "test" -> TEST, "prod" -> PRODUCTION)
}
