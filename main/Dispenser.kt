package main

object Dispenser { // Controla o estado do mecanismo de dispensa.
    // private const val Fn = 0x40 //0100 0000
    // Inicia a classe, estabelecendo os valores iniciais.
    fun init() {}

    // Envia comando para dispensar uma unidade de um produto
    fun dispense(productid: Int) {
        if (productid in 0..15) {
            SerialEmitter.send(SerialEmitter.Destination.DISPENSER, productid)
        }
    }
}