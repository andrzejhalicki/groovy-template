import com.sap.gateway.ip.core.customdev.util.Message
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import spock.lang.Shared
import spock.lang.Specification

class CreateXMLSpec extends Specification {

    @Shared Script script
    @Shared CamelContext context

    Message msg
    Exchange exchange

    def setupSpec() {
        Binding binding = new Binding()
        binding.setProperty('messageLogFactory', null)
        GroovyShell shell = new GroovyShell(binding)
        script = shell.parse(this.getClass().getResource('/script/CreateXML.groovy').toURI())
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

    def 'should convert JSON orders to XML'() {
        given: 'a JSON payload with 3 coffee orders'
        setBody(this.getClass().getResource('/unit-test/data/data.json').newInputStream())

        when: 'the script transforms the JSON'
        script.processData(msg)

        then: 'output is XML with 3 order elements and the cafe attribute'
        def root = new XmlSlurper().parseText(msg.getBody() as String)
        root.name() == 'orders'
        root.@cafe == 'The Daily Grind'
        root.order.size() == 3
    }

    def 'should calculate total order price as an attribute'() {
        given:
        setBody(this.getClass().getResource('/unit-test/data/data.json').newInputStream())

        when:
        script.processData(msg)

        then: '3.50 + 5.50 + 4.25 = 13.25'
        def root = new XmlSlurper().parseText(msg.getBody() as String)
        root.@total.toDouble() == 13.25
    }

    // =========================================================================
    // XmlParser: builds an EAGER Node tree
    //   - Returns groovy.util.Node objects
    //   - Children are NodeLists — need explicit [0] to get a single Node
    //   - Attributes accessed via map key syntax: node['@attr']
    // =========================================================================
    def 'XmlParser — eager Node tree, explicit indexing'() {
        given:
        setBody(this.getClass().getResource('/unit-test/data/data.json').newInputStream())
        script.processData(msg)

        when: 'parsed with XmlParser'
        Node root = new XmlParser().parseText(msg.getBody() as String)

        then:
        root.name() == 'orders'
        root['@cafe'] == 'The Daily Grind'            // attribute via map key
        root['@date'] == '2024-01-15'

        root.order.size() == 3
        root.order[0].customer[0].text() == 'Alice'   // NodeList → [0] → Node → .text()
        root.order[0].drink[0].text()    == 'Espresso'
        root.order[0].drink[0].@size     == 'Small'   // attribute on Node

        root.order[1].customer[0].text() == 'Bob'
        root.order[1].drink[0].text()    == 'Latte'
        root.order[1].drink[0].@size     == 'Large'
    }

    // =========================================================================
    // XmlSlurper: builds a LAZY GPathResult
    //   - Returns groovy.util.slurpersupport.GPathResult
    //   - .text() works directly — no [0] unwrapping needed
    //   - Attributes accessed via @ shorthand: node.@attr
    // =========================================================================
    def 'XmlSlurper — lazy GPathResult, GPath shorthand'() {
        given:
        setBody(this.getClass().getResource('/unit-test/data/data.json').newInputStream())
        script.processData(msg)

        when: 'parsed with XmlSlurper'
        def root = new XmlSlurper().parseText(msg.getBody() as String)

        then:
        root.name() == 'orders'
        root.@cafe == 'The Daily Grind'               // attribute via @ shorthand
        root.@date == '2024-01-15'

        root.order.size() == 3
        root.order[0].customer.text() == 'Alice'      // GPathResult — no [0] needed
        root.order[0].drink.text()    == 'Espresso'
        root.order[0].drink.@size     == 'Small'      // attribute via @ shorthand

        root.order[1].customer.text() == 'Bob'
        root.order[1].drink.text()    == 'Latte'
        root.order[1].drink.@size     == 'Large'
    }
}
