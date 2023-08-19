import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void sayHello() {
        Assertions.assertEquals("Hello", new Main().sayHello());
    }
}