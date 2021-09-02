package uabc.axel.ornelas.prctica03_imagenes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import uabc.axel.ornelas.prctica03_imagenes.databinding.ActivityColoresBinding
import java.lang.Exception


/**
 * Se modifica los valores de los colores de la imagen y se guarda
 *
 * @author Axel L Ornelas M
 * @version 02.09.2021
 */
class Colores : AppCompatActivity() {
    //Se utiliza para no crear variables con findViewById
    private lateinit var binding: ActivityColoresBinding
    //Sirve para comprobar el permiso de la camara
    companion object {
        private const val WRITE_EXTERN_PERMISSION_CODE = 1
    }
    //Se ejecuta despues de lanzar el intent para guardar el archivo

    @RequiresApi(Build.VERSION_CODES.O)
    private val resultado =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { datos ->
            //Debe terminarse correctamente el intent para guardar el archivo
            if (datos.resultCode == RESULT_OK)
                //Deben de ser diferentes de nulo
                if (datos.data != null && datos.data != null)
                    escribirArchivo(datos.data!!.data!!)
                else
                    Toast.makeText(this, "Error en guardar archivo", Toast.LENGTH_SHORT).show()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Se obtiene la imagen del intent
        val imagen = intent.getParcelableExtra<Bitmap>("foto")!!
        binding.imagen.setImageBitmap(imagen)

        //Cada vez que cambia el color se ejecuta
        binding.azul.addOnChangeListener { _, _, _ ->
            cambiarColor()
        }
        binding.verde.addOnChangeListener { _, _, _ ->
            cambiarColor()
        }
        binding.rojo.addOnChangeListener { _, _, _ ->
            cambiarColor()
        }
        //Si se presiona el boton de guardar, se preguntan los permisos y si los tiene se guarda
        binding.guardar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                guardarImagenes()
            } else {
                pedirPermiso()
            }

        }
    }


    /**
     * Se encarga de guardar el archivo de imagen con el Outpustream
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun escribirArchivo(uri: Uri) {
        //Se obtiene el bitmap de la imagen
        val bitmap: Bitmap = binding.imagen.drawToBitmap()
        //Se ingresa a un canvas la imagen
        val canvas = Canvas(bitmap)
        //Se obtienen los colores
        val rojo: Float = binding.rojo.value
        val verde: Float = binding.verde.value
        val azul: Float = binding.azul.value
        //Se crea el color
        val color: Int = Color.argb(0.5f, rojo, verde, azul)
        //Se crea el outputstream para poder grabar el archivo
        val outputStream = contentResolver.openOutputStream(uri)
        //Se pinta el color de la imagen
        canvas.drawColor(color, PorterDuff.Mode.ADD)
        //Se comprime en un png y se graba
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        try {
            //Actualiza y cierra el flujo
            outputStream!!.flush()
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(this, "Se ha guardado la imagen", Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * Cambia el color de la miniatura del imageview
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cambiarColor() {
        //Se obtienen los colores
        val rojo: Float = binding.rojo.value
        val verde: Float = binding.verde.value
        val azul: Float = binding.azul.value
        //Se pinta el color de la imagen
        val color: Int = Color.argb(0.5f, rojo, verde, azul)
        //Se pone el color en la miniatura
        binding.imagen.setColorFilter(color, PorterDuff.Mode.ADD)
    }

    /**
     * Pide los permisos para escribir y poder guardar el archivo
     */
    private fun pedirPermiso() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso necesario para camara")
                .setMessage("Este permiso es necesario para tomar una foto")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), WRITE_EXTERN_PERMISSION_CODE
                    )
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .create().show()
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERN_PERMISSION_CODE
        )

    }

    /**
     * Se ejecuta cuando se presiona el boton de guardar
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun guardarImagenes() {
        //Se crea un intent el cual abre el explorador de archivos para seleccionar la ruta
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
            putExtra(Intent.EXTRA_TITLE, "imagen.png")
        }
        resultado.launch(intent)
    }

    /**
     * Se ejecuta cuando se solicitan los permisos
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERN_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso Concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}