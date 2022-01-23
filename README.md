# Custom DoIP ECU Example

#### get started

Read the example [MyCustomGateway.kt](src/main/kotlin/MyCustomGateway.kt) to understand how the DSL and 
its features work. 

When you want to create your own simulated ECUs:
1. Copy this project into your own space
2. Change `build.gradle.kts` to your needs (project name, etc..)
3. Define your gateway/ecu topology, change names, addresses and write your requests/responses
4. Run `../gradlew run` (or use `../gradlew shadowJar` for a nice standalone shadow jar)
5. Profit


#### license

The contents of this repository itself are released under the CC0-license and into the public domain.
