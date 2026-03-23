package com.example.pokemonformularios

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pokemonformularios.analizador.LexerFormulario
import com.example.pokemonformularios.analizador.ParserFormulario
import com.example.pokemonformularios.ast.Entorno
import com.example.pokemonformularios.ast.Instruccion
import com.example.pokemonformularios.reportes.ErrorCompi
import java.io.StringReader

class FirstFragment : Fragment()
{
    private var listaErroresActuales: List<ErrorCompi> = emptyList()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val btnCompilar = view.findViewById<Button>(R.id.btnCompilar)
        val btnErrores = view.findViewById<Button>(R.id.btnErrores)
        val entradaCodigo = view.findViewById<EditText>(R.id.entradaCodigo)
        val contenedorFormulario = view.findViewById<LinearLayout>(R.id.contenedorFormulario)
        val btnPlantilla = view.findViewById<Button>(R.id.btnPlantilla)
        val btnColor = view.findViewById<Button>(R.id.btnColor)
        val btnExportarPKM = view.findViewById<Button>(R.id.btnExportarPKM)
        val btnCargarPKM = view.findViewById<Button>(R.id.btnCargarPKM)
        btnErrores.isEnabled = false
        btnExportarPKM.visibility = View.GONE
        entradaCodigo.addTextChangedListener(object : android.text.TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?)
            {
                entradaCodigo.removeTextChangedListener(this)
                colorearSintaxis(s)
                entradaCodigo.addTextChangedListener(this)
            }
        })

        btnPlantilla.setOnClickListener{
            val codigoPlantilla = """
                number titulo = 1
                WHILE (titulo <= 3) {
                    SECTION [
                        elements: {
                            TEXT [ content: "Texto generado numero: " + titulo ]
                        }
                    ]
                    titulo = titulo + 1
                }
            """.trimIndent()
            entradaCodigo.setText(codigoPlantilla)
            Toast.makeText(requireContext(), "Plantilla cargada", Toast.LENGTH_SHORT).show()
        }

        btnColor.setOnClickListener{
            mostrarSelectorDeColor(entradaCodigo)
        }

        btnCargarPKM.setOnClickListener{
            abrirArchivoPKM.launch("*/*")
        }

        btnCompilar.setOnClickListener{
            procesarCompilacion(entradaCodigo, contenedorFormulario, btnErrores, btnExportarPKM)
        }

        btnErrores.setOnClickListener{
            mostrarTablaErrores()
        }
    }

    private fun mostrarArchivoPKM(codigoPKM: String)
    {
        val contenedorFormulario = view?.findViewById<LinearLayout>(R.id.contenedorFormulario) ?: return
        contenedorFormulario.removeAllViews()
        var seccionActual: LinearLayout? = null
        var tablaActual: TableLayout? = null
        var filaActual: TableRow? = null
        val lineas = codigoPKM.split("\n")
        val regexSeccion = Regex("<section=.*?>")
        val regexCierreSeccion = Regex("</section>")
        val regexContent = Regex("<content>|</content>")
        val regexOpen = Regex("""<open=-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?,\s*"(.*?)"\s*/>""")
        val regexSelect = Regex("""<select=-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?,\s*"(.*?)",\s*\{(.*?)\},\s*(.+?)\s*/>""")
        val regexMultiple = Regex("""<multiple=-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?,\s*"(.*?)",\s*\{(.*?)\},\s*(.+?)\s*/>""")
        val regexDrop = Regex("""<drop=-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?,\s*"(.*?)",\s*\{(.*?)\},\s*(.+?)\s*/>""")
        val regexTabla = Regex("<table=-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?>")
        val regexCierreTabla = Regex("</table>")
        val regexLinea = Regex("<line>")
        val regexCierreLinea = Regex("</line>")
        val regexTexto = Regex("""<text=-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?,\s*"(.*?)"\s*/>""")
        val evaluadores = mutableListOf<() -> Int>()
        var totalCalificables = 0
        for (linea in lineas)
        {
            val l = linea.trim()
            if (l.isEmpty() || regexContent.matches(l) || l.startsWith("###") || l.contains(": ")) continue
            val obtenerDestino: () -> LinearLayout =
            {
                if (filaActual != null)
                {
                    val celda = LinearLayout(requireContext())
                    celda.orientation = LinearLayout.VERTICAL
                    val border = android.graphics.drawable.GradientDrawable()
                    border.setColor(android.graphics.Color.WHITE)
                    border.setStroke(3, android.graphics.Color.BLACK)
                    celda.background = border
                    celda.setPadding(16, 16, 16, 16)
                    val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(4, 4, 4, 4)
                    celda.layoutParams = params
                    filaActual!!.addView(celda)
                    celda
                }
                else
                {
                    seccionActual ?: contenedorFormulario
                }
            }
            if (regexSeccion.matches(l))
            {
                seccionActual = LinearLayout(requireContext())
                seccionActual?.orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 48)
                seccionActual?.layoutParams = params
                contenedorFormulario.addView(seccionActual)
            }
            else if (regexCierreSeccion.matches(l))
            {
                seccionActual = null
            }
            else if (regexTabla.matches(l))
            {
                tablaActual = TableLayout(requireContext())
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                tablaActual!!.layoutParams = params
                tablaActual!!.isStretchAllColumns = true
                (seccionActual ?: contenedorFormulario).addView(tablaActual)
            }
            else if (regexCierreTabla.matches(l))
            {
                tablaActual = null
            }
            else if (regexLinea.matches(l))
            {
                filaActual = TableRow(requireContext())
                tablaActual?.addView(filaActual)
            }
            else if (regexCierreLinea.matches(l))
            {
                filaActual = null
            }
            else if (regexOpen.matches(l))
            {
                val coincidencia = regexOpen.find(l)!!
                val labelLimpio = coincidencia.groupValues[3].replace("\"", "")
                val destino = obtenerDestino()
                agregarLabelPKM(destino, labelLimpio)
                val et = EditText(requireContext())
                et.hint = "Tu respuesta..."
                destino.addView(et)
            }
            else if (regexSelect.matches(l))
            {
                val coincidencia = regexSelect.find(l)!!
                val labelLimpio = coincidencia.groupValues[3].replace("\"", "")
                val destino = obtenerDestino()
                agregarLabelPKM(destino, labelLimpio)
                val opciones = coincidencia.groupValues[4].split(",").map { it.replace("\"", "").trim() }
                val correctaStr = coincidencia.groupValues[5].trim()
                val rg = RadioGroup(requireContext())
                destino.addView(rg)
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
                        if (rbId != -1)
                        {
                            val rb = rg.findViewById<View>(rbId)
                            val indiceSeleccionado = rg.indexOfChild(rb)
                            if (indiceSeleccionado == indexCorrecto) 1 else 0
                        }
                        else 0
                    }
                }
            }
            else if (regexMultiple.matches(l))
            {
                val coincidencia = regexMultiple.find(l)!!
                val labelLimpio = coincidencia.groupValues[3].replace("\"", "")
                val destino = obtenerDestino()
                agregarLabelPKM(destino, labelLimpio)
                val opciones = coincidencia.groupValues[4].split(",").map { it.replace("\"", "").trim() }
                val correctaStr = coincidencia.groupValues[5].trim()
                val listaCheckboxes = mutableListOf<CheckBox>()
                val contenedorMultiple = destino
                if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                {
                    cargarPokeApiLectura(destino, opciones[0], "MULTIPLE", listaCheckboxes)
                }
                else
                {
                    opciones.forEach { opt ->
                        val cb = CheckBox(requireContext())
                        cb.text = generarEmojis(opt)
                        contenedorMultiple.addView(cb)
                        listaCheckboxes.add(cb)
                    }
                }
                val correctasLimpio = correctaStr.replace("{", "").replace("}", "")
                val correctasIds = correctasLimpio.split(",").mapNotNull { it.trim().toDoubleOrNull()?.toInt() }
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
            else if (regexDrop.matches(l))
            {
                val coincidencia = regexDrop.find(l)!!
                val labelLimpio = coincidencia.groupValues[3].replace("\"", "")
                val destino = obtenerDestino()
                agregarLabelPKM(destino, labelLimpio)
                val opciones = coincidencia.groupValues[4].split(",").map { it.replace("\"", "").trim() }
                val correctaStr = coincidencia.groupValues[5].trim()
                val spinner = Spinner(requireContext())
                if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                {
                    cargarPokeApiLectura(spinner, opciones[0], "DROP")
                }
                else
                {
                    val opcionesGeneradas = opciones.map { generarEmojis(it) }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(" Selecciona ") + opciones)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
                destino.addView(spinner)
                val indexCorrecto = correctaStr.toDoubleOrNull()?.toInt() ?: -1
                if (indexCorrecto != -1)
                {
                    totalCalificables++
                    evaluadores.add{
                        val indiceSeleccionado = spinner.selectedItemPosition
                        if (indiceSeleccionado -1 == indexCorrecto) 1 else 0
                    }
                }
            }
            else if (regexTexto.matches(l))
            {
                val coincidencia = regexTexto.find(l)!!
                val contenido = coincidencia.groupValues[3]
                val destino = obtenerDestino()
                agregarLabelPKM(destino, contenido)
            }
        }
        val btnEnviar = Button(requireContext())
        btnEnviar.text = "ENVIAR FORMULARIO"
        btnEnviar.setBackgroundColor(android.graphics.Color.parseColor("#29446F"))
        btnEnviar.setTextColor(android.graphics.Color.WHITE)
        val paramsBtn = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsBtn.setMargins(0, 32, 0, 32)
        btnEnviar.layoutParams = paramsBtn
        btnEnviar.setOnClickListener{
            var totalPuntos = 0
            for (evaluador in evaluadores)
            {
                totalPuntos += evaluador()
            }
            if (totalCalificables > 0)
            {
                AlertDialog.Builder(requireContext()).setTitle("Formulario Enviado").setMessage("Tu puntuación es:\n$totalPuntos de $totalCalificables correctas.").setPositiveButton("Aceptar", null).show()
            }
            else
            {
                Toast.makeText(requireContext(), "Formulario enviado exitosamente.", Toast.LENGTH_LONG).show()
            }
        }
        contenedorFormulario.addView(btnEnviar)
        Toast.makeText(requireContext(), "Formulario .PKM cargado y listo", Toast.LENGTH_LONG).show()
    }

    private fun agregarLabelPKM(parent: LinearLayout, text: String)
    {
        val tv = TextView(requireContext())
        tv.text = generarEmojis(text)
        tv.textSize = 16f
        tv.setTextColor(android.graphics.Color.parseColor("#333333"))
        tv.setPadding(0, 16, 0, 16)
        parent.addView(tv)
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
        Thread {
            val descargados = mutableListOf<String>()
            for (i in inicio..fin)
            {
                try
                {
                    val url = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    val response = conn.inputStream.bufferedReader().use{ it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    var nombre = jsonObject.getString("name")
                    nombre = nombre.replaceFirstChar { it.uppercase() }
                    descargados.add(nombre)
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
                    descargados.forEach{ nombre ->
                        val rb = RadioButton(vistaContenedor.context)
                        rb.text = nombre
                        vistaContenedor.addView(rb)
                    }
                }
                else if (tipoPregunta == "MULTIPLE" && vistaContenedor is LinearLayout)
                {
                    descargados.forEach { nombre ->
                        val cb = CheckBox(vistaContenedor.context)
                        cb.text = nombre
                        vistaContenedor.addView(cb)
                        listaCheckboxes?.add(cb)
                    }
                }
            }
        }.start()
    }

    private fun colorearSintaxis(editable: android.text.Editable?)
    {
        if (editable == null) return
        val texto = editable.toString()
        val spans = editable.getSpans(0, editable.length, android.text.style.ForegroundColorSpan::class.java)
        for (span in spans)
        {
            editable.removeSpan(span)
        }
        fun aplicarColor(regex: String, colorHex: String)
        {
            val patron = java.util.regex.Pattern.compile(regex)
            val matcher = patron.matcher(texto)
            while (matcher.find())
            {
                editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor(colorHex)), matcher.start(), matcher.end(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        aplicarColor("(\\$.*)|(/\\*[\\s\\S]*?\\*/)", "#808080")
        aplicarColor("[+\\-*/^%]", "#88DC65")
        aplicarColor("\"[^\"]*\"", "#FF8000")
        aplicarColor("\\b\\d+(\\.\\d+)?\\b", "#A3D5EB")
        aplicarColor("\\b(SECTION|TABLE|TEXT|OPEN_QUESTION|DROP_QUESTION|SELECT_QUESTION|MULTIPLE_QUESTION|IF|ELSE|WHILE|DO|FOR|in|number|string|special)\\b", "#8800FF")
        aplicarColor("[{}\\[\\]()]", "#0000FF")
        aplicarColor("@\\[.*?\\]", "#FFDE21")
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
        val regexStar = Regex("@\\[:star[:-](\\d+):?\\]")
        texto = regexStar.replace(texto)
        { matchResult ->
            val cantidad = matchResult.groupValues[1].toIntOrNull() ?: 1
            "⭐".repeat(cantidad)
        }
        return texto
    }

    private fun procesarCompilacion(entradaCodigo: EditText, contenedorFormulario: LinearLayout, btnErrores: Button, btnExportarPKM: Button)
    {
        contenedorFormulario.removeAllViews()
        listaErroresActuales = emptyList()
        btnErrores.isEnabled = false
        btnExportarPKM.visibility = View.GONE
        val codigoStr = entradaCodigo.text.toString()
        if (codigoStr.isBlank()) return
        var lexer: LexerFormulario? = null
        var parser: ParserFormulario? = null
        try
        {
            lexer = LexerFormulario(StringReader(codigoStr))
            parser = ParserFormulario(lexer)
            val sym = parser.parse()
            val todosLosErrores = mutableListOf<ErrorCompi>()
            todosLosErrores.addAll(lexer.erroresLexicos)
            todosLosErrores.addAll(parser.erroresSintacticos)
            if (todosLosErrores.isEmpty())
            {
                val raizAST = sym.value as? Instruccion
                val entornoGlobal = Entorno(null, requireContext(), contenedorFormulario)
                raizAST?.ejecutar(entornoGlobal)
                btnExportarPKM.visibility = View.VISIBLE
                btnExportarPKM.setOnClickListener{
                    mostrarDialogoGuardarPKM(entornoGlobal)
                }
                val btnEnviar = Button(requireContext())
                btnEnviar.text = "Enviar Formulario"
                btnEnviar.setBackgroundColor(android.graphics.Color.parseColor("#29446F"))
                btnEnviar.setTextColor(android.graphics.Color.WHITE)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 32, 0, 32)
                btnEnviar.layoutParams = params
                btnEnviar.setOnClickListener{
                    val preguntas = entornoGlobal.preguntasFormulario
                    var totalPuntos = 0
                    var totalCalificables = 0
                    for (pregunta in preguntas)
                    {
                        if (pregunta.esCalificable())
                        {
                            totalCalificables++
                            totalPuntos += pregunta.evaluarPuntos()
                        }
                    }
                    if (totalCalificables > 0)
                    {
                        AlertDialog.Builder(requireContext()).setTitle("Formulario Enviado").setMessage("Tu puntuación es:\n$totalPuntos de $totalCalificables correctas.").setPositiveButton("Aceptar", null).show()
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "Formulario enviado exitosamente.", Toast.LENGTH_LONG).show()
                    }
                }
                contenedorFormulario.addView(btnEnviar)
                if (entornoGlobal.erroresSemanticos.isNotEmpty())
                {
                    todosLosErrores.addAll(entornoGlobal.erroresSemanticos)
                    listaErroresActuales = todosLosErrores
                    btnErrores.isEnabled = true
                    Toast.makeText(requireContext(), "Formulario generado, pero con Errores Semánticos", Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(requireContext(), "¡Formulario Generado con éxito!", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                listaErroresActuales = todosLosErrores
                btnErrores.isEnabled = true
                Toast.makeText(requireContext(), "Hay ${todosLosErrores.size} error(es). Revisa la tabla.", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            val erroresRescatados = mutableListOf<ErrorCompi>()
            lexer?.erroresLexicos?.let { erroresRescatados.addAll(it) }
            parser?.erroresSintacticos?.let { erroresRescatados.addAll(it) }

            if (erroresRescatados.isNotEmpty())
            {
                listaErroresActuales = erroresRescatados
                btnErrores.isEnabled = true
                Toast.makeText(requireContext(), "Caida Sintáctico. Se rescataron ${erroresRescatados.size} errores.", Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(requireContext(), "Error grave al compilar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarTablaErrores()
    {
        val dialogView = layoutInflater.inflate(R.layout.dialog_errores, null)
        val tablaErrores = dialogView.findViewById<TableLayout>(R.id.tablaErrores)
        for (error in listaErroresActuales)
        {
            val fila = crearFilaError(error)
            tablaErrores.addView(fila)
        }
        AlertDialog.Builder(requireContext()).setView(dialogView).setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun crearFilaError(error: ErrorCompi): TableRow
    {
        val fila = TableRow(requireContext())
        fila.setPadding(0, 16, 0, 16)
        val tvTipo = TextView(requireContext()).apply{
            text = error.tipo
            setPadding(0, 0, 16, 0)
        }
        val tvDesc = TextView(requireContext()).apply{
            text = error.descripcion
            setPadding(0, 0, 16, 0)
            maxWidth = 400
        }
        val tvLinea = TextView(requireContext()).apply{
            text = error.linea.toString()
            setPadding(0, 0, 16, 0)
        }
        val tvColumna = TextView(requireContext()).apply{
            text = error.columna.toString()
        }
        fila.addView(tvTipo)
        fila.addView(tvDesc)
        fila.addView(tvLinea)
        fila.addView(tvColumna)

        return fila
    }

    private fun guardarArchivoPKM(entorno: Entorno, nombreArchivo: String, autor: String, descripcion: String)
    {
        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val hora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val metadatos = """
            ###
            ###
            Author: $autor
            Fecha: $fecha
            Hora: $hora
            Description: $descripcion
            Total de Secciones: ${entorno.totalSecciones}
            Total de Preguntas: ${entorno.totalPreguntas}
            Abiertas: ${entorno.abiertas}
            Desplegables: ${entorno.desplegables}
            Selección: ${entorno.seleccion}
            Múltiples: ${entorno.multiples}
            
        """.trimIndent()
        val contenidoFinal = metadatos + "\n" + entorno.pkmBuilder.toString()
        try
        {
            val carpetaDestino = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(carpetaDestino, nombreArchivo)
            file.writeText(contenidoFinal)
            Toast.makeText(requireContext(), " Guardado en Descargas:\n$nombreArchivo", Toast.LENGTH_LONG).show()
            println(" Archivo .PKM Generado \n$contenidoFinal\n ")
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al guardar .PKM", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoGuardarPKM(entorno: Entorno)
    {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 16)
        val inputNombreArchivo = EditText(requireContext())
        inputNombreArchivo.hint = "Nombre del archivo .PKM"
        val inputAutor = EditText(requireContext())
        inputAutor.hint = "Nombre del Autor"
        val inputDesc = EditText(requireContext())
        inputDesc.hint = "Descripción del formulario"
        layout.addView(inputNombreArchivo)
        layout.addView(inputAutor)
        layout.addView(inputDesc)
        AlertDialog.Builder(requireContext()).setTitle("Guardar Archivo .PKM").setMessage("Ingresa los metadatos del formulario:").setView(layout).setPositiveButton("Guardar")
        { _, _ ->
            val nombreIngresado = inputNombreArchivo.text.toString().trim()
            val nombreDefinitivo = if (nombreIngresado.isNotBlank())
            {
                if (nombreIngresado.endsWith(".pkm")) nombreIngresado else "$nombreIngresado.pkm"
            }
            else
            {
                val fechaHoraArchivo = java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", java.util.Locale.getDefault()).format(java.util.Date())
                "pokemon_form_$fechaHoraArchivo.pkm"
            }
            val autor = inputAutor.text.toString().ifBlank{ "Autor Desconocido" }
            val descripcion = inputDesc.text.toString().ifBlank{ "Sin descripción" }
            guardarArchivoPKM(entorno, nombreDefinitivo, autor, descripcion)
        }.setNegativeButton("Cancelar", null).show()
    }

    private fun mostrarSelectorDeColor(entradaCodigo: EditText)
    {
        val colores = arrayOf(
            "#F80000", "#C6DA52", "#3F48F4", "#FFFF00",
            "#8800FF", "#FFA500", "#DDF4F5", "#CF3476",
            "#000000", "#FFFFFF", "#808080", "#FFB5C0"
        )
        val nombresBase = mapOf(
            "#F80000" to "RED", "#C6DA52" to "GREEN", "#3F48F4" to "BLUE", "#FFFF00" to "YELLOW",
            "#8800FF" to "PURPLE", "#DDF4F5" to "SKY", "#000000" to "BLACK", "#FFFFFF" to "WHITE"
        )
        val contenedor = LinearLayout(requireContext()).apply{
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
        val gridLayout = GridLayout(requireContext()).apply{
            columnCount = 4
            rowCount = 3
        }
        val dialog = AlertDialog.Builder(requireContext()).setTitle("Elige un Color").setView(contenedor).setNegativeButton("Cancelar", null).create()
        for (colorHex in colores)
        {
            val colorView = View(requireContext()).apply{
                val size = 130
                val params = GridLayout.LayoutParams().apply{
                    width = size
                    height = size
                    setMargins(16, 16, 16, 16)
                }
                layoutParams = params
                val border = android.graphics.drawable.GradientDrawable()
                val hexLimpio = colorHex.trim()
                var colorInt = android.graphics.Color.GRAY
                try
                {
                    colorInt = android.graphics.Color.parseColor(hexLimpio)
                    border.setColor(colorInt)
                }
                catch (e: Exception)
                {
                    border.setColor(colorInt)
                }
                border.setStroke(3, android.graphics.Color.DKGRAY)
                border.cornerRadius = 24f
                background = border
                setOnClickListener{
                    val r = android.graphics.Color.red(colorInt)
                    val g = android.graphics.Color.green(colorInt)
                    val b = android.graphics.Color.blue(colorInt)
                    val hsl = FloatArray(3)
                    androidx.core.graphics.ColorUtils.colorToHSL(colorInt, hsl)
                    val h = hsl[0].toInt()
                    val s = (hsl[1] * 100).toInt()
                    val l = (hsl[2] * 100).toInt()
                    val opcionesList = mutableListOf("Hexadecimal: $hexLimpio", "RGB: ($r,$g,$b)", "HSL: <$h,$s,$l>")
                    if (nombresBase.containsKey(hexLimpio))
                    {
                        opcionesList.add("Predefinido: ${nombresBase[hexLimpio]}")
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle("¿En qué formato deseas insertarlo?").setItems(opcionesList.toTypedArray())
                        { _, which ->
                            val formatoElegido = opcionesList[which]
                            val valorAInsertar = formatoElegido.split(": ")[1]
                            val cursor = entradaCodigo.selectionStart
                            if (cursor >= 0)
                            {
                                entradaCodigo.text.insert(cursor, valorAInsertar)
                            }
                            else
                            {
                                entradaCodigo.append(valorAInsertar)
                            }
                            dialog.dismiss()
                        }.show()
                }
            }
            gridLayout.addView(colorView)
        }
        contenedor.addView(gridLayout)
        dialog.show()
    }
}