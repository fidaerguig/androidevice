package fr.isen.fidae.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class ConnexionActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val device = intent.getParcelableExtra<BluetoothDevice>("deviceName")

        setContent {
            val context = LocalContext.current
            var led1State by remember { mutableStateOf(false) }
            var led2State by remember { mutableStateOf(false) }
            var led3State by remember { mutableStateOf(false) }
            var button1Count by remember { mutableStateOf(0) }
            var button3Count by remember { mutableStateOf(0) }
            var isConnected by remember { mutableStateOf(false) } // Etat de la connexion

            // Quand l'appareil est sélectionné
            LaunchedEffect(device) {
                device?.let { bluetoothDevice = it }
            }

            DeviceDetailsScreen(
                deviceName = device?.name ?: "Appareil inconnu",
                led1State = led1State,
                led2State = led2State,
                led3State = led3State,
                button1Count = button1Count,
                button3Count = button3Count,
                onLedToggle = { ledId ->
                    // Mise à jour de l'état des LEDs
                    when (ledId) {
                        1 -> led1State = !led1State
                        2 -> led2State = !led2State
                        3 -> led3State = !led3State
                    }
                    // Envoi de la commande LED au périphérique STM32
                    sendLedCommand(ledId, if (ledId == 1) led1State else if (ledId == 2) led2State else led3State)
                },
                onConnectClick = {
                    bluetoothDevice?.let { device ->
                        if (!isConnected) {
                            connectToDevice(device, context, onButtonUpdate = { btn1, btn3 ->
                                button1Count = btn1 // Mise à jour du compteur du bouton 1
                                button3Count = btn3 // Mise à jour du compteur du bouton 3
                            })
                            isConnected = true
                        }
                    }
                },
                onDisconnectClick = {
                    bluetoothGatt?.close()
                    isConnected = false
                    Toast.makeText(context, "Déconnexion de l'appareil", Toast.LENGTH_SHORT).show()
                },
                isConnected = isConnected // Etat de la connexion
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice, context: Context, onButtonUpdate: (Int, Int) -> Unit) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Le Bluetooth est désactivé", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothGatt = device.connectGatt(this, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    runOnUiThread {
                        Toast.makeText(context, "Connecté à ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread {
                        Toast.makeText(context, "Déconnecté", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Services et caractéristiques découverts
                    val service = gatt?.services?.getOrNull(2) // On sélectionne le service 3
                    service?.let { s ->
                        val characteristic = s.characteristics.getOrNull(1) // On sélectionne la caractéristique 2
                        characteristic?.let {
                            gatt.setCharacteristicNotification(it, true) // Activer les notifications pour cette caractéristique
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                characteristic?.let {
                    Log.d("Bluetooth", "Changement caractéristique : ${it.uuid}")
                    if (it.value != null && it.value.size >= 2) {
                        val btn1Count = it.value[0].toInt()  // Nombre de clics du bouton 1
                        val btn3Count = it.value[1].toInt()  // Nombre de clics du bouton 3
                        runOnUiThread {
                            onButtonUpdate(btn1Count, btn3Count) // Mise à jour des compteurs
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun sendLedCommand(ledId: Int, state: Boolean) {
        bluetoothGatt?.let { gatt ->
            // Trouver le service correspondant aux LEDs
            val service = gatt.services.getOrNull(2) // Supposez que le service des LEDs est au 2ème index
            service?.let { s ->
                val characteristic = s.characteristics.getOrNull(0) // Supposez que la caractéristique est au 0ème index
                characteristic?.let {
                    val ledCommand = if (state) ledId.toByte() else 0x00 // Envoi de la commande LED
                    it.value = byteArrayOf(ledCommand)
                    gatt.writeCharacteristic(it)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}

@Composable
fun DeviceDetailsScreen(
    deviceName: String,
    led1State: Boolean,
    led2State: Boolean,
    led3State: Boolean,
    button1Count: Int,
    button3Count: Int,
    onLedToggle: (Int) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    isConnected: Boolean
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2196F3))
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Détails de l'appareil", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nom de l'appareil : $deviceName", style = MaterialTheme.typography.bodyLarge)

            // Connexion à l'appareil
            Button(
                onClick = {
                    if (isConnected) {
                        onDisconnectClick()
                    } else {
                        onConnectClick()
                    }
                },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = if (isConnected) "Déconnexion " else "Connexion à l'appareil")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LED Buttons
            LedButton(ledId = 1, isOn = led1State, onLedToggle = onLedToggle)
            LedButton(ledId = 2, isOn = led2State, onLedToggle = onLedToggle)
            LedButton(ledId = 3, isOn = led3State, onLedToggle = onLedToggle)

            Spacer(modifier = Modifier.height(24.dp))

            // Button Counters
            Text(text = "Bouton un  : $button1Count clics")
            Text(text = "Bouton trois : $button3Count clics")
        }
    }
}

@Composable
fun LedButton(ledId: Int, isOn: Boolean, onLedToggle: (Int) -> Unit) {
    Button(
        onClick = { onLedToggle(ledId) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOn) Color.Green else Color.Gray
        )
    ) {
        Text(text = "LED $ledId ${if (isOn) "ON" else "OFF"}")
    }
}