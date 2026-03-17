package com.example.pokemonformularios.ast;

public class Asignacion implements Instruccion
{
    private String id;
    private Expresion valorExpresion;

    public Asignacion(String id, Expresion valorExpresion)
    {
        this.id = id;
        this.valorExpresion = valorExpresion;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Object nuevoValor = valorExpresion.evaluar(ent);
        if (nuevoValor == null) return null;
        Simbolo sim = ent.obtenerVariable(id);
        if (sim == null)
        {
            System.err.println("Error Semántico: La variable '" + id + "' no existe.");
            return null;
        }
        if (sim.getTipo().equals("number") && !(nuevoValor instanceof Double))
        {
            System.err.println("Error Semántico: No puedes asignar un texto a la variable numérica '" + id + "'");
            return null;
        }
        if (sim.getTipo().equals("string") && !(nuevoValor instanceof String))
        {
            System.err.println("Error Semántico: No puedes asignar un número a la variable de texto '" + id + "'");
            return null;
        }
        ent.reasignarVariable(id, nuevoValor);
        return null;
    }
}