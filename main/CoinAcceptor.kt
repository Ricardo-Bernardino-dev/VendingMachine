package main

import isel.leic.utils.Time

object CoinAcceptor { // Implementa a interface com o moedeiro.
    //Usar só setBits e clearBits

    var counter = 0

    // Inicia a classe
    fun init(){
    }


    // Retorna true se foi introduzida uma nova moeda.
    fun hasCoin(): Boolean {
        return HAL.isBit(0x20)
    }


    // Informa o moedeiro que a moeda foi contabilizada.
    fun acceptCoin() {
        HAL.setBits(0x10)
        HAL.clrBits(0x10)
        counter+=5
    }


    // Devolve as moedas que estão no moedeiro.
    fun ejectCoins() {
        HAL.setBits(0x40)
        Time.sleep(1000)
        HAL.clrBits(0x40)
        counter=0
    }


    // Recolhe as moedas que estão no moedeiro.
    fun collectCoins() {
        HAL.setBits(0x20)
        Time.sleep(1000)
        HAL.clrBits(0x20)
        counter=0
    }
}