# Content Distributor

An experiment.

Tries to provide a (short) in-memory, remote queue for multiple producers and consumers. Features:

1. Messages have a key. If a key/value pair appears before a message is consumed, the value is updated.
2. There is a maximum number of messages in-flight.
3. Consumers shall ackknowledge processing of a message. The ack is send back to the producer.
4. Acks time out.
5. For a given key, values are guaranteed to not overtake each other. Rationale: only the latest message is relevant. Earlier messages, if not yet processed, can be skipped, but at no time an older message shall be processed.
