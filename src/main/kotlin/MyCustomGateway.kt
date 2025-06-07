import library.decodeHex
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun myCustomGateway(gateway: CreateDoipEntityFunc) {
    gateway("GATEWAY") {
        // The logical address for your gateway
        logicalAddress = 0x1010
        functionalAddress = 0xcafe.toShort()

        // VIN - will be padded to the right with 0 until 17 chars are reached, if left empty, 0xFF will be used
        vin = "MYVIN"
        // Define the entity id (defaults to 6 byte of 0x00), typically the MAC of an ECU
        eid = "101010101010".decodeHex()
        // Define the group id (defaults to 6 byte of 0x00), should be used to address a group of ecus when no vin is known
        gid = "909090909090".decodeHex()


        // You can now define how requests for the gateway should be handled

        // Now, let's see some Exact matches examples:
        // To start off, let's use a simple example, let's acknowledge the presence of a tester
        request("3E 00") { ack() }
        // This is semantically exactly the same as
        request("3E 00") { respond("7E 3E") }
        // As well as
        request(byteArrayOf(0x3E, 0x00)) { respond(byteArrayOf(0x7E, 0x3E)) }

        // We can also define a name for the request, which is used when the request gets logged
        request("3E 00", "TesterPresent") { ack() }

        // It's also possible to send nrc's
        request("3E 00") { nrc() }
        // Which is semantically the same as
        request("3e 00") { nrc(NrcError.GeneralReject) }
        // And again
        request("3E 00") { respond("7F 3E 10") }
        // As well as
        request(byteArrayOf(0x3E, 0x00)) { respond(byteArrayOf(0x7F, 0x3E, 0x10)) }

        // The matching can also be done by only defining the start of the request, using [] or .* will set the
        // onlyStartsWith = true flag

        // So this expression
        request("3e 00 []") { ack() }
        // is semantically the same as
        request(byteArrayOf(0x3E, 0x0), onlyStartsWith = true) { ack() }


        // At this point you could also programmatically open a csv-file, instead of defining each request,
        // read in a list of requests and responses, to transform them into request(..) { respond(...) } pairs.

        // But since csv-files are static, where's the fun in that?
        // Since everything in between {} is a lambda function that'll be executed when the request matches,
        // we can actually do useful things - as an example, let's maintain an ecu session state machine
        var ecuSession = SessionState.DEFAULT
        request("10 01") { ack(); ecuSession = SessionState.DEFAULT }
        request("10 02") { ack(); ecuSession = SessionState.PROGRAMMING }
        request("10 03") { ack(); ecuSession = SessionState.DIAGNOSTIC }
        request("10 04") { ack(); ecuSession = SessionState.SAFETY }

        // Now, let's say we can only HardReset if we're in the PROGRAMMING session
        request("11 01", "HardReset") {
            if (ecuSession == SessionState.PROGRAMMING) {
                ack()
            } else {
                nrc(NrcError.SecurityAccessDenied)
            }
        }

        // State checking could also be done with an extension function that uses the state, now every conditioned
        // response can just use the extension function to save you time from writing plenty of redundant code
        fun RequestResponseData.respondIfProgramming(response: RequestResponseHandler) {
            if (ecuSession == SessionState.PROGRAMMING) {
                response.invoke(this)
            } else {
                nrc(NrcError.SecurityAccessDenied)
            }
        }
        // Let's do this for the soft reset, as well as the key off on reset
        request("11 03") { respondIfProgramming { ack() } }
        request("11 02") { respondIfProgramming { ack() } }

        // Now we all know that an ecu doesn't respond while rebooting, that's something we can simulate too,
        // by using an interceptor. Interceptors run before all request matching and are described below
        request("11 01", "HardReset") {
            ack()
            addOrReplaceEcuInterceptor("WaitWhileRebooting", 4000.milliseconds) { req ->
                // We could access the request and respond
                if (req.message[0] == 0xFF.toByte() || // Multiple ways to access the request message
                    this.message[0] == 0xFF.toByte() ||
                    this.request.message[0] == 0xFF.toByte()) {
                    // This case won't ever happen, just to show what's possible in this context
                    ack()
                    // You could respond in the same ways as shown in the gateway examples
                }

                // When true is returned, all request matching is forfeited, and no matching will
                // be executed. When all interceptors return false, the normal request matching will
                // commence afterward
                true

                // This is also a pretty powerful tool for testing - you could start an interceptor that records all
                // calls to an ecu with an uds request (or run REST-webservices on a different port for this),
                // return all recorded calls with another one and use a third one to delete the
                // data/stop the interceptor. This enables you to text the exact commands/messages sent to the ecu
                // in an integration test.
            }
        }

        // To integration-test code that uses this simulated ecu, we could define a custom write-service with
        // a parameter, which could introduce specific error conditions through state, until reset by a
        // different parameter

        // Misc. commands:

        // You can also decide to not handle it in this handler and continue calling other matches by calling continueMatching()
        request("3E 00") { if (true) continueMatching() }

        // You can also set and replace timers -
        // e.g. to reset your session to default when no tester present was sent in 5 seconds
        request("3E 00") {
            ack()
            addOrReplaceEcuTimer("RESETSESSION", 5.seconds) {
                ecuSession = SessionState.DEFAULT
            }
        }

        // Remember the session state example earlier?
        // If we do it that way, we actually have a hard time resetting the ecu into a defined initial state.
        // To get around this, you can save state in a storage container associated with the ecu or request, which are
        // persistent across different requests (ecu), or within multiple consecutive requests (caller)
        //
        // These storages are reset, when the reset() method is called on the ecu or request
        @Suppress("UNUSED_CHANGED_VALUE")
        request("10 02") {
            var sessionState by ecu.storedProperty { SessionState.DEFAULT } // {} contains the initial value that'll be used when the property is initialized
            if (sessionState != SessionState.PROGRAMMING) {
                sessionState = SessionState.PROGRAMMING
            }
            ack()
            // this session state is persisted for the ecu, so if you get it in another request, the last
            // set value will be the current one (until reset is called)

            // We can also do this on the request level by using caller instead of ecu
            var requestCounter: Int by caller.storedProperty { 0 }
            requestCounter++
            // It's named "caller", because the exact same concept also applies to interceptors

            // to reset the storage for the request
//            caller.reset()

//            ecu.reset()
            // to reset the storage for the ecu (and all its requests)
            // this can be pretty nifty when you want to reset your state after each integration test
            // with a custom write command
        }

        // You can also respond with a sequence, advancing in the list of responses with each matched request

        // When the end of the responses-list is reached, either stop at the end and repeat the last answer until reset is called
        request("3E 00") {
            sequenceStopAtEnd(
                "7E 00",
                "7F 10"
            )
        }

        // Or wrap around to the beginning when the end has been reached (reset will reset the sequence to the first entry)
        request("3E 00") {
            sequenceWrapAround(
                "7E 00",
                "7F 10"
            )
        }

        // Since usually there are other ECUs behind a gateway, we can define them too
        // please note, that no vam will be sent for them, unless you specify
        // the additionalVam property

        // Either by directly adding them
        ecu("ECU1") {
            logicalAddress = 0x1111
            functionalAddress = 0xcafe.toShort()
            // Optional - when no request is matched, automatically send out of range nrc (default true)
            nrcOnNoMatch = true

            // Same requests and logic as in gateway
            request("10 01") { ack() }
        }

        // Since stacking all the requests for multiple ecus inside a single file would get a bit too large,
        // we can also call functions  with the ecu-creator reference, to add them indirectly
        exampleEcu2(::ecu)
    }
}

fun exampleEcu2(ecu: CreateEcuFunc) {
    ecu("EXAMPLEECU2") {
        logicalAddress = 0x2211
        functionalAddress = 0xcafe.toShort()

        request("10 01") { ack() }
    }
}

enum class SessionState {
    DEFAULT,
    PROGRAMMING,
    DIAGNOSTIC,
    SAFETY
}
