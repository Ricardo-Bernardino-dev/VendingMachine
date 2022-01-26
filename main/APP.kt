package main

import isel.leic.utils.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun main() {
    var app: APP = APP()
}

class APP {

    //Manuntencao mostra todos

    //true -> manual | false -> arrow
    var mode = 0
    var firstProd: Products.Product? = null
    var currentProd: Products.Product? = null

    private fun getFirstProduct(): Products.Product? {
        var notFinished = true
        var i = 0
        var firstItem: Products.Product? = null
        while (notFinished) {
            if (Products.products[i] != null) {
                firstItem = Products.products[i]!!
                notFinished = false
            }
            i++
        }
        return firstItem
    }

    private fun getLastProduct(): Products.Product? {
        var notFinished = true
        var i = Products.products.size-1
        var lastItem: Products.Product? = null
        while (notFinished) {
            if (Products.products[i] != null) {
                lastItem = Products.products[i]!!
                notFinished = false
            }
            --i
        }
        return lastItem
    }

    var numOfDigits = 1
    val timeout: Long = 1000
    var currentKey: Char? = null

    /**
     * Ecrã principal da aplicação, que atualiza constatemente o tempo
     * (ou a data caso passe da meia-noite)
     */
    fun home() {
        LCD.clear()
        LCD.cursor(0, 0)
        LCD.writeS("Vending Machine")
        LCD.cursor(1, 0)
        val currentDateTime = LocalDateTime.now()
        var date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        LCD.writeS(date + " ")
        LCD.writeS(time)
        LCD.cursor(2, 16)
        var datechanges: Boolean
        var changed: String?
        var key: Char
        do {
            changed = timechanges(time)
            if (changed != null) {
                LCD.cursor(1, 9)
                LCD.writeS(changed)
                time = changed
                datechanges = (time == "00:01")
                if (datechanges) {
                    date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    LCD.cursor(1, 0)
                    LCD.writeS(date)
                }
                LCD.cursor(2, 16)
            }
            key = KBD.getKey()
            if (HAL.isBit(0x40)) MaintenanceMode()
        } while (key != '#')
        if (mode == 0) saleManual()
        else saleArrow()
    }



    private fun timechanges(time: String?): String? {
        val currentDateTime = LocalDateTime.now()
        val currenttime = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        if (currenttime == time) return null
        return currenttime
    }

    /**
     * Função de inicialização dos componentes base, e da lista de produtos através da leitura do ficheiro PRODUCTS.txt
     */
    fun init() {
        HAL.init()
        SerialEmitter.init()
        LCD.init()
        Products.buildProducts()
        firstProd = getFirstProduct()
        currentProd = firstProd
    }


    /**
     * Construtor primário da Aplicação,
     * inicia os componentes (init) e redireciona para o ecrã principal (home)
     */
    init {
        init()
        val home = home()
        val sale = saleManual()
        // if(Sale) SaleMode()
        //else MaintenanceMode()
    }
    private fun saleArrow() {
        TUI.clear()
        writeProductArrow(currentProd!!)
        var current = currentProd!!.id
        var counter = 0
        while (counter <= 5) {
            val idx = TUI.arrowInteraction(Products.products,current)
            if(idx=='*'.code) saleManual()
                when(idx){
                    -1 -> {
                        val last = getLastProduct()!!
                        writeProductArrow(last)
                        current = last.id
                        counter = 0
                        currentProd = last
                    }

                    Products.products.size ->{
                        val first = getFirstProduct()!!
                        writeProductArrow(first)
                        current = first.id
                        counter = 0
                        currentProd = first
                    }

                    null -> if(currentProd!!.quantity>0) sellProduct(currentProd!!)

                    else -> {
                        current = idx
                        counter = 0
                        val prod = Products.products[idx]
                        writeProductArrow(prod!!)
                        currentProd = prod
                    }
                }
        counter++
        }
        home()
    }



    /**
     * Mode 0 - Manual
     * Mode 1 - Arrow
     */
    private fun saleManual() {
        TUI.clear()
        if (firstProd != null) {
            if(currentProd==firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            LCD.writeS("No Products")
            home()
        }
        var prod = 0
        var counter = 0
        var item = currentProd
        while (counter <= 5) {
            val key = TUI.pressedKeyManual(1000)
            if (key == "*"|| key == "**") {
                saleArrow()
            }
            if (key != null) {
                if (key != "#"){
                    counter = 0
                    prod = key.toInt()
                    if (key.length == 1) {
                        item = Products.products[key[0].digitToInt()]
                        if (item != null){
                            currentProd = item
                            writeProductManual(item)
                        }
                        else productNotAvailable(key.toString())
                    } else {
                        println(key.toInt())
                        if (key.toInt() in 10..15) {
                            item = Products.products[key.toInt()]
                            if (item != null){
                                currentProd = item
                                writeProductManual(item)
                            }
                            else productNotAvailable(key.toString())
                        } else productNotAvailable(key.toString())
                    }
                }else if(currentProd!!.quantity>0) sellProduct(currentProd!!)
               // if (key.toString() == "#" && Products.products[prod]!=null && Products.products[prod]!!.quantity > 0) sellProduct(prod)
            }
            counter++
        }
        home()
    }


    private fun sellProduct(product: Products.Product) {
        println("Sell Product $product")
        val price = product.price
        var priceleft = price
        LCD.cursor(1, 0)
        LCD.writeS("      ${getCoinValue(price)}$      ")
        var key: String? = null
        CoinAcceptor.init()
        while (true) {
            key = TUI.pressedKeyManual(500)
            if(key=="#"){
                abort()
                CoinAcceptor.ejectCoins()
                home()
            }
            if(CoinAcceptor.counter==price){
                FileAccess.addCoins(CoinAcceptor.counter)
                CoinAcceptor.collectCoins()
                LCD.cursor(1,0)
                LCD.writeS("Collect Product")
                SerialEmitter.send(SerialEmitter.Destination.DISPENSER,product.id)
                LCD.cursor(1,0)
                LCD.writeS("  See You Soon!    ")
                Time.sleep(1000)
                home()
            }
            if(CoinAcceptor.hasCoin()){
                CoinAcceptor.acceptCoin()
                priceleft-=5
                LCD.cursor(1, 0)
                LCD.writeS("      ${getCoinValue(priceleft)}$      ")
            }
        }
    }

    private fun abort() {
        LCD.clear()
        LCD.writeS("Vending Aborted")
        if(CoinAcceptor.counter>0){
            LCD.cursor(1,0)
            LCD.writeS("  Return ${getCoinValue(CoinAcceptor.counter)}$")
        }
    }

    private fun productNotAvailable(key: String) {
        LCD.clear()
        LCD.cursor(0, 3)
        LCD.writeS("Product $key")
        LCD.cursor(1, 1)
        LCD.writeS("Not Available")

    }

    private fun writeProductArrow(prod: Products.Product) {
        LCD.clear()
        LCD.cursor(0, 5)
        LCD.writeS(prod.name)
        LCD.cursor(1, 0)
        val prodId = prod.id
        if (prodId in 10..16) LCD.writeS("$prodId-")
        else LCD.writeS("0${prodId}-")
        LCD.cursor(1, 5)
        val prodQuant = prod.quantity
        if (prodQuant in 10..16) LCD.writeS("#${prodQuant}")
        else LCD.writeS("#0$prodQuant")
        LCD.cursor(1, 12)
        LCD.writeS("${getCoinValue(prod.price)}$")
    }

    private fun writeProductManual(prod: Products.Product) {
        LCD.clear()
        LCD.cursor(0, 5)
        LCD.writeS(prod.name)
        LCD.cursor(1, 0)
        val prodId = prod.id
        if (prodId in 10..16) LCD.writeS(prodId.toString())
        else LCD.writeS("0$prodId")
        LCD.cursor(1, 6)
        val prodQuant = prod.quantity
        if (prodQuant in 10..16) LCD.writeS("#${prodQuant}")
        else LCD.writeS("#0$prodQuant")
        LCD.cursor(1, 12)
        LCD.writeS("${getCoinValue(prod.price)}$")
    }

    private fun getCoinValue(value: Int): Double {
        return value.toDouble() / 10
    }

    private fun MaintenanceMode() {
        LCD.clear()
        LCD.cursor(0, 0)
        LCD.writeS("Maintenance Mode")
        var key: Char
        do {
            LCD.cursor(1, 0)
            LCD.writeS("1-Dispense test")
            LCD.cursor(2, 16)
            key = KBD.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !(HAL.isBit(0x40))) break
            LCD.cursor(1, 0)
            LCD.writeS("2-Update Prod. ")
            LCD.cursor(2, 16)
            key = KBD.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !(HAL.isBit(0x40))) break
            LCD.cursor(1, 0)
            LCD.writeS("3-Remove Prod. ")
            LCD.cursor(2, 16)
            key = KBD.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !(HAL.isBit(0x40))) break
            LCD.cursor(1, 0)
            LCD.writeS("4-Shutdown     ")
            LCD.cursor(2, 16)
            key = KBD.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !(HAL.isBit(0x40))) break
        } while (true)
        if (!(HAL.isBit(0x40))) home()
        when (key) {
            '1' -> print("x == 1")  //Dispense Test
            '2' -> print("x == 2")  //Update Prod
            '3' -> print("x == 3") //Remove Prod
            '4' -> print("x == 4") //Shutdown

        }
    }


    /*
    fun getProducts():Array<String>{
       // TODO
    }
    
    fun mode(manual:Boolean):Int{ 
        if(manual) currentKey = TUI.manualInteraction(numOfDigits, timeout)
        else  cycle(TUI.arrowInteraction())
    }

     */

    private fun cycle(arrowInteraction: Int) {

    }


    //ponteiro idx de TUI.arrowInteraction é dos produtos
    // Iteração de setas: ir chamando a função TUI.arrowInteraction
    //Se a tecla for '#' no TUI.arrowInteraction entrar numa função que muda um booleano para false, que acaba o ciclo while (cicle de tecla)
    // se end() acaba por alteração, chama a função com while de TUI.getKey (modo manual)
    // se end() acaba por confirmação, chama a função de print da tecla e info
    //A App dá cycle no array de produtos, consuante o retorno da função TUI.arrowInteraction
}


/*Modo de Venda
 1- Averiguar o modo de análise de tecla (arrow ou manual) usando a classe TUI
 2- Atribuir o produto desejado à tecla pressa (array de produtos)
 3- Display do info do produto no LCD
 */

/* Modo de Manuntenção
 TODO
 */