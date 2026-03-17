package com.example.pokemonformularios.ast;
import java.util.HashMap;
import java.util.Map;

public class Entorno
{
    private Map<String, Simbolo> tabla;
    private Entorno padre;
    public Entorno(Entorno padre)
    {
        this.tabla = new HashMap<>();
        this.padre = padre;
    }

    public boolean agregarVariable(String id, String tipo, Object valor)
    {
        if (tabla.containsKey(id))
        {
            return false;
        }
        tabla.put(id, new Simbolo(id, tipo, valor));
        return true;
    }
    public Simbolo obtenerVariable(String id)
    {
        for (Entorno e = this; e != null; e = e.padre)
        {
            if (e.tabla.containsKey(id))
            {
                return e.tabla.get(id);
            }
        }
        return null;
    }
    public boolean reasignarVariable(String id, Object nuevoValor)
    {
        for (Entorno e = this; e != null; e = e.padre)
        {
            if (e.tabla.containsKey(id))
            {
                Simbolo sim = e.tabla.get(id);
                sim.setValor(nuevoValor);
                return true;
            }
        }
        return false;
    }
}