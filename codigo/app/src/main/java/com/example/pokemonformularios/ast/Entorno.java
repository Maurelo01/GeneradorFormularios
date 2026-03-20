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
    private StringBuilder pkmBuilder;
    public int totalSecciones = 0;
    public int totalPreguntas = 0;
    public int abiertas = 0;
    public int desplegables = 0;
    public int seleccion = 0;
    public int multiples = 0;

    public Entorno(Entorno anterior, Context contexto, LinearLayout layoutActual)
    {
        this.tablaSimbolos = new HashMap<>();
        this.anterior = anterior;
        this.contexto = contexto;
        this.layoutActual = layoutActual;
        this.erroresSemanticos = new ArrayList<>();
        this.preguntasFormulario = new ArrayList<>();
        this.pkmBuilder = new StringBuilder();
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
            this.pkmBuilder = anterior.getPkmBuilder();
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

    public void registrarSeccionPKM()
    {
        if (this.anterior != null)
        {
            this.anterior.registrarSeccionPKM();
        }
        else
        {
            this.totalSecciones++;
        }
    }

    public void registrarPreguntaPKM(String tipoPregunta)
    {
        if (this.anterior != null)
        {
            this.anterior.registrarPreguntaPKM(tipoPregunta);
        }
        else
        {
            this.totalPreguntas++;
            switch (tipoPregunta)
            {
                case "OPEN": this.abiertas++; break;
                case "DROP": this.desplegables++; break;
                case "SELECT": this.seleccion++; break;
                case "MULTIPLE": this.multiples++; break;
            }
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
    public StringBuilder getPkmBuilder()
    {
        return pkmBuilder;
    }

    public int getTotalSecciones()
    {
        return this.anterior != null ? this.anterior.getTotalSecciones() : this.totalSecciones;
    }
    public int getTotalPreguntas()
    {
        return this.anterior != null ? this.anterior.getTotalPreguntas() : this.totalPreguntas;
    }
    public int getAbiertas()
    {
        return this.anterior != null ? this.anterior.getAbiertas() : this.abiertas;
    }
    public int getDesplegables()
    {
        return this.anterior != null ? this.anterior.getDesplegables() : this.desplegables;
    }
    public int getSeleccion()
    {
        return this.anterior != null ? this.anterior.getSeleccion() : this.seleccion;
    }
    public int getMultiples()
    {
        return this.anterior != null ? this.anterior.getMultiples() : this.multiples;
    }

    public void setLayoutActual(LinearLayout layoutActual)
    {
        this.layoutActual = layoutActual;
    }
}