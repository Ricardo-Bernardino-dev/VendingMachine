package main.APP

import main.UI.HAL

object M{
    fun isMaintenance(): Boolean {
        return HAL.isBit(0x40)
    }
}

//so verificar o isbit do M