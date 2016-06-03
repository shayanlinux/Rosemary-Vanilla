package nl.amc.ebioscience.rosemary.core.search

import nl.amc.ebioscience.rosemary.models.Searchable
import nl.amc.ebioscience.rosemary.models.core.Valunit
import org.apache.lucene.document.{ Document, StringField, Field, TextField }
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import play.api.Logger

object SearchWriter {

  val writerConfig = new IndexWriterConfig(SearchConfig.version, SearchConfig.analyzer)
  // config.setRAMBufferSizeMB(256.0)
  val writer = new IndexWriter(SearchConfig.directory, writerConfig)

  def add(item: Searchable) {
    Logger.debug(s"Indexing Item: ${item.id}")
    val doc = new Document()

    doc.add(new StringField(SearchConfig.ID_FIELD, item.id.toString, Field.Store.YES))

    doc.add(new TextField(SearchConfig.NAME_FIELD, item.name, Field.Store.NO))

    generateTextFieldsWithPermutatedKeys(item.info.dict).foreach { tf => doc.add(tf) }

    // Collect all values
    val all = new StringBuilder(item.name)
    item.info.dict.foreach(entry => {
      all.append(' ')
      all.append(entry._2.value)
    })
    doc.add(new TextField(SearchConfig.ALL_FIELD, all.toString, Field.Store.NO))

    Logger.trace("Indexing: " + doc.toString)

    try {
      writer.addDocument(doc)
    } catch {
      case e: Exception => Logger.error(s"SearchWriter addDocument exception: ${e.getMessage}")
    }
  }

  /** Sample input:
    * <pre><code>
    * Map(this/is/a/test -> Valunit(value1,None),
    *   key -> Valunit(value2,None),
    *   foo/bar -> Valunit(value3,Some(unit)))
    * </code></pre>
    *
    * Sample output:
    * <pre><code>
    * Map(test -> value1, a/test -> value1, is/a/test -> value1, this/is/a/test -> value1,
    *   key -> value2,
    *   bar -> value3, foo/bar -> value3)
    * </code></pre>
    */
  private def generateTextFieldsWithPermutatedKeys(dict: Map[String, Valunit]): Iterable[TextField] =
    for {
      entry <- dict
      permKeyStr <- entry._1.toLowerCase.split('/').scanRight(List[String]())(_ +: _).filterNot(_.isEmpty).map(_.mkString("/"))
    } yield new TextField(permKeyStr, entry._2.value, Field.Store.NO)

  def deleteAllAndCommit {
    Logger.info("Deleting the old index...")
    try {
      writer.deleteAll
      writer.commit
    } catch {
      case e: Exception => Logger.error(s"SearchWriter deleteAll and commit exception: ${e.getMessage}")
    }
  }

  def commit {
    Logger.debug("Committing search writer...")
    try {
      writer.commit
    } catch {
      case e: Exception => Logger.error(s"SearchWriter commit exception: ${e.getMessage}")
    }
  }

  def close {
    Logger.debug("Closing search writer...")
    try {
      writer.close
    } catch {
      case e: Exception => Logger.error(s"SearchWriter close exception: ${e.getMessage}")
    }
  }
}
