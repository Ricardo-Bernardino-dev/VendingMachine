package main

import isel.leic.utils.Time

fun main() {
    HAL.init()
    SerialEmitter.init()
    LCD.init()
    //  KBD.init()
    LCD.writeS("H")
    Time.sleep(3000)

}

object LCD { // Escreve no LCD usando a interface a 4 bits.

    private const val enableMask = 0x20 //(0010 0000) //input
    private const val rsMask = 0x10 //(0001 0000) //input
    private const val dataMask = 0x0F //(0000 1111) //output
    private const val writeNibbleSelect = false
    private var INIT_STATE = false

    private fun writeNibble(rs: Boolean, data: Int) {
        if (writeNibbleSelect) writeNibbleParalel(rs, data)
        else writeNibbleSerial(rs, data)
    }

    private fun writeNibbleParalel(rs: Boolean, data: Int) {
        if (rs)
            HAL.setBits(rsMask)
        else
            HAL.clrBits(rsMask)
        HAL.setBits(enableMask)
        HAL.writeBits(dataMask, data)
        HAL.clrBits(enableMask)
        Time.sleep(5)
    }

    private fun writeNibbleSerial(rs: Boolean, data: Int) {
        val input = data shl 1
        if (rs) SerialEmitter.send(SerialEmitter.Destination.LCD, input or 1)
        else SerialEmitter.send(SerialEmitter.Destination.LCD, input or 0)
    }

    private fun writeByte(rs: Boolean, data: Int) {
        writeNibble(rs, data shr 4)
        writeNibble(rs, data)
        //Time.sleep(100)
    }

    fun writeCMD(data: Int) {
        writeByte(false, data)
    }

    private fun writeDATA(data: Int) {
        writeByte(true, data)
    }


    fun init() {
        if (INIT_STATE) return
        Time.sleep(15) //60   15
        writeNibble(false, 0x03)
        Time.sleep(15) //5  15
        writeNibble(false, 0x03)
        Time.sleep(5) //1   5
        writeNibble(false, 0x03)
        writeNibble(false, 0x02)
        writeCMD(0x28)
        writeCMD(0x08)
        writeCMD(0x01)
        writeCMD(0x06)
        writeCMD(0x0F)
        Time.sleep(20)
        INIT_STATE = true
    }

    fun writeC(c: Char) {
        writeDATA(c.code)
    }

    fun writeS(text: String) {
        for (i in text.indices) {
            writeC(text[i])
        }
    }

    fun cursor(line: Int, column: Int) {
        var d = 0
        if (line == 0) d = 128 + column
        else d = 192 + column
        writeByte(false, d)
    }

    fun clear() {
        writeCMD(0x01)
        cursor(0, 0)
        Time.sleep(10)
    }
}



