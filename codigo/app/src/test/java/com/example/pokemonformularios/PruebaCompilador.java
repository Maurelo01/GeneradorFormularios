package com.example.pokemonformularios;

import org.junit.Test;
import java.io.StringReader;

import com.example.pokemonformularios.analizador.LexerFormulario;
import com.example.pokemonformularios.analizador.ParserFormulario;
import com.example.pokemonformularios.ast.Entorno;
import com.example.pokemonformularios.ast.Instruccion;

public class PruebaCompilador {

    // La etiqueta @Test le dice a Android Studio que esto se puede ejecutar como una mini-aplicación de consola
    @Test
    public void probarMiCompilador() {

        String entrada =
                "number contador = 1\n" +
                        "WHILE (contador <= 2) {\n" +
                        "   SECTION [\n" +
                        "       elements: {\n" +
                        "           TEXT [ content: \"Iteracion del While numero: \" + contador ]\n" +
                        "       }\n" +
                        "   ]\n" +
                        "   contador = contador + 1\n" +
                        "}\n" +
                        "\n" +
                        "FOR (v in 1..3) {\n" +
                        "   OPEN_QUESTION [\n" +
                        "       label: \"Pregunta autogenerada \" + v\n" +
                        "   ]\n" +
                        "}";

        try {
            System.out.println("Iniciando Compilación...");

            LexerFormulario lexer = new LexerFormulario(new StringReader(entrada));
            ParserFormulario parser = new ParserFormulario(lexer);

            java_cup.runtime.Symbol sym = parser.parse();
            Instruccion raizAST = (Instruccion) sym.value;

            if (parser.listaErrores.isEmpty()) {
                System.out.println("✅ Análisis Sintáctico y Léxico completado sin errores.\n");

                Entorno entornoGlobal = new Entorno(null);

                System.out.println("--- INICIANDO EJECUCIÓN SEMÁNTICA ---");
                if (raizAST != null) {
                    raizAST.ejecutar(entornoGlobal);
                }
                System.out.println("--- FIN DE EJECUCIÓN ---");

            } else {
                System.out.println("❌ Se encontraron errores. El código no se ejecutará.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}