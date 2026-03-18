import java_cup.runtime.*;
import java.util.ArrayList;
import com.example.pokemonformularios.reportes.ErrorCompi;

%%
%class LexerFormulario
%cup
%line
%column
%public
%unicode

%{
    public static class ErrorLexico
    {
        public String lexema;
        public int linea;
        public int columna;
        public String descripcion;
        public ErrorLexico(String lexema, int linea, int columna, String descripcion)
        {
            this.lexema = lexema;
            this.linea = linea;
            this.columna = columna;
            this.descripcion = descripcion;
        }
    }
    public ArrayList<ErrorLexico> listaErrores = new ArrayList<>();
    private Symbol symbol(int type, Object value)
    {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }

    private void reportarError(String lexema, String descripcion)
    {
        listaErrores.add(new ErrorLexico(lexema, yyline + 1, yycolumn + 1, descripcion));
        System.err.println("Error Léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1) + ": " + descripcion + " '" + lexema + "'");
    }

    public java.util.ArrayList<ErrorCompi> erroresLexicos = new java.util.ArrayList<>();
%}

/***** EXPRESIONES REGULARES *****/
Letra = [a-zA-Z_]
Digito = [0-9]
Entero = {Digito}+
Decimal = {Entero}"."{Entero}
Identificador = {Letra}({Letra}|{Digito})*
Cadena = \"[^\"]*\"
CadenaSinCerrar = \"[^\"\n]*
Comentariolinea = \$[^\n]*\n?
ComentarioBloque = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EspaciosEnBlanco = [ \t\r\n\f]+

// Patrones de Colores Especiales
ColorHex = #[0-9a-fA-F]{6}
ColorRGB = \({EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*,{EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*,{EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*\)
ColorHSL = <{EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*,{EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*,{EspaciosEnBlanco}*{Entero}{EspaciosEnBlanco}*>

%%

/***** IGNORAR ESPACIOS Y COMENTARIOS *****/
{EspaciosEnBlanco}
{
    /* Ignorar */
}

{Comentariolinea}
{
    /* Ignorar */
}

{ComentarioBloque}
{
    /* Ignorar */
}

/***** TIPOS DE DATOS *****/
"number" 
{
    return symbol(sym.TYPE_NUMBER, yytext());
}

"string"
{
    return symbol(sym.TYPE_STRING, yytext());
}

"special"
{
    return symbol(sym.TYPE_SPECIAL, yytext());
}

/***** COMPONENTES ESTRUCTURALES *****/
"SECTION"
{
    return symbol(sym.SECTION, yytext());
}

"TABLE"
{
    return symbol(sym.TABLE, yytext());
}

"TEXT"
{
    return symbol(sym.TEXT, yytext());
}

/***** TIPOS DE PREGUNTAS *****/
"OPEN_QUESTION"
{
    return symbol(sym.OPEN_QUESTION, yytext());
}

"DROP_QUESTION"
{
    return symbol(sym.DROP_QUESTION, yytext());
}

"SELECT_QUESTION"
{
    return symbol(sym.SELECT_QUESTION, yytext());
}

"MULTIPLE_QUESTION"
{
    return symbol(sym.MULTIPLE_QUESTION, yytext());
}

/***** ESTRUCTURAS DE CONTROL *****/
"IF"
{
    return symbol(sym.IF, yytext());
}

"ELSE"
{
    return symbol(sym.ELSE, yytext());
}

"WHILE"
{
    return symbol(sym.WHILE, yytext());
}

"DO"
{
    return symbol(sym.DO, yytext());
}

"FOR"
{
    return symbol(sym.FOR, yytext());
}

/***** CICLOS FOR *****/
";"
{
    return symbol(sym.PUNTO_Y_COMA, yytext());
}
"in"
{
    return symbol(sym.IN, yytext());
}
".."
{
    return symbol(sym.PUNTO_PUNTO, yytext());
}

/***** ATRIBUTOS RESERVADOS *****/
"width"
{
    return symbol(sym.ATTR_WIDTH, yytext());
}

"height"
{
    return symbol(sym.ATTR_HEIGHT, yytext());
}

"pointX"
{
    return symbol(sym.ATTR_POINTX, yytext());
}

"pointY"
{
    return symbol(sym.ATTR_POINTY, yytext());
}

"orientation"
{
    return symbol(sym.ATTR_ORIENTATION, yytext());
}

"elements"
{
    return symbol(sym.ATTR_ELEMENTS, yytext());
}

"styles"
{
    return symbol(sym.ATTR_STYLES, yytext());
}

"content"
{
    return symbol(sym.ATTR_CONTENT, yytext());
}

"label"
{
    return symbol(sym.ATTR_LABEL, yytext());
}

"options"
{
    return symbol(sym.ATTR_OPTIONS, yytext());
}

"correct"
{
    return symbol(sym.ATTR_CORRECT, yytext());
}

/***** ENUMS Y CONSTANTES DE ESTILO *****/
"VERTICAL"
{
    return symbol(sym.ENUM_VERTICAL, yytext());
}

"HORIZONTAL"
{
    return symbol(sym.ENUM_HORIZONTAL, yytext());
}

"MONO"
{
    return symbol(sym.ENUM_MONO, yytext());
}

"SANS_SERIF"
{
    return symbol(sym.ENUM_SANS_SERIF, yytext());
}

"CURSIVE"
{
    return symbol(sym.ENUM_CURSIVE, yytext());
}

"LINE"
{
    return symbol(sym.ENUM_LINE, yytext());
}

"DOTTED"
{
    return symbol(sym.ENUM_DOTTED, yytext());
}

"DOUBLE"
{
    return symbol(sym.ENUM_DOUBLE, yytext());
}

/***** COLORES PREDEFINIDOS *****/
"RED"
{
    return symbol(sym.COLOR_RED, yytext());
}

"BLUE"
{
    return symbol(sym.COLOR_BLUE, yytext());
}

"GREEN"
{
    return symbol(sym.COLOR_GREEN, yytext());
}

"PURPLE"
{
    return symbol(sym.COLOR_PURPLE, yytext());
}

"SKY"
{
    return symbol(sym.COLOR_SKY, yytext());
}

"YELLOW"
{
    return symbol(sym.COLOR_YELLOW, yytext());
}

"BLACK"
{
    return symbol(sym.COLOR_BLACK, yytext());
}

"WHITE"
{
    return symbol(sym.COLOR_WHITE, yytext());
}

/***** FUNCIONES ESPECIALES *****/
"draw"
{
    return symbol(sym.FUNC_DRAW, yytext());
}

"who_is_that_pokemon"
{
    return symbol(sym.FUNC_POKEMON, yytext());
}

/***** OPERADORES ARITMÉTICOS *****/
"+"
{
    return symbol(sym.MAS, yytext());
}

"-"
{
    return symbol(sym.MENOS, yytext());
}

"*"
{
    return symbol(sym.POR, yytext());
}

"/"
{
    return symbol(sym.DIV, yytext());
}

"^"
{
    return symbol(sym.POTENCIA, yytext());
}

"%"
{
    return symbol(sym.MODULO, yytext());
}


/***** OPERADORES RELACIONALES Y LÓGICOS *****/
"=="
{
    return symbol(sym.IGUALDAD, yytext());
}

"!!"
{
    return symbol(sym.DIFERENTE, yytext());
}

">"
{
    return symbol(sym.MAYOR, yytext());
}

"<"
{
    return symbol(sym.MENOR, yytext());
}

">="
{
    return symbol(sym.MAYOR_IGUAL, yytext());
}

"<="
{
    return symbol(sym.MENOR_IGUAL, yytext());
}

"&&"
{
    return symbol(sym.AND, yytext());
}

"||"
{
    return symbol(sym.OR, yytext());
}

"!"
{
    return symbol(sym.NOT, yytext());
}

"="
{
    return symbol(sym.IGUAL_ASIG, yytext());
}

/***** SIGNOS DE AGRUPACIÓN Y PUNTUACIÓN *****/
"("
{
    return symbol(sym.PAR_IZQ, yytext());
}

")"
{
    return symbol(sym.PAR_DER, yytext());
}

"["
{
    return symbol(sym.CORCHETE_IZQ, yytext());
}

"]"
{
    return symbol(sym.CORCHETE_DER, yytext());
}

"{"
{
    return symbol(sym.LLAVE_IZQ, yytext());
}

"}"
{
    return symbol(sym.LLAVE_DER, yytext());
}

","
{
    return symbol(sym.COMA, yytext());
}

":"
{
    return symbol(sym.DOS_PUNTOS, yytext());
}

"?"
{
    return symbol(sym.COMODIN, yytext());
}

/***** LITERALES Y PATRONES *****/
{ColorHex}
{
    return symbol(sym.VAL_COLOR_HEX, yytext());
}

{ColorRGB}
{
    return symbol(sym.VAL_COLOR_RGB, yytext());
}

{ColorHSL}
{
    return symbol(sym.VAL_COLOR_HSL, yytext());
}

{Cadena}
{
    return symbol(sym.CADENA, yytext());
}

{Decimal}
{
    return symbol(sym.DECIMAL, Double.parseDouble(yytext()));
}

{Entero}
{
    return symbol(sym.ENTERO, Double.parseDouble(yytext()));
}

{Identificador}
{
    return symbol(sym.ID, yytext());
}

/***** MANEJO DE ERRORES *****/
{CadenaSinCerrar}
{
    String lexema = yytext();
    int linea = yyline + 1;
    int columna = yycolumn + 1;
    erroresLexicos.add(new ErrorCompi("Léxico", "Cadena sin cerrar: " + lexema, linea, columna));
    System.err.println("Error Léxico en (" + linea + ", " + columna + "): Cadena sin cerrar");
}
.
{
    String lexema = yytext();
    int linea = yyline + 1;
    int columna = yycolumn + 1;
    erroresLexicos.add(new ErrorCompi("Léxico", "Caracter no válido: " + lexema, linea, columna));
    System.err.println("Error Léxico en (" + linea + ", " + columna + "): Caracter no válido: " + lexema);
}