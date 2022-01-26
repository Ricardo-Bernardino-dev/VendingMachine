package main

object CoinDeposit {

    //Moedas de 0,5 - 5
    //Preço real = 10/5 = 2 moedas de 0,5

    var currentcoins: Int = 0

    fun addCoin() {
        val result = currentcoins + 5
        currentcoins = result
    }
}