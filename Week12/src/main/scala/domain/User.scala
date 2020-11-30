package domain

case class User(id: String,
                name: String,
                login:String,
                password:String)

case class CreateUser(name: String,
                      login: String,
                      password: String)