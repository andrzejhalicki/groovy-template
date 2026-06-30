import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import spock.lang.Shared
import spock.lang.Specification

class CreateJSONSpec extends Specification {

    @Shared Script script
    @Shared CamelContext context

    Message msg
    Exchange exchange

    def setupSpec() {
        Binding binding = new Binding()
        binding.setProperty('messageLogFactory', null)
        GroovyShell shell = new GroovyShell(binding)
        script = shell.parse(this.getClass().getResource('/script/CreateJSON.groovy').toURI())
        context = new DefaultCamelContext()
    }

    def setup() {
        exchange = new DefaultExchange(context)
        msg = new Message(exchange)
    }

    def setBody(Object body) {
        exchange.getIn().setBody(body)
        msg.setBody(exchange.getIn().getBody())
    }

    def 'should convert XML coffee menu to JSON'() {
        given: 'an XML menu with 5 items across 2 categories'
        setBody(this.getClass().getResource('/unit-test/data/data.xml').newInputStream())

        when: 'the script transforms the XML'
        script.processData(msg)

        then: 'output is JSON with the cafe name and correct item count'
        def json = new JsonSlurper().parseText(msg.getBody() as String)
        json.cafe == 'The Daily Grind'
        json.totalItems == 5
        json.menu.size() == 5
    }

    def 'should carry category name onto each menu item'() {
        given:
        setBody(this.getClass().getResource('/unit-test/data/data.xml').newInputStream())

        when:
        script.processData(msg)

        then: 'items are grouped correctly by category'
        def json = new JsonSlurper().parseText(msg.getBody() as String)
        json.menu.findAll { it.category == 'Hot Drinks' }.size() == 3
        json.menu.findAll { it.category == 'Cold Drinks' }.size() == 2
    }

    def 'should map sizes as a nested array with price as Double'() {
        given:
        setBody(this.getClass().getResource('/unit-test/data/data.xml').newInputStream())

        when:
        script.processData(msg)

        then: 'Espresso has 2 sizes, both price values are Doubles'
        def json = new JsonSlurper().parseText(msg.getBody() as String)
        def espresso = json.menu.find { it.id == 'HD-01' }
        espresso.name == 'Espresso'
        espresso.sizes.size() == 2
        espresso.sizes[0].name == 'Small'
        espresso.sizes[0].price == 3.50
        espresso.sizes[1].name == 'Medium'
        espresso.sizes[1].price == 4.00
    }
}
