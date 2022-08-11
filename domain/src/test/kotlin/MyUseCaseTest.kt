import com.example.domain.MyUseCase
import org.junit.Assert.*
import org.junit.Test

class MyUseCaseTest {

    @Test
    fun testIsDone() {
        assertEquals("done", MyUseCase().doSomething())
    }

}
