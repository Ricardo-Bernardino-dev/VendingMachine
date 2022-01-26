package main

import isel.leic.UsbPort

object HAL {
    private var input = 0x00
    private var out = 0x00
    fun init() {
        input = 0x00
        clrBits(0xff)
    }

    fun output(out: Int) {
        HAL.out = out
        UsbPort.out(out.inv())
    }

    fun isBit(mask: Int): Boolean {
        return (readBits(mask) == mask)
    }

    fun readBits(mask: Int): Int {
        return mask and UsbPort.`in`().inv()
    }

    fun writeBits(mask: Int, value: Int) {
        output((mask.inv().and(out)).or(value.and(mask)))
    }

    fun setBits(mask: Int) {
        out = out.or(mask)
        UsbPort.out(out.inv())
    }

    fun clrBits(mask: Int) {
        output(out.and(mask.inv()))
    }
}