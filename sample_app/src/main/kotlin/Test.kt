import com.tritus.persist.Persist

@Persist
interface Test {
    val name: String
    val description: String
}