/*
 * Copyright (C) 2016  Academic Medical Center of the University of Amsterdam (AMC)
 * 
 * This program is semi-free software: you can redistribute it and/or modify it
 * under the terms of the Rosemary license. You may obtain a copy of this
 * license at:
 * 
 * https://github.com/AMCeScience/Rosemary-Vanilla/blob/master/LICENSE.md
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * You should have received a copy of the Rosemary license
 * along with this program. If not, 
 * see https://github.com/AMCeScience/Rosemary-Vanilla/blob/master/LICENSE.md.
 * 
 *        Project: https://github.com/AMCeScience/Rosemary-Vanilla
 *        AMC eScience Website: http://www.ebioscience.amc.nl/
 */
package nl.amc.ebioscience.rosemary.models

import nl.amc.ebioscience.rosemary.models.core._
import nl.amc.ebioscience.rosemary.models.core.ModelBase._
import nl.amc.ebioscience.rosemary.models.core.Implicits._
import com.mongodb.casbah.Imports._
import java.util.Date
import play.api.Logger

case class ProcessingGroup(
    name: String,
    initiator: User.Id,
    inputs: Seq[ParamOrDatum] = Seq.empty,
    outputs: Seq[ParamOrDatum] = Seq.empty,
    recipes: Set[Recipe.Id],
    executionDate: Date = new Date(),
    tags: Set[Tag.Id] = Set.empty, // To relate this processing to a workspace (WorkspaceTag) or send via a message (MessageTag)
    progress: Int = 0,
    statuses: Seq[Status] = Nil,
    id: ProcessingGroup.Id = new ProcessingGroup.Id,
    info: Info = new Info) extends ProcessingBase { // TODO Index and search ProcessingGroups

  lazy val processings = ProcessingGroup.processings.findByParentId(id).toList
}

object ProcessingGroup extends DefaultModelBase[ProcessingGroup]("processingGroups")
    with TagsQueries[ProcessingGroup] with ProcessingIOQueries[ProcessingGroup] {

  val processings = new DefaultModelChild[Processing]("processings")
}
