package nl.amc.ebioscience.rosemary.controllers.api

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import nl.amc.ebioscience.rosemary.models._
import nl.amc.ebioscience.rosemary.models.core._
import nl.amc.ebioscience.rosemary.models.core.ModelBase._
import nl.amc.ebioscience.rosemary.models.core.Implicits._
import nl.amc.ebioscience.rosemary.controllers.JsonHelpers
import nl.amc.ebioscience.rosemary.services.SecurityService

@Singleton
class ResourcesController @Inject() (securityService: SecurityService) extends Controller with JsonHelpers {

  def index = securityService.HasToken(parse.empty) { implicit request =>
    Ok(Resource.findAll.toList.map(_.copy(username = None, password = None)).toJson)
  }

  case class CreateResourceRequest(
      name: String,
      kind: String,
      protocol: String,
      host: String,
      port: Option[Int],
      basePath: Option[String],
      username: Option[String], // Community username
      password: Option[String]) {

    def validateKind = kind.toLowerCase match {
      case "webdav" => Right(ResourceKind.Webdav)
      case "irods"  => Right(ResourceKind.Irods)
      case k @ _    => Left(s"Unsupported kind $k")
    }

    def validateProtocol = protocol.toLowerCase match {
      case p @ ("http" | "https") => Right(p)
      case p @ _                  => Left(s"Unsupported protocol $p")
    }
  }
  object CreateResourceRequest {
    implicit val createResourceRequestFmt = Json.format[CreateResourceRequest]
  }

  def create = Action(parse.json) { implicit request =>
    val json = request.body
    Logger.trace("Request: " + json)
    json.validate[CreateResourceRequest].fold(
      valid = { req =>
        req.validateKind match {
          case Right(kind) => req.validateProtocol match {
            case Right(protocol) =>
              val res = Resource(
                name = req.name,
                kind = kind,
                protocol = protocol,
                host = req.host,
                port = req.port.getOrElse(80),
                basePath = req.basePath,
                username = req.username,
                password = req.password).insert
              Ok(res.toJson)
            case Left(error) => Conflict(error)
          }
          case Left(error) => Conflict(error)
        }
      },
      invalid = {
        errors => BadRequest(Json.toJson(errors))
      })
  }

  def queryId(id: Resource.Id) = securityService.HasToken(parse.empty) { implicit request =>
    Resource.findOneById(id).map { resource =>
      Ok(resource.toJson)
    } getOrElse Conflict(s"Could not find resource_id $id")
  }
}