package tornadofx.tests

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import tornadofx.*
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PropertiesTest {
    @Test
    fun quickTest() {
        val fred = object {
            val dateProperty = NonNullProperty<LocalDate>(LocalDate.now())
            var date by dateProperty  // is LocalDate
            val systemDateProperty = NonNullProperty(LocalDate.now())
            var systemDate by systemDateProperty  // is LocalDate!, but still null safe
            val nullableProperty = SimpleObjectProperty<LocalDate?>(LocalDate.MAX)
            var nullable by nullableProperty  // is LocalDate?
        }

        // fred.date = null  // Compile error
        fred.systemDate = null
        assertNotNull(fred.systemDate)
        assertNotNull(fred.systemDateProperty.value)

        fred.dateProperty.set(null)
        assertNotNull(fred.date)
        assertNotNull(fred.dateProperty.value)
        fred.date = LocalDate.MIN
        assert(fred.dateProperty.value == LocalDate.MIN)

        fred.dateProperty.bind(fred.nullableProperty)
        fred.nullableProperty.value = null
        assertNull(fred.nullable)
        assertNotNull(fred.date)
        assert(fred.dateProperty.isBound)
    }

    @Test
    fun property_delegation() {
        // given:
        val fixture = object {
            val nameProperty = SimpleStringProperty("Alice")
            var name by nameProperty

            val ageProperty = SimpleDoubleProperty(1.0)
            var age by ageProperty

            val dirtyProperty = SimpleBooleanProperty(false)
            var dirty by dirtyProperty
        }
        // expect:
        assertEquals("Alice", fixture.name)
        assertEquals("Alice", fixture.nameProperty.value)

        assertEquals(1.0, fixture.age)
        assertEquals(1.0, fixture.ageProperty.value)

        assertEquals(false, fixture.dirty)
        assertEquals(false, fixture.dirtyProperty.value)

        // when:
        fixture.name = "Bob"
        fixture.age = 100.0
        fixture.dirty = true

        // expect:
        assertEquals("Bob", fixture.name)
        assertEquals("Bob", fixture.nameProperty.value)

        assertEquals(100.0, fixture.age)
        assertEquals(100.0, fixture.ageProperty.value)

        assertEquals(true, fixture.dirty)
        assertEquals(true, fixture.dirtyProperty.value)
    }

    @Test
    fun property_get_should_read_value() {
        // given:
        val fixture = object {
            val string by property<String>()
            val integer: Int? by property()
            val stringDefault by property("foo")
            val integerDefault by property(42)
        }

        // expect:
        assertEquals(null, fixture.string)
        assertEquals(null, fixture.integer)
        assertEquals("foo", fixture.stringDefault)
        assertEquals(42, fixture.integerDefault)
    }

    @Test
    fun property_set_should_write_value() {
        // given:
        val fixture = object {
            var string by property<String>()
            var integer: Int? by property()
            var stringDefault by property("foo")
            var integerDefault by property(42)
        }

        // when:
        fixture.string = "foo"
        fixture.integer = 42

        // then:
        assertEquals("foo", fixture.string)
        assertEquals("foo", fixture.stringDefault)
        assertEquals(42, fixture.integer)
        assertEquals(42, fixture.integerDefault)
    }

    class TestClass {
        var myProperty: String by singleAssign()
    }

    @Test
    fun failNoAssignment() {
        val instance = TestClass()
        var failed = false

        try {
            instance.myProperty
        } catch (e: Exception) {
            failed = true
        }
        assertTrue(failed)
    }

    @Test
    fun succeedAssignment() {
        val instance = TestClass()
        instance.myProperty = "foo"
        instance.myProperty
    }

    @Test
    fun failDoubleAssignment() {

        val instance = TestClass()
        var failed = false
        instance.myProperty = "foo"

        try {
            instance.myProperty = "bar"
        } catch (e: Exception) {
            failed = true
        }
        assertTrue(failed)
    }

    @Test
    fun pojoWritableObservable() {
        val person = JavaPerson()
        person.id = 1
        person.name = "John"

        val idObservable = person.observable(JavaPerson::getId, JavaPerson::setId)
        val nameObservable = person.observable<String>("name")

        idObservable.value = 44
        nameObservable.value = "Doe"

        Assert.assertEquals(44, person.id)
        Assert.assertEquals("Doe", person.name)

        person.id = 5
        idObservable.refresh()
        Assert.assertEquals(5, idObservable.value)
    }

    @Test fun property_on_change() {
        var called = false
        val property = SimpleStringProperty("Hello World")
        property.onChange { called = true }
        property.value = "Changed"
        assertTrue(called)
    }
}
