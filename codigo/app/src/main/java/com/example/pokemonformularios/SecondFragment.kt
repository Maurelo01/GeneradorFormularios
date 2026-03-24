package com.example.pokemonformularios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pokemonformularios.databinding.FragmentSecondBinding
import android.app.AlertDialog
import android.widget.*

class SecondFragment : Fragment()
{
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val btnVolver = view.findViewById<Button>(R.id.btnVolver)
        val btnCargar = view.findViewById<Button>(R.id.btnCargarPKMContestar)
        btnVolver.setOnClickListener{
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        btnCargar.setOnClickListener{
            abrirArchivoPKM.launch("*/*")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val abrirArchivoPKM = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent())
    { uri ->
        uri?.let{
            try
            {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val contenido = inputStream?.bufferedReader().use { reader -> reader?.readText() }
                if (contenido != null)
                {
                    mostrarArchivoPKM(contenido)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al leer el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generarEmojis(textoOriginal: String): String
    {
        var texto = textoOriginal
        texto = texto.replace(Regex("@\\[:\\)+\\]|@\\[:smile:\\]"), "\uD83D\uDE00")
        texto = texto.replace(Regex("@\\[:\\(+\\]|@\\[:sad:\\]"), "\uD83E\uDD72")
        texto = texto.replace(Regex("@\\[:\\]+\\]|@\\[:serious:\\]"), "\uD83D\uDE10")
        texto = texto.replace(Regex("@\\[<+3+\\]|@\\[:heart:\\]"), "❤\uFE0F")
        texto = texto.replace(Regex("@\\[:\\^\\^:\\]|@\\[:cat:\\]"), "\uD83D\uDE3A")
        texto = texto.replace(Regex("@\\[:star:\\]"), "⭐")
        return Regex("@\\[:star[:-](\\d+):?\\]").replace(texto) { matchResult ->
            "⭐".repeat(matchResult.groupValues[1].toIntOrNull() ?: 1)
        }
    }

    private fun cargarPokeApiLectura(vistaContenedor: View, comando: String, tipoPregunta: String, listaCheckboxes: MutableList<CheckBox>? = null)
    {
        val partes = comando.split(":")
        val inicio = partes[1].toInt()
        val fin = partes[2].toInt()
        val tvCargando = TextView(vistaContenedor.context)
        tvCargando.text = "Cargando Pokemon..."
        tvCargando.setTextColor(android.graphics.Color.GRAY)
        if (vistaContenedor is ViewGroup && vistaContenedor !is Spinner)
        {
            vistaContenedor.addView(tvCargando)
        }
        Thread{
            val descargados = mutableListOf<String>()
            for (i in inicio..fin)
            {
                try
                {
                    val url = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val nombre = org.json.JSONObject(response).getString("name")
                    descargados.add(nombre.replaceFirstChar { it.uppercase() })
                }
                catch (e: Exception)
                {
                    descargados.add("Error #$i")
                }
            }
            activity?.runOnUiThread{
                if (vistaContenedor is ViewGroup && vistaContenedor !is Spinner)
                {
                    vistaContenedor.removeView(tvCargando)
                }
                if (tipoPregunta == "DROP" && vistaContenedor is Spinner)
                {
                    val adapter = ArrayAdapter(vistaContenedor.context, android.R.layout.simple_spinner_item, listOf("--- Selecciona ---") + descargados)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    vistaContenedor.adapter = adapter
                }
                else if (tipoPregunta == "SELECT" && vistaContenedor is RadioGroup)
                {
                    descargados.forEach{ rb ->
                        vistaContenedor.addView(RadioButton(vistaContenedor.context).apply { text = rb })
                    }
                }
                else if (tipoPregunta == "MULTIPLE" && vistaContenedor is LinearLayout)
                {
                    descargados.forEach{ cb ->
                        val check = CheckBox(vistaContenedor.context).apply { text = cb }
                        vistaContenedor.addView(check)
                        listaCheckboxes?.add(check)
                    }
                }
            }
        }.start()
    }

    private fun extraerTexto(linea: String): String
    {
        val inicio = linea.indexOf("\"")
        if (inicio == -1) return ""
        val fin = linea.indexOf("\"", inicio + 1)
        if (fin == -1) return ""
        return linea.substring(inicio + 1, fin)
    }

    private fun extraerOpciones(linea: String): List<String>
    {
        val inicio = linea.indexOf("{")
        val fin = linea.indexOf("}")
        if (inicio == -1 || fin == -1) return emptyList()
        val contenido = linea.substring(inicio + 1, fin)
        return contenido.split(",").map { it.replace("\"", "").trim() }
    }

    private fun extraerRespuestaCorrecta(linea: String): String
    {
        val lastComma = linea.lastIndexOf(",")
        if (lastComma == -1) return ""
        var finStr = linea.indexOf("/>")
        if (finStr == -1) finStr = linea.indexOf(">")
        if (finStr == -1 || finStr <= lastComma) return ""
        return linea.substring(lastComma + 1, finStr).trim()
    }

    private fun mostrarArchivoPKM(codigoPKM: String)
    {
        val contenedorFormulario = view?.findViewById<LinearLayout>(R.id.contenedorLimpio) ?: return
        contenedorFormulario.removeAllViews()
        var seccionActual: LinearLayout? = null
        var tablaActual: TableLayout? = null
        var filaActual: TableRow? = null
        var ultimoElementoCreado: View? = null
        var ultimoLabel: TextView? = null
        val fondosEstilos = mutableMapOf<View, android.graphics.drawable.GradientDrawable>()
        val lineas = codigoPKM.split("\n")
        val regexBackgroundColor = Regex("<background color=(.*?)/>")
        val regexColor = Regex("<color=(.*?)/>")
        val regexFont = Regex("<font family=(.*?)/>")
        val regexSize = Regex("<text size=(.*?)>")
        val regexBorder = Regex("""<border=\((.*?),(.*?),(.*?)\)/>""")
        val evaluadores = mutableListOf<() -> Int>()
        var totalCalificables = 0
        for (linea in lineas)
        {
            val l = linea.trim()
            if (l.isEmpty() || l.startsWith("###") || l.contains(": ") || l == "<style>" || l == "</style>" || l == "<content>" || l == "</content>")
            {
                continue
            }

            if (l == "</section>") { seccionActual = null; continue }
            if (l == "</table>") { tablaActual = null; continue }
            if (l == "</line>") { filaActual = null; continue }
            if (l.startsWith("</")) { continue }

            if (regexBackgroundColor.matches(l))
            {
                val colorStr = regexBackgroundColor.find(l)!!.groupValues[1]
                val gd = fondosEstilos.getOrPut(ultimoElementoCreado!!) { android.graphics.drawable.GradientDrawable()}
                gd.setColor(parsearColores(colorStr))
                ultimoElementoCreado?.background = gd
                continue
            }
            else if (regexBorder.matches(l))
            {
                val match = regexBorder.find(l)!!
                val grosor = match.groupValues[1].trim().toFloat().toInt()
                val tipo = match.groupValues[2].trim()
                val colorBorde = parsearColores(match.groupValues[3].trim())
                val gd = fondosEstilos.getOrPut(ultimoElementoCreado!!) { android.graphics.drawable.GradientDrawable()}
                if (tipo == "DOTTED") gd.setStroke(grosor, colorBorde, 15f, 10f)
                else gd.setStroke(grosor, colorBorde)
                ultimoElementoCreado?.background = gd
                continue
            }
            else if (regexColor.matches(l))
            {
                val colorStr = regexColor.find(l)!!.groupValues[1]
                ultimoLabel?.setTextColor(parsearColores(colorStr))
                continue
            }
            else if (regexFont.matches(l))
            {
                val fontStr = regexFont.find(l)!!.groupValues[1]
                if (fontStr.contains("MONO")) ultimoLabel?.typeface = android.graphics.Typeface.MONOSPACE
                else if (fontStr.contains("SANS_SERIF")) ultimoLabel?.typeface = android.graphics.Typeface.SANS_SERIF
                else if (fontStr.contains("CURSIVE")) ultimoLabel?.typeface = android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                continue
            }
            else if (regexSize.matches(l))
            {
                val sizeStr = regexSize.find(l)!!.groupValues[1]
                ultimoLabel?.textSize = sizeStr.toFloat()
                continue
            }

            val obtenerDestino: () -> LinearLayout = lambda@
            {
                if (filaActual != null)
                {
                    val celda = LinearLayout(requireContext())
                    celda.orientation = LinearLayout.VERTICAL
                    val border = android.graphics.drawable.GradientDrawable()
                    border.setColor(android.graphics.Color.WHITE)
                    border.setStroke(2, android.graphics.Color.parseColor("#CCCCCC"))
                    celda.background = border
                    celda.setPadding(16, 16, 16, 16)
                    val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(4, 4, 4, 4)
                    celda.layoutParams = params

                    filaActual!!.addView(celda)
                    return@lambda celda
                }
                else
                {
                    return@lambda seccionActual ?: contenedorFormulario
                }
            }

            if (l.startsWith("<section="))
            {
                seccionActual = LinearLayout(requireContext()).apply{
                    orientation = LinearLayout.VERTICAL
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 48)
                    layoutParams = params
                    setPadding(32, 32, 32, 32)
                }
                contenedorFormulario.addView(seccionActual)
                ultimoElementoCreado = seccionActual
                ultimoLabel = null
            }
            else if (l.startsWith("<table="))
            {
                tablaActual = TableLayout(requireContext()).apply{
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 16)
                    layoutParams = params
                    setPadding(16, 16, 16, 16)
                }
                (seccionActual ?: contenedorFormulario).addView(tablaActual)
                ultimoElementoCreado = tablaActual
                ultimoLabel = null
            }
            else if (l == "<line>")
            {
                filaActual = TableRow(requireContext())
                tablaActual?.addView(filaActual)
            }
            else if (l.startsWith("<text="))
            {
                val contenido = extraerTexto(l)
                val tv = TextView(requireContext()).apply{
                    text = generarEmojis(contenido)
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#333333"))
                    setPadding(16, 16, 16, 16)
                }
                obtenerDestino().addView(tv)
                ultimoElementoCreado = tv
                ultimoLabel = tv
            }
            else if (l.startsWith("<open=") || l.startsWith("<select=") || l.startsWith("<multiple=") || l.startsWith("<drop="))
            {
                var labelLimpio = extraerTexto(l)

                val layoutPregunta = LinearLayout(requireContext()).apply{
                    orientation = LinearLayout.VERTICAL
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 32)
                    layoutParams = params
                    setPadding(24, 24, 24, 24)
                }
                val tvLabel = TextView(requireContext()).apply {
                    textSize = 18f
                    setTextColor(android.graphics.Color.parseColor("#111111"))
                    setPadding(0, 0, 0, 16)
                    text = generarEmojis(labelLimpio)
                }

                layoutPregunta.addView(tvLabel)

                if (l.startsWith("<open="))
                {
                    val et = EditText(requireContext())
                    et.hint = "Tu respuesta..."
                    layoutPregunta.addView(et)
                }
                else if (l.startsWith("<select="))
                {
                    val opciones = extraerOpciones(l)
                    val correctaStr = extraerRespuestaCorrecta(l)
                    val rg = RadioGroup(requireContext())
                    layoutPregunta.addView(rg)
                    if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                    {
                        cargarPokeApiLectura(rg, opciones[0], "SELECT")
                    }
                    else
                    {
                        opciones.forEach{ opt ->
                            val rb = RadioButton(requireContext())
                            rb.text = generarEmojis(opt)
                            rg.addView(rb)
                        }
                    }
                    val indexCorrecto = correctaStr.toDoubleOrNull()?.toInt() ?: -1
                    if (indexCorrecto != -1)
                    {
                        totalCalificables++
                        evaluadores.add{
                            val rbId = rg.checkedRadioButtonId
                            if (rbId != -1 && rg.indexOfChild(rg.findViewById<View>(rbId)) == indexCorrecto) 1 else 0
                        }
                    }
                }
                else if (l.startsWith("<multiple="))
                {
                    val opciones = extraerOpciones(l)
                    val correctaStr = extraerRespuestaCorrecta(l)
                    val listaCheckboxes = mutableListOf<CheckBox>()

                    if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                    {
                        cargarPokeApiLectura(layoutPregunta, opciones[0], "MULTIPLE", listaCheckboxes)
                    }
                    else
                    {
                        opciones.forEach{ opt ->
                            val cb = CheckBox(requireContext())
                            cb.text = generarEmojis(opt)
                            layoutPregunta.addView(cb)
                            listaCheckboxes.add(cb)
                        }
                    }
                    val correctasIds = correctaStr.replace("{", "").replace("}", "").split(",").mapNotNull { it.trim().toDoubleOrNull()?.toInt() }
                    if (correctasIds.isNotEmpty() && correctasIds[0] != -1)
                    {
                        totalCalificables++
                        evaluadores.add{
                            var marcadas = 0
                            var aciertos = 0
                            for (i in listaCheckboxes.indices)
                            {
                                if (listaCheckboxes[i].isChecked)
                                {
                                    marcadas++
                                    if (correctasIds.contains(i)) aciertos++
                                }
                            }
                            if (aciertos == correctasIds.size && marcadas == correctasIds.size) 1 else 0
                        }
                    }
                }
                else if (l.startsWith("<drop="))
                {
                    val opciones = extraerOpciones(l)
                    val correctaStr = extraerRespuestaCorrecta(l)
                    val spinner = Spinner(requireContext())

                    if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                    {
                        cargarPokeApiLectura(spinner, opciones[0], "DROP")
                    }
                    else
                    {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(" Selecciona ") + opciones.map { generarEmojis(it) })
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
                    layoutPregunta.addView(spinner)
                    val indexCorrecto = correctaStr.toDoubleOrNull()?.toInt() ?: -1
                    if (indexCorrecto != -1)
                    {
                        totalCalificables++
                        evaluadores.add { if (spinner.selectedItemPosition - 1 == indexCorrecto) 1 else 0 }
                    }
                }

                obtenerDestino().addView(layoutPregunta)
                ultimoElementoCreado = layoutPregunta
                ultimoLabel = tvLabel
            }
        }

        val btnEnviar = Button(requireContext()).apply{
            text = "ENVIAR FORMULARIO"
            setBackgroundColor(android.graphics.Color.parseColor("#29446F"))
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply{
                setMargins(0, 48, 0, 32)
            }
            setOnClickListener{
                var totalPuntos = 0
                for (evaluador in evaluadores) totalPuntos += evaluador()
                if (totalCalificables > 0)
                {
                    AlertDialog.Builder(requireContext()).setTitle("Formulario Enviado").setMessage("Tu puntuación es:\n$totalPuntos de $totalCalificables correctas.").setPositiveButton("Aceptar", null).show()
                }
                else
                {
                    Toast.makeText(requireContext(), "Formulario enviado exitosamente.", Toast.LENGTH_LONG).show()
                }
            }
        }
        contenedorFormulario.addView(btnEnviar)
        Toast.makeText(requireContext(), "Modo Contestar: Formulario Listo", Toast.LENGTH_SHORT).show()
    }

    private fun parsearColores(colorOriginal: String): Int
    {
        val c = colorOriginal.trim().replace("\"", "").uppercase()
        return when (c)
        {
            "RED" -> android.graphics.Color.parseColor("#F80000")
            "BLUE" -> android.graphics.Color.parseColor("#3F48F4")
            "GREEN" -> android.graphics.Color.parseColor("#C6DA52")
            "YELLOW" -> android.graphics.Color.parseColor("#FFFF00")
            "PURPLE" -> android.graphics.Color.parseColor("#8800FF")
            "SKY" -> android.graphics.Color.parseColor("#DDF4F5")
            "BLACK" -> android.graphics.Color.parseColor("#000000")
            "WHITE" -> android.graphics.Color.parseColor("#FFFFFF")
            else ->
            {
                try
                {
                    if (c.startsWith("(") && c.endsWith(")"))
                    {
                        val rgb = c.replace("(", "").replace(")", "").split(",")
                        android.graphics.Color.rgb(rgb[0].trim().toInt(), rgb[1].trim().toInt(), rgb[2].trim().toInt())
                    }
                    else if (c.startsWith("<") && c.endsWith(">"))
                    {
                        val hsl = c.replace("<", "").replace(">", "").split(",")
                        androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hsl[0].trim().toFloat(), hsl[1].trim().toFloat() / 100f, hsl[2].trim().toFloat() / 100f))
                    }
                    else
                    {
                        android.graphics.Color.parseColor(c)
                    }
                }
                catch (e: Exception)
                {
                    android.graphics.Color.TRANSPARENT
                }
            }
        }
    }
}