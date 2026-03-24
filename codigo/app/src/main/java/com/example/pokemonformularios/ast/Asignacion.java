package com.example.pokemonformularios.ast;

import com.example.pokemonformularios.reportes.ErrorCompi;

public class Asignacion implements Instruccion
{
    private String id;
    private Expresion valorExpresion;
    private int linea;
    private int columna;

    public Asignacion(String id, Expresion valorExpresion, int linea, int columna)
    {
        this.id = id;
        this.valorExpresion = valorExpresion;
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Object nuevoValor = valorExpresion.evaluar(ent);
        if (nuevoValor == null) return null;
        Simbolo sim = ent.obtenerVariable(id);
        if (sim == null)
        {
            String mensaje = "La variable '" + id + "' no existe.";
            System.err.println("Error Semántico: " + mensaje);
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
            return null;
        }
        if (sim.getTipo().equals("number") && !(nuevoValor instanceof Double))
        {
            String mensaje = "No puedes asignar un texto a la variable numérica '" + id + "'";
            System.err.println("Error Semántico: " + mensaje);
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
            return null;
        }
        if (sim.getTipo().equals("string") && !(nuevoValor instanceof String))
        {
            String mensaje = "No puedes asignar un número a la variable de texto '" + id + "'";
            System.err.println("Error Semántico: " + mensaje);
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
            return null;
        }
        ent.reasignarVariable(id, nuevoValor);
        return null;
    }

    public String getId()
    {
        return this.id;
    }
}