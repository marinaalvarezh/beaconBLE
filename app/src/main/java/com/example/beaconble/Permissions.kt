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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat


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
        val sharedPreference = context.getSharedPreferences("org.altbeacon.permisisons",
            AppCompatActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(permissionString,isFirstTime).apply()
        //se guarda el valor en el XML
    }

    fun isFirstTimeAskingPermission(permissionString: String): Boolean {
        val sharedPreference = context.getSharedPreferences(
            "org.altbeacon.permisisons",
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
            permissions.add(arrayOf(Manifest.permission.INTERNET))
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
    lateinit var permissionGroups: List<Array<String>>
    lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        //hay que ejecutar el codigo de la AppCompatActivity padre antes que el de esta clase
        super.onCreate(savedInstanceState)
        permissionGroups = PermissionsHelper(this).beaconScanPermissionGroupsNeeded(intent.getBooleanExtra("backgroundAccessRequested",true))
        setContent{
            PermissionsScreen(permissionsHelper = PermissionsHelper(this))
        }
    }

    @Composable
    fun PermissionsScreen(permissionsHelper: PermissionsHelper){
        val context = LocalContext.current
        val permissionsHelper = PermissionsHelper(context)
        val backgroundAccessRequested= intent.getBooleanExtra("backgroundAccessRequested", true)
        val title = intent.getStringExtra("title") ?: "Permissions Needed"
        val message = intent.getStringExtra("message") ?: "In order to scan for beacons, this app requrires the following permissions from the operating system.  Please tap each button to grant each required permission."
        val permissionButtonTitles = intent.getBundleExtra("permissionBundleTitles") ?: getDefaultPermissionTitlesBundle()

        val permissionGroups = remember {permissionsHelper.beaconScanPermissionGroupsNeeded(backgroundAccessRequested)}
        val permissionStates = remember { mutableStateListOf<Boolean>() }

        LaunchedEffect(key1 = permissionGroups) {
            permissionStates.clear()
            permissionStates.addAll(List(permissionGroups.size) { false })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(text = title, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            permissionGroups.forEach {permissionGroup ->
                val buttonText = permissionButtonTitles.getString(permissionGroup.first())?:"Unknown Permission"
                Button(
                    onClick = {
                        promptForPermissions(permissionGroup)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ){
                    Text(text = buttonText)
                }
            }
        }

    }

//establece una relacion entre el permiso de cada boton y el titulo del boton
    @SuppressLint("InlinedApi")
    fun getDefaultPermissionTitlesBundle(): Bundle {
        //Define la palabra de los botones que aparecera en la Activity de Permisos
        val bundle = Bundle()
        bundle.putString(Manifest.permission.ACCESS_FINE_LOCATION, "Location")
        bundle.putString(Manifest.permission.ACCESS_BACKGROUND_LOCATION, "Background Location")
        bundle.putString(Manifest.permission.BLUETOOTH_SCAN, "Bluetooth")
        bundle.putString(Manifest.permission.POST_NOTIFICATIONS, "Notifications")
        bundle.putString(Manifest.permission.INTERNET, "Internet")
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

    override fun onResume() {
        super.onResume()
        //llama a la Activity padre
        if(::permissionGroups.isInitialized) {
            if (allPermissionGroupsGranted()) {
                // si se han aceptado todos los permisos de todos los grupos deja continuar
                continueButton.isEnabled = true
            }
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
        val permissionsHelper = PermissionsHelper(this)
        for (permission in permissionsGroup) {
            if (!permissionsHelper.isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }

    companion object {
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