package com.example

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable
import scala.concurrent.Future

//#user-case-classes
final case class Contact(name: String, phone: String, address: String)
final case class Contacts(contacts: immutable.Seq[Contact])
//#user-case-classes

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetContacts(replyTo: ActorRef[Contacts]) extends Command
  final case class CreateContact(contact: Contact, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetContact(name: String, replyTo: ActorRef[GetContactResponse]) extends Command
  final case class UpdateContact(name: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class DeleteContact(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetContactResponse(maybeUser: Option[Contact])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)


  private def registry(users: Set[Contact]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetContacts(replyTo) =>
        replyTo ! Contacts(users.toSeq)
        Behaviors.same
      case CreateContact(user, replyTo) =>
        replyTo ! ActionPerformed(s"Contact ${user.name} created.")
        registry(users + user)
      case GetContact(name, replyTo) =>
        replyTo ! GetContactResponse(users.find(_.name == name))
        Behaviors.same
      case UpdateContact(name, replyTo) =>
        replyTo ! ActionPerformed(s"Contact $name updated.")
        registry(users.filter(_.name == name))
      case DeleteContact(name, replyTo) =>
        replyTo ! ActionPerformed(s"Contact $name deleted.")
        registry(users.filterNot(_.name == name))
    }
}


//#user-registry-actor
//CRUD = Create Retrieve Update Delete