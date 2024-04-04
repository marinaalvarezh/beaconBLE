package com.example.beaconble

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

open class PermissionsActivity: AppCompatActivity() {
    //comprueba el resultado de todos los permisos mediante una Actividad y un Contract

    val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Es un mapa Map<String, Boolean> donde String=Key permiso y Boolean=resultado
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")

                } else {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")
                    // Mostrar mensaje que se ha rechazado el permiso
                }
            }
        }
    companion object {
        //Al ser Log.d no afectan al usuario solo informativo del Debug
        const val TAG = "PermissionsActivity"
    }
}

class PermissionsHelper(val context: Context) {
    // se obtiene info de si un permiso concreto está aceptado, si es la primera vez que se pide un permiso y los permisos que hay que pedir segun SDK
    // Estas son las permissionString:
    // Manifest.permission.ACCESS_BACKGROUND_LOCATION
    // Manifest.permission.ACCESS_FINE_LOCATION
    // Manifest.permission.BLUETOOTH_CONNECT
    // Manifest.permission.BLUETOOTH_SCAN
    fun isPermissionGranted(permissionString: String): Boolean {
        //se comprueba con el Context un permiso específico
        return (ContextCompat.checkSelfPermission(context, permissionString) == PackageManager.PERMISSION_GRANTED)
    }
    fun setFirstTimeAskingPermission(permissionString: String, isFirstTime: Boolean) {
        //Se almacena en XML dentro de Android un dato clave-valor (Shared Preferences), en este caso si es la primera vez que se pide un permiso
        val sharedPreference = context.getSharedPreferences("com.example.beaconble",
            Context.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(permissionString, isFirstTime).apply()
        //se guarda el valor en el XML
    }

    fun isFirstTimeAskingPermission(permissionString: String): Boolean {
        //Se busca en el XML si es la primera vez que se pide el permiso
        val sharedPreference = context.getSharedPreferences("com.example.beaconble",
            AppCompatActivity.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(
            permissionString,
            true
        )
        //getBoolen (string permiso, valor que se devuelve si no existe valor para la string)
    }
    fun beaconScanPermissionGroupsNeeded(backgroundAccessRequested: Boolean = false): List<Array<String>> {
        val permissions = ArrayList<Array<String>>()
        //Lista de Arrays cada uno tiene un grupo de permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Version Android M (Marshmallow, Android 6.0). Se necesita ACCES_FINE_LOCATION
            permissions.add(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Version Android 10. El permiso anterior + ACCESS_BACKGROUND_LOCATION si es necesario
            if (backgroundAccessRequested) {
                permissions.add(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Version Android 12. Los anteriores + BLUETOOTH_SCAN
            // BLUETOOTH_CONNECT si se quiere informacion adicional
            permissions.add(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Version 13. POST_NOTIFICATIONS si utiliza un servicio en primer plano
            permissions.add(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        return permissions
    }

}



open class BeaconScanPermissionsActivity: PermissionsActivity()  {
    //hereda de la Activity que comprueba el resultado de todos los permisos
    lateinit var layout: LinearLayout
    lateinit var permissionGroups: List<Array<String>>
    lateinit var continueButton: Button
    // scale tiene un getter que obtiene la densidad de pantalla del dispositivo para adapatar la IU
    var scale: Float = 1.0f
        get() {
            return this.getResources().getDisplayMetrics().density
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        //hay que ejecutar el codigo de la AppCompatActivity padre antes que el de esta clase
        super.onCreate(savedInstanceState)

        layout = LinearLayout(this)
        layout.setPadding(dp(20))
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.WHITE)
        layout.orientation = LinearLayout.VERTICAL
        //se utiliza intent para recibir datos enviados a la Activity
        //se define la IU de la Activity de los Permisos
        val backgroundAccessRequested = intent.getBooleanExtra("backgroundAccessRequested", true)
        val title = intent.getStringExtra("title") ?: "Permissions Needed"
        val message = intent.getStringExtra("message") ?: "In order to scan for beacons, this app requrires the following permissions from the operating system.  Please tap each button to grant each required permission."
        val continueButtonTitle = intent.getStringExtra("continueButtonTitle") ?: "Continue"
        val permissionButtonTitles = intent.getBundleExtra("permissionBundleTitles") ?: getDefaultPermissionTitlesBundle()

        //hace la lista de permisos necesarios en funcion de la SDK
        permissionGroups = PermissionsHelper(this).beaconScanPermissionGroupsNeeded(backgroundAccessRequested)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(dp(0), dp(10), dp(0), dp(10))


        val titleView = TextView(this)
        titleView.setGravity(Gravity.CENTER)
        titleView.textSize = dp(10).toFloat()
        titleView.text = title
        titleView.layoutParams = params

        layout.addView(titleView)
        val messageView = TextView(this)
        messageView.text = message
        messageView.setGravity(Gravity.CENTER)
        messageView.textSize = dp(5).toFloat()
        messageView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        messageView.layoutParams = params
        layout.addView(messageView)

        //bucle para definir cada boton, el bundle se utiliza para pasar datos y guardar un estado
        var index = 0
        for (permissionGroup in permissionGroups) {
            val button = Button(this)
            val buttonTitle = permissionButtonTitles.getString(permissionGroup.first())
            button.id = index
            button.text = buttonTitle
            button.layoutParams = params
            button.setOnClickListener(buttonClickListener)
            layout.addView(button)
            index += 1
        }

        continueButton = Button(this)
        continueButton.text = continueButtonTitle
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            this.finish()
        }
        continueButton.layoutParams = params
        layout.addView(continueButton)

        setContentView(layout)
    }

    fun dp(value: Int): Int {
        return (value * scale + 0.5f).toInt()
    }

    //al pulsar lanza el prompt con el permiso correspondiente
    val buttonClickListener = View.OnClickListener { button ->
        val permissionsGroup = permissionGroups.get(button.id)
        promptForPermissions(permissionsGroup)
    }

    @SuppressLint("InlinedApi")
    fun getDefaultPermissionTitlesBundle(): Bundle {
        //Define la palabra de los botones que aparecera en la Activity de Permisos
        val bundle = Bundle()
        bundle.putString(Manifest.permission.ACCESS_FINE_LOCATION, "Location")
        bundle.putString(Manifest.permission.ACCESS_BACKGROUND_LOCATION, "Background Location")
        bundle.putString(Manifest.permission.BLUETOOTH_SCAN, "Bluetooth")
        bundle.putString(Manifest.permission.POST_NOTIFICATIONS, "Notifications")
        return bundle
    }


    fun allPermissionGroupsGranted(): Boolean {
        //pregunta si todos los permisos de todos los grupos han sido aceptados
        for (permissionsGroup in permissionGroups) {
            if (!allPermissionsGranted(permissionsGroup)) {
                return false
            }
        }
        return true
    }

    fun setButtonColors() {
        var index = 0
        //define el color de los botones en funcion de si se han aceptado
        for (permissionsGroup in this.permissionGroups) {
            val button = findViewById<Button>(index)
            if (allPermissionsGranted(permissionsGroup)) {
                // si todos los permisos de ese grupo se han aceptado
                button.setBackgroundColor(Color.parseColor("#448844"))
            }
            else {
                button.setBackgroundColor(Color.RED)
            }
            index += 1
        }
    }
    override fun onResume() {
        super.onResume()
        //llama a la Activity padre, define los botones
        setButtonColors()
        if (allPermissionGroupsGranted()) {
            // si se han aceptado todos los permisos de todos los grupos deja continuar
            continueButton.isEnabled = true
        }
    }

    fun promptForPermissions(permissionsGroup: Array<String>) {
        // si hay algun grupo de permisos no concedido se ejecuta
        if (!allPermissionsGranted(permissionsGroup)) {
            val firstPermission = permissionsGroup.first()

            var showRationale = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showRationale = shouldShowRequestPermissionRationale(firstPermission)
            }
            if (showRationale ||  PermissionsHelper(this).isFirstTimeAskingPermission(firstPermission)) {
                PermissionsHelper(this).setFirstTimeAskingPermission(firstPermission, false)
                requestPermissionsLauncher.launch(permissionsGroup)
            }
            else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Can't request permission")
                builder.setMessage("This permission has been previously denied to this app.  In order to grant it now, you must go to Android Settings to enable this permission.")
                builder.setPositiveButton("OK", null)
                builder.show()
            }
        }
    }
    fun allPermissionsGranted(permissionsGroup: Array<String>): Boolean {
        //pregunta si un grupo de permisos ha sido aceptado
        val permissionsHelper = PermissionsHelper(this)
        for (permission in permissionsGroup) {
            if (!permissionsHelper.isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }

    companion object {
        const val TAG = "BeaconScanPermissionActivity"

        // es la función principial que se llama desde la MainActivity
        fun allPermissionsGranted(context: Context, backgroundAccessRequested: Boolean): Boolean {
            val permissionsHelper = PermissionsHelper(context)
            val permissionsGroups = permissionsHelper.beaconScanPermissionGroupsNeeded(backgroundAccessRequested)
            for (permissionsGroup in permissionsGroups) {
                for (permission in permissionsGroup) {
                    if (!permissionsHelper.isPermissionGranted(permission)) {
                        return false
                    }
                }
            }
            return true
        }
    }
}