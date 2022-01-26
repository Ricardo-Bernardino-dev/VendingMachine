package main

import isel.leic.utils.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun main() {
    var app: APP = APP()
}

class APP {
    /**
     * Mode 0 - Manual
     * Mode 1 - Arrow
     */
    var mode = 0

    /**
     * Desired timeout for all application processes
     */
    val timeout: Long = 1000

    /**
     * First {Product} of the list of {Products}, obtained after building the list
     */
    var firstProd: Products.Product? = null

    /**
     * Last {Product} visualized by the user
     */
    var currentProd: Products.Product? = null


    /**
     * Get the first Product found in the list of products
     */
    private fun getFirstProduct(): Products.Product? {
        var notFinished = true
        var i = 0
        var firstItem: Products.Product? = null
        while (notFinished) {
            if(i==Products.products.size) emptyMachineStock()
            if (Products.products[i] != null) {
                firstItem = Products.products[i]!!
                notFinished = false
            }
            i++
        }
        return firstItem
    }

    private fun emptyMachineStock() {
        LCD.clear()
        LCD.cursor(0,0)
        LCD.writeS(" Empty machine  ")
        LCD.cursor(1,0)
        LCD.writeS("     Stock     ")
    }


    /**
     * Get the first Product found in the list of products
     */
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

    /**
     * Home screen of the application, updates time constantly and
     * date if the time is passed due midnight
     */
    fun home() {
        LCD.clear()
        LCD.cursor(0, 0)
        LCD.writeS("Vending Machine")
        LCD.cursor(1, 0)
        val currentDateTime = LocalDateTime.now()
        var date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        LCD.writeS(date + "   ")
        LCD.writeS(time)
        LCD.cursor(2, 16)
        var datechanges: Boolean
        var changed: String?
        var key: Char
        do {
            changed = timechanges(time)
            if (changed != null) {
                LCD.cursor(1, 11)
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
        } while (key != '#' && key != '*')
        if(key == '#') {
            if (mode == 0) sellManual()
            else SellArrow()
        }
        if(key=='*') cycleProductsDisplay()
    }

    /**
     * Feature that cycles through all available products (quantity above zero)
     * Press * to return to Home Screen
     */
    private fun cycleProductsDisplay() {
        LCD.clear()
        LCD.cursor(0,0)
        LCD.writeS(" All Available  ")
        LCD.cursor(1,0)
        LCD.writeS("   Products:    ")
        LCD.cursor(3,16)
        Time.sleep(2500)
        var idx = 0
        var key :String? = null
        while(true){
            if(idx==Products.products.size) home()
            LCD.clear()
            if(Products.products[idx]!=null) {
                if(Products.products[idx]!!.quantity > 0){
                    writeProductManual(Products.products[idx]!!)
                    key = TUI.pressedKeyManual(3000)
                    if(key=="*") home()
                }
            }
            idx++
        }
    }

    /**
     * Returns the current time (if changed), null if not.
     */
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
        val sale = sellManual()
        // if(Sale) SaleMode()
        //else MaintenanceMode()
    }
    private fun SellArrow() {
        TUI.clear()
        writeProductArrow(currentProd!!)
        var current = currentProd!!.id
        var counter = 0
        while (counter <= 5) {
            val idx = TUI.arrowInteraction(Products.products,current)
            if(idx=='*'.code){
                mode = 0
                sellManual()
            }
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
                            else noStock()

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
     * Second Version of the Sell Mode, implements a slower but more accurate key reader display
     */
    private fun sellManualV2() {
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
        while (counter <= 8) {
            val key = TUI.pressedKeyManual(300)
            if (key == "*"|| key == "**") {
                mode = 1
                SellArrow()
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
                };
                if(key== "#"){
                    if(currentProd!!.quantity>0) sellProduct(currentProd!!)
                    else noStock()
                }
               // if (key.toString() == "#" && Products.products[prod]!=null && Products.products[prod]!!.quantity > 0) sellProduct(prod)
            }
            counter++
        }
        home()
    }

    /**
     * The Sell Mode prints the last visualized item (first Product if application restarted),
     * and constantly waits for a key to be pressed.
     * If the key pressed is a first digit, it display's the Product immediatly and waits for the second key
     * if the second key isn't the digit '1', it prints the Product immediatly similarly to what was previously mentioned,
     * if the second key is the digit '1' it displays the full 2 number digit Product, if it exists in the product list
     */
    private fun sellManual() {
        TUI.clear()
        if (firstProd != null) {
            if (currentProd == firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            LCD.writeS("No Products")
            home()
        }
        var counter = 0
        var prod: Products.Product?
        var fullNumber:Int
        var key: Char?
        while (counter <= 5) {
            key = TUI.pressedQuickKeyManual(1000)
            if(key!=null){
                if (key == '*') {
                    mode = 1
                    SellArrow()
                }
                if (key != '#') {
                    counter = 0
                    if(TUI.isSecondDigit&&TUI.firstPressedKey=='1'){
                        fullNumber = ("${TUI.firstPressedKey!!}$key").toInt()
                        if(fullNumber<Products.products.size) {
                            prod = Products.products[fullNumber]
                            if (prod != null){
                                currentProd = prod
                                writeProductManual(prod)
                            }
                            else{
                                prod = Products.products[key!!.digitToInt()]
                                if(prod!=null){
                                    writeProductManual(prod)
                                    currentProd=prod
                                }else productNotAvailable2(key)
                            }
                        }
                        TUI.isSecondDigit=false
                    }else{
                        prod = Products.products[key.digitToInt()]
                        if (prod != null) {
                            currentProd = prod
                            writeProductManual(prod)
                            TUI.firstPressedKey = key
                            TUI.isSecondDigit = true
                        }else productNotAvailable2(key)
                    }
                }
                if(key== '#'){
                    if(currentProd!!.quantity>0) sellProduct(currentProd!!) else noStock()
                }
            }
            counter++
        };home()
    }


    private fun noStock() {
        TUI.clear()
        LCD.cursor(0,0)
        LCD.writeS("  Out Of Stock  ")
        LCD.cursor(1,0)
        LCD.writeS("       :(       ")
        Time.sleep(2300)
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
                CoinAcceptor.collectCoins()
                LCD.cursor(1,0)
                LCD.writeS("Collect Product")
                LCD.cursor(2,16)
                SerialEmitter.send(SerialEmitter.Destination.DISPENSER,product.id)
                LCD.cursor(0,0)
                LCD.writeS("  Thank You!")
                LCD.cursor(1,0)
                LCD.writeS("  See You Soon!    ")
                LCD.cursor(2,16)
                Time.sleep(2000)
                FileAccess.addCoins(CoinAcceptor.counter)
                FileAccess.removeProduct(product.id)
                Products.products[product.id]!!.quantity-=1
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
        LCD.cursor(2,16)
        if(CoinAcceptor.counter>0){
            LCD.cursor(1,0)
            LCD.writeS("  Return ${getCoinValue(CoinAcceptor.counter)}$")
            LCD.cursor(2,16)
        }
    }

    private fun productNotAvailable(key: String) {
        LCD.clear()
        LCD.cursor(0, 3)
        LCD.writeS("Product $key")
        LCD.cursor(1, 1)
        LCD.writeS("Not Available")
        LCD.cursor(2, 16)
    }

    private fun productNotAvailable2(key: Char) {
        LCD.clear()
        LCD.cursor(0, 3)
        LCD.writeS("Product $key")
        LCD.cursor(1, 1)
        LCD.writeS("Not Available")
        LCD.cursor(2, 16)
    }

    private fun writeProductArrow(prod: Products.Product) {
        LCD.clear()
        LCD.cursor(0, 5)
        LCD.writeS(prod.name)
        LCD.cursor(1, 0)
        val prodId = prod.id
        if (prodId in 10..16) LCD.writeS("$prodId-")
        else LCD.writeS("0${prodId}-")
        LCD.cursor(1, 6)
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