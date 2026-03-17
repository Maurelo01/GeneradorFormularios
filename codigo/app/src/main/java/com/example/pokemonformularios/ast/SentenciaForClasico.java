package com.example.pokemonformularios.ast;
import java.util.List;

public class SentenciaForClasico implements Instruccion
{
    private Instruccion asignacionInicial;
    private Expresion condicion;
    private Instruccion asignacionActualizacion;
    private List<Instruccion> instrucciones;

    public SentenciaForClasico(Instruccion asignacionInicial, Expresion condicion, Instruccion asignacionActualizacion, List<Instruccion> instrucciones)
    {
        this.asignacionInicial = asignacionInicial;
        this.condicion = condicion;
        this.asignacionActualizacion = asignacionActualizacion;
        this.instrucciones = instrucciones;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        System.out.println(" Iniciando FOR CLÁSICO...");
        if (asignacionInicial != null)
        {
            asignacionInicial.ejecutar(ent);
        }
        while (true)
        {
            Object valorCondicion = condicion.evaluar(ent);
            if (!(valorCondicion instanceof Double))
            {
                System.err.println("Error Semántico: La condición del FOR debe ser numérica.");
                break;
            }
            if ((Double) valorCondicion < 1.0)
            {
                break;
            }
            Entorno entornoLocal = new Entorno(ent);
            for (Instruccion instr : instrucciones)
            {
                if (instr != null) instr.ejecutar(entornoLocal);
            }
            if (asignacionActualizacion != null)
            {
                asignacionActualizacion.ejecutar(ent);
            }
        }
        System.out.println(" Fin de FOR CLÁSICO");
        return null;
    }
}