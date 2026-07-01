import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonBuilder
import groovy.json.JsonOutput

def Message processData(Message message) {
    //What's the difference between XmlSlurper and XmlParser?
    def xml = new XmlSlurper().parse(message.getBody(Reader))

    def items = []
    xml.category.each { category ->
        category.item.each { item ->
            items << [
                id  : item.@id.text(),
                name: item.name.text(),
                // TODO: add description
                // TODO: add category name (hint: it comes from the parent category element, not the item)
                // TODO: add sizes as a list of maps — each size has a name and a price (Double)
            ]
        }
    }

    def builder = new JsonBuilder()
    builder {
        cafe xml.@cafe.text()
        totalItems items.size()
        menu items
    }

    message.setBody(JsonOutput.prettyPrint(builder.toString()))
    return message
}
