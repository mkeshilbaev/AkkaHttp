package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.example.UserRegistry

//#import-json-formats
//#user-routes-class
class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats


  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getContacts(): Future[Contacts] =
    userRegistry.ask(GetContacts)
  def getContact(name: String): Future[GetContactResponse] =
    userRegistry.ask(GetContact(name, _))
  def createContact(user: Contact): Future[ActionPerformed] =
    userRegistry.ask(CreateContact(user, _))
  def updateContact(name: String): Future[ActionPerformed] =
    userRegistry.ask(UpdateContact(name, _))
  def deleteContact(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteContact(name, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("contacts") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {
              complete(getContacts())
            },
            post {
              entity(as[Contact]) { contact =>
                onSuccess(createContact(contact)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },

        //#users-get-delete
        //#users-get-post
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              rejectEmptyResponse {
                onSuccess(getContact(name)) { response =>
                  complete(response.maybeUser)
                }
              }
              //#retrieve-user-info
            },
            put {
              onSuccess(updateContact(name)){ performed =>
                complete((StatusCodes.OK, performed))
              }
            },
            delete {
              //#users-delete-logic
              onSuccess(deleteContact(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            })
        })
      //#users-get-delete
    }
  //#all-routes
}
