package com.example.pokemonformularios.ast;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.widget.LinearLayout;
import com.example.pokemonformularios.reportes.ErrorCompi;
import java.util.ArrayList;
import java.util.List;

public class Entorno
{
    private Map<String, Simbolo> tablaSimbolos;
    private Entorno anterior;
    private Context contexto;
    private LinearLayout layoutActual;
    private List<ErrorCompi> erroresSemanticos;
    private List<ComponentePregunta> preguntasFormulario;

    public Entorno(Entorno anterior, Context contexto, LinearLayout layoutActual)
    {
        this.tablaSimbolos = new HashMap<>();
        this.anterior = anterior;
        this.contexto = contexto;
        this.layoutActual = layoutActual;
        this.erroresSemanticos = new ArrayList<>();
        this.preguntasFormulario = new ArrayList<>();
    }

    public Entorno(Entorno anterior)
    {
        this.tablaSimbolos = new HashMap<>();
        this.anterior = anterior;
        if (anterior != null)
        {
            this.contexto = anterior.getContexto();
            this.layoutActual = anterior.getLayoutActual();
            this.erroresSemanticos = anterior.getErroresSemanticos();
            this.preguntasFormulario = anterior.getPreguntasFormulario();
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

    public void registrarPregunta(ComponentePregunta pregunta)
    {
        if (preguntasFormulario != null)
        {
            preguntasFormulario.add(pregunta);
        }
    }

    public void reportarErrorSemantico(String descripcion)
    {
        if (erroresSemanticos != null)
        {
            erroresSemanticos.add(new ErrorCompi("Semántico", descripcion, 0, 0));
        }
    }

    public Context getContexto()
    {
        return contexto;
    }
    public LinearLayout getLayoutActual()
    {
        return layoutActual;
    }
    public List<ErrorCompi> getErroresSemanticos()
    {
        return erroresSemanticos;
    }
    public List<ComponentePregunta> getPreguntasFormulario()
    {
        return preguntasFormulario;
    }
    public void setLayoutActual(LinearLayout layoutActual)
    {
        this.layoutActual = layoutActual;
    }
}