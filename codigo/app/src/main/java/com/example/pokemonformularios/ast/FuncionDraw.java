package com.example.pokemonformularios.ast;

import java.util.List;
import com.example.pokemonformularios.reportes.ErrorCompi;

public class FuncionDraw implements Instruccion
{
    private String idVariable;
    private List<Object> argumentos;

    public FuncionDraw(String idVariable, List<Object> argumentos)
    {
        this.idVariable = idVariable;
        this.argumentos = argumentos;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Simbolo sim = ent.obtenerVariable(idVariable);
        if (sim == null)
        {
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "La variable especial '" + idVariable + "' no existe.", 0, 0));
            return null;
        }
        if (!"special".equals(sim.getTipo()))
        {
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "Solo las variables 'special' pueden usar la función .draw().", 0, 0));
            return null;
        }
        Object componenteGuardado = sim.getValor();
        if (componenteGuardado instanceof Instruccion)
        {
            Object valorArgumento = null;
            if (argumentos != null && !argumentos.isEmpty())
            {
                Object exp = argumentos.get(0);
                if (exp instanceof Expresion)
                {
                    valorArgumento = ((Expresion) exp).evaluar(ent);
                }
            }
            if (valorArgumento != null)
            {
                if (!ent.existeVariable("$$comodin$$"))
                {
                    ent.agregarVariable("$$comodin$$", "any", valorArgumento);
                }
                else
                {
                    ent.actualizarVariable("$$comodin$$", valorArgumento);
                }
            }
            ((Instruccion) componenteGuardado).ejecutar(ent);
        }
        return null;
    }
}