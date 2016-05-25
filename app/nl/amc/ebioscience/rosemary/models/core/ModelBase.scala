package nl.amc.ebioscience.rosemary.models.core

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.dao.{ SalatDAO, SalatMongoCursor, ModelCompanion }
import play.api.Logger
import se.radley.plugin.salat.PlaySalat
import nl.amc.ebioscience.rosemary.core.JJson

class ModelBase[T <: BaseEntity, I <: Any](val name: String)(
  implicit mot: Manifest[T], mid: Manifest[I], ctx: Context, ps: PlaySalat)
    extends SalatDAO[T, I](collection = ps.collection(name)) { // TODO: maybe use Salat CompanionModel?

  class ModelChild[CT <: BaseEntity, CI <: Any](val name: String, val parent: String = "parentId")(
    implicit mct: Manifest[CT], mcid: Manifest[CI])
      extends ChildCollection[CT, CI](collection = ps.collection(name), parentIdField = parent)

  def findAll() = find(MongoDBObject())
  def findByIds(ids: Set[I]): Set[T] = find("_id" $in ids).toSet
  def findByIds(ids: List[I]): List[T] = find("_id" $in ids).toList

  def removeById(id: I): WriteResult = removeById(id, defaultWriteConcern)
  def removeByIds(ids: List[I]): WriteResult = removeByIds(ids, defaultWriteConcern)

  /** Find documents in the collection that their type ends with a given string */
  def findByType(myType: String): List[T] = find("_t" $regex s".*$myType$$").toList

  def emptyCursor = find("dummy" $eq "dummy")

  implicit class Queries(entity: T) {

    // TODO: How to pass up the WriteResult?
    def save: T = {
      val wr = ModelBase.this.save(entity)
      Logger.trace("Entity saved: " + entity)
      entity
    }

    def insert: T = {
      val oi = ModelBase.this.insert(entity)
      Logger.trace("Entity inserted: " + entity)
      entity
    }

    def remove: T = {
      val wr = ModelBase.this.remove(entity)
      Logger.trace("Entity removed: " + entity)
      entity
    }

    def update: T = {
      val wr = ModelBase.this.update("_id" $eq entity.id, entity, false, false, defaultWriteConcern)
      Logger.trace("Entity updated: " + entity)
      entity
    }

  }
}

/**
 * Vragen:
 *
 * 1. toMDB*: hieronder
 * 2. type alias in object, als parameter van het object zelf -> class[Foo] { type Foo = String }
 */
object ModelBase {

  import scala.language.implicitConversions

  /** Transform collection of vectors to MongoDBObjects */
  implicit def toMDB1[A <: String, B <: Any](elems: ((A, B))) = MongoDBObject(elems)
  implicit def toMDB2[A <: String, B <: Any](elems: ((A, B), (A, B))) = MongoDBObject(elems._1, elems._2)
  implicit def toMDB3[A <: String, B <: Any](elems: ((A, B), (A, B), (A, B))) = MongoDBObject(elems._1, elems._2, elems._3)
  implicit def toMDB4[A <: String, B <: Any](elems: ((A, B), (A, B), (A, B), (A, B))) = MongoDBObject(elems._1, elems._2, elems._3, elems._4)
  implicit def toMDB5[A <: String, B <: Any](elems: ((A, B), (A, B), (A, B), (A, B), (A, B))) = MongoDBObject(elems._1, elems._2, elems._3, elems._4, elems._5)
  implicit def toMDB6[A <: String, B <: Any](elems: ((A, B), (A, B), (A, B), (A, B), (A, B), (A, B))) = MongoDBObject(elems._1, elems._2, elems._3, elems._4, elems._5, elems._6)

  implicit class TraversableToJson[T <: BaseEntity](l: Traversable[T])(implicit ctx: Context, m: Manifest[T]) {
    def toJsonString = grater[T].toCompactJSONArray(l)
    def toJson = JJson.toValue(grater[T].toJSONArray(l))
  }

  implicit class CursorToJson[T <: BaseEntity](l: SalatMongoCursor[T])(implicit ctx: Context, m: Manifest[T]) {
    def toJsonString = grater[T].toCompactJSONArray(l.toTraversable)
  }

  implicit class EntityToJson[T <: BaseEntity](e: T)(implicit ctx: Context, m: Manifest[T]) {
    def toJsonString = grater[T].toCompactJSON(e)
    def toJson = JJson.toValue(grater[T].toJSON(e))
  }
}