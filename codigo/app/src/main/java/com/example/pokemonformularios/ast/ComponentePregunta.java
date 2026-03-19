package com.example.pokemonformularios.ast;
import java.util.List;
import java.util.ArrayList;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Looper;

public class ComponentePregunta implements Instruccion
{
    private String tipoPregunta;
    private List<Atributo> atributos;
    private Object respuestaCorrecta = null;
    private View vistaEntrada = null;
    private List<CheckBox> listaCheckboxes = new ArrayList<>();
    public ComponentePregunta(String tipoPregunta, List<Atributo> atributos)
    {
        this.tipoPregunta = tipoPregunta;
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        String label = "Pregunta sin título";
        List<Object> opciones = new ArrayList<>();
        if (atributos != null)
        {
            for (Atributo attr : atributos)
            {
                if (attr.getNombre().equals("label"))
                {
                    Object val = ((Expresion) attr.getValor()).evaluar(ent);
                    if (val != null) label = val.toString();
                }
                else if (attr.getNombre().equals("options"))
                {
                    opciones = (List<Object>) attr.getValor();
                }
                else if (attr.getNombre().equals("correct"))
                {
                    respuestaCorrecta = ((Expresion) attr.getValor()).evaluar(ent);
                }
                else if (attr.getNombre().equals("correct_multiple"))
                {
                    List<Object> expresiones = (List<Object>) attr.getValor();
                    List<Integer> correctasEvaluadas = new ArrayList<>();
                    for (Object exp : expresiones)
                    {
                        if (exp instanceof Expresion)
                        {
                            Object val = ((Expresion) exp).evaluar(ent);
                            if (val instanceof Double)
                            {
                                correctasEvaluadas.add(Double.valueOf(val.toString()).intValue());
                            }
                        }
                    }
                    respuestaCorrecta = correctasEvaluadas;
                }
            }
        }
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            LinearLayout layoutPregunta = new LinearLayout(ent.getContexto());
            layoutPregunta.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 48);
            layoutPregunta.setLayoutParams(params);
            TextView tvLabel = new TextView(ent.getContexto());
            tvLabel.setText(label);
            tvLabel.setTextSize(16f);
            tvLabel.setTextColor(Color.parseColor("#333333"));
            tvLabel.setPadding(0, 0, 0, 16);
            layoutPregunta.addView(tvLabel);
            switch (tipoPregunta)
            {
                case "OPEN":
                    EditText editText = new EditText(ent.getContexto());
                    editText.setHint("Tu respuesta...");
                    layoutPregunta.addView(editText);
                    this.vistaEntrada = editText;
                    break;
                case "SELECT":
                    RadioGroup radioGroup = new RadioGroup(ent.getContexto());
                    for (Object obj : opciones)
                    {
                        if (obj instanceof Expresion)
                        {
                            Object val = ((Expresion) obj).evaluar(ent);
                            String textoOpcion = val != null ? val.toString() : "";
                            RadioButton rb = new RadioButton(ent.getContexto());
                            rb.setText(textoOpcion);
                            radioGroup.addView(rb);
                        }
                    }
                    layoutPregunta.addView(radioGroup);
                    this.vistaEntrada = radioGroup;
                    break;
                case "MULTIPLE":
                    for (Object obj : opciones)
                    {
                        if (obj instanceof Expresion)
                        {
                            Object val = ((Expresion) obj).evaluar(ent);
                            String textoOpcion = val != null ? val.toString() : "";
                            CheckBox cb = new CheckBox(ent.getContexto());
                            cb.setText(textoOpcion);
                            layoutPregunta.addView(cb);
                            this.listaCheckboxes.add(cb);
                        }
                    }
                    break;
                case "DROP":
                    Spinner spinner = new Spinner(ent.getContexto());
                    List<String> opcionesString = new ArrayList<>();
                    opcionesString.add(" Selecciona ");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ent.getContexto(), android.R.layout.simple_spinner_item, opcionesString);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    layoutPregunta.addView(spinner);
                    this.vistaEntrada = spinner;
                    boolean requiereApi = false;
                    int pokeInicio = 1;
                    int pokeFin = 1;
                    for (Object obj : opciones)
                    {
                        if (obj instanceof Expresion)
                        {
                            Object val = ((Expresion) obj).evaluar(ent);
                            if (val != null && val.toString().startsWith("POKEAPI:"))
                            {
                                requiereApi = true;
                                String[] parts = val.toString().split(":");
                                pokeInicio = Integer.parseInt(parts[1]);
                                pokeFin = Integer.parseInt(parts[2]);
                            }
                            else
                            {
                                opcionesString.add(val != null ? val.toString() : "");
                            }
                        }
                    }
                    if (requiereApi)
                    {
                        opcionesString.add("Cargando Pokémon...");
                        adapter.notifyDataSetChanged();
                        cargarPokemon(pokeInicio, pokeFin, opcionesString, adapter);
                    }
                    break;
            }
            ent.getLayoutActual().addView(layoutPregunta);
            ent.registrarPregunta(this);
        }
        return null;
    }

    public int evaluarPuntos()
    {
        if (respuestaCorrecta == null) return 0;
        try
        {
            if (tipoPregunta.equals("SELECT") && vistaEntrada instanceof RadioGroup)
            {
                RadioGroup rg = (RadioGroup) vistaEntrada;
                int radioButtonID = rg.getCheckedRadioButtonId();
                if (radioButtonID != -1)
                {
                    View radioButton = rg.findViewById(radioButtonID);
                    int indiceSeleccionado = rg.indexOfChild(radioButton) + 1;
                    int indiceCorrecto = Double.valueOf(respuestaCorrecta.toString()).intValue();
                    if (indiceSeleccionado == indiceCorrecto) return 1;
                }
            }
            else if (tipoPregunta.equals("DROP") && vistaEntrada instanceof Spinner)
            {
                Spinner sp = (Spinner) vistaEntrada;
                int indiceSeleccionado = sp.getSelectedItemPosition();
                int indiceCorrecto = Double.valueOf(respuestaCorrecta.toString()).intValue();
                if (indiceSeleccionado == indiceCorrecto) return 1;
            }
            else if (tipoPregunta.equals("MULTIPLE"))
            {
                @SuppressWarnings("unchecked")
                List<Integer> correctas = (List<Integer>) respuestaCorrecta;
                int opcionesMarcadas = 0;
                int aciertos = 0;
                for (int i = 0; i < listaCheckboxes.size(); i++)
                {
                    if (listaCheckboxes.get(i).isChecked())
                    {
                        opcionesMarcadas++;
                        if (correctas.contains(i + 1))
                        {
                            aciertos++;
                        }
                    }
                }
                if (aciertos == correctas.size() && opcionesMarcadas == correctas.size())
                {
                    return 1;
                }
                return 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean esCalificable()
    {
        return respuestaCorrecta != null && !tipoPregunta.equals("OPEN");
    }

    private void cargarPokemon(int inicio, int fin, List<String> opcionesString, ArrayAdapter<String> adapter)
    {
        new Thread(() ->
        {
            List<String> pokemonesDescargados = new ArrayList<>();
            for (int i = inicio; i <= fin; i++)
            {
                try
                {
                    URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + i);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null)
                    {
                        response.append(linea);
                    }
                    reader.close();
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String nombre = jsonObject.getString("name");
                    nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
                    pokemonesDescargados.add(nombre);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    pokemonesDescargados.add("Error con la pokedex o PC #" + i);
                }
            }
            new Handler(Looper.getMainLooper()).post(() ->
            {
                opcionesString.remove(opcionesString.size() - 1);
                opcionesString.addAll(pokemonesDescargados);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}