import main.HAL
import main.KBD

fun main() {
    HAL.init()
    val usbIn = HAL.readBits(0x0f)
    val key = KBD.getKey()
    println(key)
    println(usbIn)
    //HAL.setBits(usbIn)
    //HAL.clrBits(0x01)
    // println(HAL.readBits(0x03))
    println(HAL.isBit(0x01))
    println(KBD.waitKey(3000))
    //HAL.clrBits(0xff)

}


/*
UsbPort.out(0x80)
main.KBD.init()
val key = main.KBD.waitKey(500000)
println(key)

 */
