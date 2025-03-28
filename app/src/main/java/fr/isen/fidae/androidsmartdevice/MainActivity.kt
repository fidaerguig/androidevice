package fr.isen.fidae.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.fidae.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                MainScreen { navigateToScanActivity() } // Passe la fonction de navigation en paramètre
            }
        }
    }

    // Fonction qui crée l'Intent pour démarrer ScanActivity
    private fun navigateToScanActivity() {
        val intent = Intent(this, ScanActivity::class.java) // Créer l'intent pour lancer ScanActivity
        startActivity(intent) // Démarrer ScanActivity
    }
}

@Composable
fun MainScreen(onNavigateToScanActivity: () -> Unit) {
    // Corps de la page avec un fond bleu
    Column(
        modifier = Modifier
            .fillMaxSize() // Remplir toute la taille de l'écran
            .background(Color(0xC81EE2E9)) // Définir le fond bleu
            .padding(16.dp), // Ajouter des marges autour
        verticalArrangement = Arrangement.Center, // Centre verticalement
        horizontalAlignment = Alignment.CenterHorizontally // Centre horizontalement
    ) {
        // Titre de l'application
        Text(
            text = "Bienvenue dans votre application Smart Device", // Le titre de la page
            style = MaterialTheme.typography.headlineLarge, // Typographie de taille grande
            color = Color.White // Texte en blanc pour contraster avec le bleu
        )
        Spacer(modifier = Modifier.height(16.dp)) // Ajoute un espace entre le titre et la description

        // Description sous le titre
        Text(
            text = "Cette application gere les appareils bluetooth", // Description sous le titre
            style = MaterialTheme.typography.bodyMedium, // Typographie pour une description
            color = Color.White // Texte en blanc pour contraster avec le bleu
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espace avant l'icône

        // Ajout de l'icône Bluetooth en tant que bouton pour rediriger vers ScanActivity
        IconButton(
            onClick = onNavigateToScanActivity // Appelle la fonction de navigation
        ) {
            Image(
                painter = painterResource(id = R.drawable.bluetooth_icon), // Utilise l'icône Bluetooth
                contentDescription = "Bluetooth Icon", // Description de l'icône
                modifier = Modifier.size(48.dp) // Taille de l'icône
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Espace avant le bouton "Commencer"

        // Ajout d'un bouton "Commencer"
        Button(
            onClick = onNavigateToScanActivity, // L'action qui redirige vers ScanActivity
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray) // Couleur de fond du bouton
        ) {
            Text(text = "Commencer", color = Color.Black) // Texte bleu pour contraster avec le fond blanc
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidSmartDeviceTheme {
        MainScreen(onNavigateToScanActivity = {}) // On passe une fonction vide pour la prévisualisation
    }
}
