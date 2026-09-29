import org.koin.dsl.koinApplication
import org.koin.dsl.module

class Repository
class Service(val repository: Repository)

fun main() {
    val application = koinApplication {
        modules(module {
            single { Repository() }
            factory { Service(get()) }
        })
    }
    try {
        val first = application.koin.get<Service>()
        val second = application.koin.get<Service>()
        check(first !== second)
        check(first.repository === second.repository)
    } finally {
        application.close()
    }
}
