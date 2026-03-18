package com.example.pokemonformularios.ast;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.widget.LinearLayout;

public class Entorno
{
    private Map<String, Simbolo> tablaSimbolos;
    private Entorno anterior;
    private Context contexto;
    private LinearLayout layoutActual;

    public Entorno(Entorno anterior, Context contexto, LinearLayout layoutActual)
    {
        this.tablaSimbolos = new HashMap<>();
        this.anterior = anterior;
        this.contexto = contexto;
        this.layoutActual = layoutActual;
    }

    public Entorno(Entorno anterior)
    {
        this.tablaSimbolos = new HashMap<>();
        this.anterior = anterior;
        if (anterior != null)
        {
            this.contexto = anterior.getContexto();
            this.layoutActual = anterior.getLayoutActual();
        }
    }

    public boolean agregarVariable(String id, String tipo, Object valor)
    {
        if (tablaSimbolos.containsKey(id))
        {
            return false;
        }
        tablaSimbolos.put(id, new Simbolo(id, tipo, valor));
        return true;
    }
    public Simbolo obtenerVariable(String id)
    {
        for (Entorno e = this; e != null; e = e.anterior)
        {
            if (e.tablaSimbolos.containsKey(id))
            {
                return e.tablaSimbolos.get(id);
            }
        }
        return null;
    }
    public boolean reasignarVariable(String id, Object nuevoValor)
    {
        for (Entorno e = this; e != null; e = e.anterior)
        {
            if (e.tablaSimbolos.containsKey(id))
            {
                Simbolo sim = e.tablaSimbolos.get(id);
                sim.setValor(nuevoValor);
                return true;
            }
        }
        return false;
    }

    public Context getContexto()
    {
        return contexto;
    }
    public LinearLayout getLayoutActual()
    {
        return layoutActual;
    }
    public void setLayoutActual(LinearLayout layoutActual)
    {
        this.layoutActual = layoutActual;
    }
}