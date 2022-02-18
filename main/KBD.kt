package main

import isel.leic.utils.Time
import main.UI.HAL

object KBD {
    const val NONE = 0.toChar()
    const val DVAL = 0x10   //(0001 0000)
    const val ACK = 0x80    // (1000 0000)
    const val readKey = 0x0F// (0000 1111)
    val keys = charArrayOf( '1', '4', '7', '*', '2', '5', '8', '0', '3', '6', '9', '#', NONE, NONE, NONE, NONE ) //Usar com teclado hardware
    //val keys = charArrayOf('1','2','3','4','5','6','7','8','9','*','0','#',NONE,NONE,NONE,NONE) //Usar em simulação

    fun init() {
        HAL.init()
    }

    fun getKey(): Char {
        var key = NONE
        if (HAL.isBit(DVAL)) {
            key = keys[HAL.readBits(readKey)]
            HAL.setBits(ACK)
            while (HAL.isBit(DVAL));
            HAL.clrBits(ACK)
        }
        return (key)
    }

    fun waitKey(timeout: Long): Char {
        val waitTime = Time.getTimeInMillis() + timeout
        while (Time.getTimeInMillis() <= waitTime) {
            val k = getKey()
            if (k != NONE) return k
        }
        return NONE
    }
}

fun main() {
    HAL.init()
    while (true) {
        val key = KBD.waitKey(5000)
        if (key != KBD.NONE) print(key)
        else {
            print('.')
        }
    }
}
