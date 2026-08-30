package no.uio.ifi.in2000.dagligvareapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import no.uio.ifi.in2000.dagligvareapp.presentation.navigation.AppNavigation
import no.uio.ifi.in2000.dagligvareapp.ui.theme.DagligvareAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DagligvareAppTheme {
                AppNavigation()
            }
        }
    }
}
