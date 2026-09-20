package me.collumy.pointy.bitshift

object BitShiftRegistry {

    private val receivers = HashMap<Int, BitShiftReceiver>()

    fun register(receiver: BitShiftReceiver) {
        receivers[receiver.packetId] = receiver
    }

    fun unregister(receiver: BitShiftReceiver) {
        receivers.remove(receiver.packetId)
    }

    fun get(packetId: Int): BitShiftReceiver? = receivers[packetId]
}