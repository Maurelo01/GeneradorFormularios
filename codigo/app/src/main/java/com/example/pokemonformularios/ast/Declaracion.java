package com.example.pokemonformularios.ast;
import com.example.pokemonformularios.reportes.ErrorCompi;
public class Declaracion implements Instruccion
{
    private String tipoVar;
    private String id;
    private Expresion valorExpresion;
    private int linea;
    private int columna;


    public Declaracion(String tipoVar, String id, Expresion valorExpresion, int linea, int columna)
    {
        this.tipoVar = tipoVar;
        this.id = id;
        this.valorExpresion = valorExpresion;
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        if (ent.existeVariable(id))
        {
            String mensaje = "La variable '" + id + "' ya existe y no se puede redefinir.";
            System.err.println("Error Semántico: " + mensaje);
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
            return null;
        }
        Object valorFinal = null;
        if (valorExpresion != null)
        {
            valorFinal = valorExpresion.evaluar(ent);
            if (valorFinal == null) return null;
            if (tipoVar.equals("number") && !(valorFinal instanceof Double))
            {
                String mensaje = "Se esperaba un número para la variable '" + id + "'";
                System.err.println("Error Semántico: " + mensaje);
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
                valorFinal = 0.0;
            }
            else if (tipoVar.equals("string") && !(valorFinal instanceof String))
            {
                String mensaje = "Se esperaba un texto para la variable '" + id + "'";
                System.err.println("Error Semántico: " + mensaje);
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
                valorFinal = "";
            }
        }
        else
        {
            if (tipoVar.equals("number")) valorFinal = 0.0;
            if (tipoVar.equals("string")) valorFinal = "";
        }
        ent.agregarVariable(id, tipoVar, valorFinal);
        return null;
    }
}