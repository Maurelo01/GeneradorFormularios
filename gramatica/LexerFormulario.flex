import java_cup.runtime.*;
import java.util.ArrayList;

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
/***** MANEJO DE ERRORES *****/
{CadenaSinCerrar}
{
    reportarError(yytext(), "Cadena sin cerrar - falta comilla de cierre");
}
.
{
    reportarError(yytext(), "Caracter no válido en el lenguaje");
}