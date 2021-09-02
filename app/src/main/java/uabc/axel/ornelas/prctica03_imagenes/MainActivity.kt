package uabc.axel.ornelas.prctica03_imagenes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import uabc.axel.ornelas.prctica03_imagenes.databinding.ActivityMainBinding

/**
 * Actividad principal donde se encuentra el boton para tomar la foto
 *
 * @author Axel L Ornelas M
 * @version 02.09.2021
 */
class MainActivity : AppCompatActivity() {

    //Sirve para comprobar el permiso de la camara
    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
    }
    //Se utiliza para no crear variables con findViewById
    private lateinit var binding: ActivityMainBinding
    //Se ejecuta despues de que la actividad se acabe
    private val resultado =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { datos ->
            //Verifica si se acabo correctamente la actividad
            if (datos.resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "No se tomo la foto", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            //Guarda la imagen como un bitmap
            val thumbNail: Bitmap = datos.data?.extras?.get("data") as Bitmap
            //Se crea el intent para iniciar la otra actividad
            val intent = Intent(applicationContext, Colores::class.java)
            //Se pone como extra la foto
            intent.putExtra("foto", thumbNail)
            //Se inicia la actividad
            startActivity(intent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Se inicializa el objeto binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Preguntara por los permisos, si en el caso ya los tenía inicia la camara
        binding.btnTomar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            )
                iniciarCamara()
            else
                pedirPermiso()
        }

    }

    /**
     * Inicia la camara con el intent y usa la variable con el callback para obtener la foto
     */
    private fun iniciarCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultado.launch(intent)
    }

    /**
     * Pide los permisos de la camara para poder tomar la foto
     */
    private fun pedirPermiso() {
        //Si el permiso no esta activado se muestra un alert dialog y se pregunta por el permiso
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso necesario para camara")
                .setMessage("Este permiso es necesario para tomar una foto")
                .setPositiveButton("OK") { _, _ ->
                    //Pregunta el permiso
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.CAMERA
                        ), CAMERA_PERMISSION_CODE
                    )
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .create().show()
            return
        }
        //Si no lo había negado antes simplemente los pregunta
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
        )

    }

    /**
     * Si el permiso fue exitoso le muestra el mensaje, se realiza despues de preguntar el permiso
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permiso Concedido", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show()
        }
    }
}