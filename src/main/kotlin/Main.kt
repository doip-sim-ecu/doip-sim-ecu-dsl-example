fun main() {
    // network defines the network interface for the doip entities, and how ip addresses are assigned to those
    // entities, as well as some configuration properties for the network (e.g. port, broadcasting)
    network {
        println("Custom Gateway")
        // Optional: Define the network interface the gateway should bind to
        // For multiple gateways, networkInterface should be an interface name with multiple ips assigned to it
        // networkMode AUTO will then automatically assign an ip to each doip entity/gateway
        networkInterface = "0.0.0.0"
        networkMode = NetworkMode.AUTO

        // Optional: Define the network port used for doip
        localPort = 13400

        myCustomGateway(::gateway)
    }
    start()
}
