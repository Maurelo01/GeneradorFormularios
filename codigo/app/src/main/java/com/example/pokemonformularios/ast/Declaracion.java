package com.example.pokemonformularios.ast;

public class Declaracion implements Instruccion
{
    private String tipoVar;
    private String id;
    private Expresion valorExpresion;

    public Declaracion(String tipoVar, String id, Expresion valorExpresion)
    {
        this.tipoVar = tipoVar;
        this.id = id;
        this.valorExpresion = valorExpresion;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Object valorFinal = null;
        if (valorExpresion != null)
        {
            valorFinal = valorExpresion.evaluar(ent);
            if (valorFinal == null) return null;
            if (tipoVar.equals("number") && !(valorFinal instanceof Double))
            {
                System.err.println("Error Semántico: Se esperaba un número para la variable '" + id + "'");
                return null;
            }
            if (tipoVar.equals("string") && !(valorFinal instanceof String))
            {
                System.err.println("Error Semántico: Se esperaba un texto para la variable '" + id + "'");
                return null;
            }
        }
        else
        {
            if (tipoVar.equals("number")) valorFinal = 0.0;
            if (tipoVar.equals("string")) valorFinal = "";
        }
        boolean guardado = ent.agregarVariable(id, tipoVar, valorFinal);
        if (!guardado)
        {
            System.err.println("Error Semántico: La variable '" + id + "' ya existe y no se puede redefinir.");
        }
        return null;
    }
}