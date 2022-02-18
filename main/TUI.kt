package main


fun main() {

}

object TUI {

    //array Produtos (0,1,2,4...)

    fun init() {
        HAL.init()
        LCD.init()
        KBD.init()
    }

    var isSecondDigit = false
    var firstPressedKey:Char? = null

    /**
    Função que determina o modo de escolha através de setas (2 |\ cima, 8 \| baixo), selecionado pelo * e confirma com #.
     */
    fun arrowInteraction(array: MutableList<Products.Product?>, current: Int): Int? {
        var key: Char?
        do {
            key = KBD.waitKey(1000)
        } while (key != '8' && key != '2' && key != '*' && key != '#')
        when (key) {
            '2' -> {               //previous() //1
                var i = current
                while (true) {
                    --i
                   if (i==-1 || array[i] != null) break
                }
                return i
            }
            '8' -> {                // next() //0
                var i = current
                while (true) {
                    ++i
                    if (i==array.size || array[i] != null) break
                }
                return i
            }
            '*' -> return key.code//end() //acaba por alteração, retorna NULL
            else -> {
                return null //'#' -> return 3//end() //acaba por confirmação, acaba e devolve a tecla
            }
        }
    }

    /**
    Função que determina o modo manual de escolha de produto, introduzindo o número respetivo.
     */
    fun manualInteraction(numerodedgitos: Int, timeout: Long): Array<Char> { //apenas escreve o numero
        var key: Char
        var keyArray: Array<Char> = emptyArray()
        for (i in 0..numerodedgitos) {
            key = KBD.waitKey(timeout)
            if (key != KBD.NONE) {
                keyArray.set(i, key)
            }
        };return keyArray
        //writeKey(numerodedgitos,keyArray)
    }

    fun pressedKeyManual(timeout: Long): String? {
        var value = ""
        var pressed = false
        var key: Char = KBD.waitKey(timeout)
        if (key != KBD.NONE) {
            if(key=='*'||key=='#') return key.toString()
            pressed = true
            value += key
            key = KBD.waitKey(500)
        }
        if(key=='*'||key=='#') return value
        if (key == KBD.NONE && pressed) return value
        else {
            if (pressed) {
                value += key
                return value
            } else return null
        }
    }

    fun pressedQuickKeyManual(timeout:Long): Char? {
        val key: Char = KBD.waitKey(timeout)
        if (key != KBD.NONE) {
            return key
        }
        return null
    }

    fun writeKey(numerodedgitos: Int, keyArray: Array<Char>) {
        var resultKey = ""
        for (i in 0..numerodedgitos) {
            resultKey += keyArray[i]
        }
        LCD.writeS(resultKey)
    }

    fun clear() {
        LCD.clear()
    }

    fun cursor(line:Int,column:Int){
        LCD.cursor(line,column)
    }

    fun writeS(text: String) {
        LCD.writeS(text)
    }

    fun writeC(c:Char){
        LCD.writeC(c)
    }


    /**
     * Metodo generico que escreve a lista passada como argumento
     */
}

