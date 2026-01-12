package ru.otus.otuskotlin.marketplace.backend.repository.gremlin

import com.arcadedb.gremlin.ArcadeGraphFactory
import com.arcadedb.query.sql.executor.ResultSet
import com.arcadedb.remote.RemoteDatabase
import com.arcadedb.remote.RemoteServer
import org.apache.tinkerpop.gremlin.driver.AuthProperties
import org.apache.tinkerpop.gremlin.driver.Cluster
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal
import org.apache.tinkerpop.gremlin.process.traversal.P.gte
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.junit.Test
import kotlin.test.Ignore
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__ as gr

/**
 * Для запуска тестов этого файла требуется запустить локальный экземпляр ArcadeDB
 * Можно использовать файл /deploy/docker-compose-arcadedb.yml
 */
@Ignore("Тест для экспериментов")
class SimpleTest {
    private val host: String = "localhost"
    private val user: String = "root"
    private val pass: String = "root_root"

    private val dbName: String = "graph"
//     private val dbName: String = "mkpl" // Этот граф должен быть настроен в /home/arcadedb/config/gremlin-server.groovy

    private val aPort: Int = 2480 // Порт для интерфейса ArcadeDb
    private val gPort: Int = 8182 // Порт для интерфейса Apache Tinkerpop Gremlin

    /**
     * Пример запроса без использования библиотеки Gremlin.
     * Дает доступ к другим графам данных без ограничений.
     */
    @Test
    fun arcadeSimple() {
        val server = RemoteServer(host, aPort, user, pass)
        if (!server.exists(dbName)) {
            server.create(dbName)
        }
        assert(server.databases().contains(dbName))
        RemoteDatabase(host, aPort, dbName, user, pass).use { db ->
            val res: ResultSet = db.command("gremlin", "g.V().elementMap().toList()")
            res.forEachRemaining { block ->
                val vxList = block.getProperty<List<Map<String, String>>>("result")
                vxList.forEach { vx ->
                    println("V:")
                    vx.forEach { p ->
                        println("  ${p.key}=${p.value}")
                    }
                }
            }
        }
    }

    /**
     * Организуем соединение с БД средствами ArcadeDB, выполняем запросы с помощью Gremlin
     * Доступ по умолчанию только к graph.
     */
    @Test
    fun arcadeConnection() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { pool ->
            pool.get().use { graph ->
                val userId = graph.traversal()
                    .addV("User")
                    .property(VertexProperty.Cardinality.single, "name", "Evan")
                    .next()
                    .id()
                println("UserID: $userId")
            }
        }
    }

    /**
     * Работа только средствами Gremlin
     */
    @Test
    fun gremlinConnection() {
        val authProp = AuthProperties().apply {
            with(AuthProperties.Property.USERNAME, user)
            with(AuthProperties.Property.PASSWORD, pass)
        }
        val cluster = Cluster.build()
            .addContactPoints(host)
            .port(gPort)
            .authProperties(authProp)
            .create()
        traversal()
            //               Этот граф должен быть указан в /home/arcadedb/config/gremlin-server.groovy
            .withRemote(DriverRemoteConnection.using(cluster, dbName))
            .use { g ->
                val user = g
                    .addV("User")
                    .property(VertexProperty.Cardinality.single, "name", "Evan")
                    .next()
                val userId = user.id()
                println("UserID: $userId")
            }
    }

    /*
        Vertices:
            Person(name, age, city)
            Post(title, text, date)

        Edges:
            FRIEND
            LIKES
            POSTED
     */
    @Test
    fun `define domain model example`() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { graphFactory ->
            graphFactory.get().use { graph ->
                // VERTICES
                val alice = graph.traversal().addV("Person").property("name", "Alice").property("age", 30)
                    .property("city", "Moscow").next()
                val bob = graph.traversal().addV("Person").property("name", "Bob").property("age", 32)
                    .property("city", "Saint-Petersburg").next()
                val carol = graph.traversal().addV("Person").property("name", "Carol").property("age", 38)
                    .property("city", "Ekaterinburg").next()
                val dave = graph.traversal().addV("Person").property("name", "Dave").property("age", 25)
                    .property("city", "Samara").next()
                val eve = graph.traversal().addV("Person").property("name", "Eve").property("age", 27)
                    .property("city", "Kazan").next()
                listOf(alice, bob, carol, dave, eve).forEach { println("Person ID: ${it.id()}") }

                val p1 = graph.traversal().addV("Post").property("title", "Hello Graphs")
                    .property("text", "Graphs are awesome!").property("date", "2025-10-13").next()
                val p2 = graph.traversal().addV("Post").property("title", "Gremlin Tricks")
                    .property("text", "10 cool Gremlin traversals").property("date", "2025-10-11").next()
                val p3 = graph.traversal().addV("Post").property("title", "ArcadeDB Tips")
                    .property("text", "Best practices for schema-less mode").property("date", "2025-10-10").next()
                val p4 = graph.traversal().addV("Post").property("title", "Graph vs SQL")
                    .property("text", "Why graphs beat joins").property("date", "2025-10-09").next()
                val p5 = graph.traversal().addV("Post").property("title", "Tech Scene")
                    .property("text", "Why Kotlin rocks!").property("date", "2025-10-07").next()
                listOf(p1, p2, p3, p4, p5).forEach { println("Post ID: ${it.id()}") }
                // FRIEND EDGES
                graph.traversal().addE("FRIEND").from(alice).to(bob).next()
                graph.traversal().addE("FRIEND").from(bob).to(carol).next()
                graph.traversal().addE("FRIEND").from(carol).to(dave).next()
                graph.traversal().addE("FRIEND").from(bob).to(eve).next()
                // POST EDGES
                graph.traversal().addE("POSTED").from(alice).to(p1).next()
                graph.traversal().addE("POSTED").from(bob).to(p2).next()
                graph.traversal().addE("POSTED").from(carol).to(p3).next()
                graph.traversal().addE("POSTED").from(dave).to(p4).next()
                graph.traversal().addE("POSTED").from(alice).to(p5).next()
                // LIKES
                // Bob и Carol лайкнули пост Алисы
                graph.traversal().addE("LIKES").from(bob).to(p1).next()
                graph.traversal().addE("LIKES").from(carol).to(p1).next()
                // Dave лайкнул пост Bob’а
                graph.traversal().addE("LIKES").from(dave).to(p2).next()
                // Eve и Alice лайкнули пост Carol
                graph.traversal().addE("LIKES").from(eve).to(p3).next()
                graph.traversal().addE("LIKES").from(alice).to(p3).next()
                // Carol и Eve лайкнули пост Dave
                graph.traversal().addE("LIKES").from(carol).to(p4).next()
                graph.traversal().addE("LIKES").from(eve).to(p4).next()
                // Bob лайкнул второй пост Алисы
                graph.traversal().addE("LIKES").from(bob).to(p5).next()
            }
        }
    }

    // Базовая выборка
    @Test
    fun `all posts alice friends liked`() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { graphFactory ->
            graphFactory.get().use { graph ->
                graph.traversal().V().has("Person", "name", "Alice")
                    .out("FRIEND")
                    .out("LIKES")
                    .values<String>("title")
                    .forEach { println("Post title: $it") }
            }
        }
    }


    // Демонстрация Project и by()
    @Test
    fun `influencer stats`() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { graphFactory ->
            graphFactory.get().use { graph ->
                graph.traversal().V()
                    .hasLabel("Person")
                    .project<Map<String, Any>>("name", "likes")
                    .by("name") // поле "name" заполняется значением свойства name вершины
                    .by(gr.out("POSTED").`in`("LIKES").count()) // поле "likes" = количество лайков постов
                    .toList()
                    .forEach { println(it) }
            }
        }
    }

    // Демонстрация условных запросов choose() (a.k.a if - else)
    @Test
    fun `conditional query`() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { graphFactory ->
            graphFactory.get().use { graph ->
                graph.traversal().V()
                    .hasLabel("Person")
                    .choose(
                        gr.values<Vertex, Int>("age").`is`(gte(30)), // условие: age >= 30
                        gr.out("FRIEND").values("name"), // если true → друзья
                        gr.out("POSTED").values<String>("title")  // если false → посты
                    )
                    .toList()
                    .forEach { println(it) }
            }
        }
    }

    @Test
    fun `closest friends to Alice`() {
        ArcadeGraphFactory.withRemote(host, aPort, dbName, user, pass).use { graphFactory ->
            graphFactory.get().use { graph ->
                val closestFriends = graph.traversal().V()
                    .hasLabel("Person").has("name", "Alice")
                    .repeat(gr.out("FRIEND")).times(2)
                    .dedup()
                    .values<String>("name")
                    .toList()
                    .toList()
                println("Через 2 рукопожатия: $closestFriends")
            }
        }
    }
}
