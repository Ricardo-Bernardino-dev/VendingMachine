package main

import isel.leic.utils.Time

fun main() {
    HAL.init()
    LCD.init()
    SerialEmitter.init()
    SerialEmitter.send(SerialEmitter.Destination.LCD, 0x03)
    Time.sleep(3000)
    //LCD.writeS("Hello")
    //Time.sleep(3000)
}

object SerialEmitter { // Envia tramas para o módulo Serial Receiver
    enum class Destination { DISPENSER, LCD }

    private const val LCD_SELECT_MASK: Int = 0x08 // 0000 1000
    private const val DISPENSER_SELECT_MASK: Int = 0x40  // 0100 0000
    private const val SDX_MASK: Int = 0x01 // 0000 0001 //output
    private const val SCLK_MASK: Int = 0x02 //0000 0010 //output
    private const val BUSY: Int = 0x80 // 1000 0000 //input

    private val DESTINATION_MASKS = arrayOf(DISPENSER_SELECT_MASK, LCD_SELECT_MASK)

    // Inicia a classe
    fun init() {
        HAL.init()
        HAL.setBits(SDX_MASK)
        HAL.clrBits(SCLK_MASK)
    }

    // Envia uma trama para o Serial Receiver identificando o destino em addr e os bits de dados em‘data’.
    fun send(addr: Destination, data: Int) {
        while (isBusy()) {
        }
        HAL.clrBits(SCLK_MASK)
        HAL.clrBits(SDX_MASK)
        val destination: Int = DESTINATION_MASKS[addr.ordinal]
        val size: Int = if (destination == LCD_SELECT_MASK) 5 else 4
        var i = 0
        var numOfOnes: Int = 0
        var bit = 0
        var databit = data
        if (size == 5) {
            HAL.setBits(SDX_MASK)
            numOfOnes++
        }
        HAL.setBits(SCLK_MASK)
        while (i <= size - 1) {
            bit = 0x01 and (databit shr (i))
            if (bit != 0) numOfOnes++
            HAL.writeBits(SDX_MASK, bit)
            HAL.clrBits(SCLK_MASK)
            HAL.setBits(SCLK_MASK)
            i++
        }
        if (numOfOnes % 2 != 0) HAL.writeBits(SDX_MASK, 0)
        else HAL.writeBits(SDX_MASK, 1)
        HAL.clrBits(SCLK_MASK)
        HAL.setBits(SCLK_MASK)
        HAL.setBits(SDX_MASK)
        HAL.clrBits(SCLK_MASK)
    }

    fun isBusy(): Boolean {
        return HAL.isBit(BUSY)
    }
}