package mgonzalez.clients

import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.update.Update
import mgonzalez.BaseSpec

import java.util.UUID

class PostgresClientSpec extends BaseSpec:
  private val _xa = ResourceFixture(PostgresClient.transactor("postgres", "postgres"))

  case class Person(id: UUID, name: String, fields: Map[String, String])

  _xa.test("test connectivity") { xa =>
    val tableName = "some_table"
    val select    = (sql"select id, name, fields from " ++ Fragment.const(tableName)).query[Person]

    val p1 = Person(UUID.randomUUID(), "Mauro", Map("age" -> "32"))
    val insert = {
      val sql = s"insert into $tableName(id, name, fields) values (?, ?, ?)"
      Update[Person](sql).updateMany(p1 :: Nil)
    }
    val res = for {
      _       <- insert
      persons <- select.stream.compile.toList
    } yield persons

    res.transact(xa).map(_.contains(p1))

  }
