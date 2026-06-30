import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

def Message processData(Message message) {
    def json = new JsonSlurper().parse(message.getBody(Reader))
    def coffeeshop = json.coffeeshop

    // TODO: calculate the total price of all orders (hint: use .sum { it.price as Double })

    def outputStream = new ByteArrayOutputStream()
    def writer = new OutputStreamWriter(outputStream, 'UTF-8')
    def builder = new MarkupBuilder(writer)

    // TODO: add total as an attribute on the orders element
    builder.orders(cafe: coffeeshop.name, date: coffeeshop.date) {
        coffeeshop.orders.each { o ->
            order(id: o.id) {
                customer(o.customer)
                // TODO: add a drink element — it has a size attribute and the drink name as text
                // TODO: add a price element
            }
        }
    }

    message.setBody(outputStream.toString('UTF-8'))
    return message
}
