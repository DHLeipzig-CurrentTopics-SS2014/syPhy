package importers

import java.util.{Iterator => JIterator}

import grizzled.slf4j.Logging
import importers.model._

import scala.collection.convert.wrapAll._

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class FrenchTreebankImporter extends SyntaxCorpusImporter with Logging {

  override def getCorpus: SyntaxCorpus = SyntaxCorpus.FRENCH_TB

  override protected def createSentenceIterator(): JIterator[PSGNode] = {



    Iterator.empty
  }

  override def close() {}
}

object FrenchTreebankImporter extends Logging with App {

  new FrenchTreebankImporter().createSentenceIterator()

}