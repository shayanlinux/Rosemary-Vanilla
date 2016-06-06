package nl.amc.ebioscience.rosemary.models

import nl.amc.ebioscience.rosemary.models.core._
import nl.amc.ebioscience.rosemary.models.core.ModelBase._
import nl.amc.ebioscience.rosemary.models.core.Implicits._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import nl.amc.ebioscience.processingmanager.types.ProcessingLifeCycle

@Salat
sealed trait Notification extends BaseEntity with WithTags {
}

/** {actor} {action} {affected} to/from {workspace} */
case class UserWorkspaceNotification(
  actor: User.Id,
  action: String, // added | removed
  affected: User.Id,
  workspace: Tag.Id, // WorkspaceTag
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info) extends Notification

/** {actor} imported {summary} from {resource} to {workspace}, see {imported} */
case class ImportNotification(
  actor: User.Id,
  resource: Resource.Id,
  workspace: Tag.Id, // WorkspaceTag
  imported: Tag.Id, // SystemTag
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info // 1 project, 20 subjects, 20 image sessions, 80 scans, 160 files
  ) extends Notification

/** New data is available */
case class NewDataNotification(
  processing: ProcessingGroup.Id,
  newdata: Tag.Id, // SystemTag
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info) extends Notification

/** {actor} {action} {processing} */
case class UserProcessingNotification(
  actor: User.Id,
  action: String, // submitted | resumed | cancled
  processing: ProcessingGroup.Id,
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info) extends Notification

/** {processing} is {status} */
case class ProcessingNotification(
  processing: ProcessingGroup.Id,
  status: ProcessingLifeCycle.Value,
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info) extends BaseEntity with Notification

/** {actor} sent a {message} [with {} data and {} processings] to {receivers} */
case class MessageNotification(
  actor: Option[User.Id], // if None, it's System
  message: Tag.Id, // MessageTag
  thread: Thread.Id,
  receivers: Set[User.Id], // this might be different than watchers
  tags: Set[Tag.Id],
  id: Notification.Id = new Notification.Id,
  info: Info = new Info) extends Notification

object Notification extends DefaultModelBase[Notification]("notifications") with TagsQueries[Notification] {

  collection.ensureIndex(("_id" -> 1, "_t" -> 1), ("default_language" -> "none"))
}
