/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
import com.google.common.base.{Function => GuavaFunction}


package object utils {

  implicit def guavaFunction[T, R](f: T => R): GuavaFunction[T, R] = {
    new GuavaFunction[T, R] {
      override def apply(x: T): R = f(x)
    }
  }
}
