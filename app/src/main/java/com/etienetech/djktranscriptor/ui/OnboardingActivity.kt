package com.etienetech.djktranscriptor.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.etienetech.djktranscriptor.R
import com.etienetech.djktranscriptor.util.PrefsManager
import com.google.android.material.button.MaterialButton

/**
 * Onboarding screens introducing the app and requesting permissions.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: ViewGroup
    private lateinit var btnPrimary: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var prefs: PrefsManager

    private val pages = listOf(
        OnboardingPage(
            R.drawable.ic_mic,
            "Bienvenue sur DJK Transcriptor",
            "Contrôlez votre téléphone avec votre voix.\nParlez naturellement, DJK exécute vos commandes."
        ),
        OnboardingPage(
            R.drawable.ic_mic,
            "Reconnaissance Vocale Avancée",
            "DJK comprend vos commandes en français et en anglais.\nOuvrir des apps, jouer de la musique, passer des appels..."
        ),
        OnboardingPage(
            R.drawable.ic_settings,
            "Permissions Requises",
            "L'accès au microphone et au stockage est nécessaire pour le fonctionnement optimal de l'app."
        )
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // All permissions handled, proceed to main
        completeOnboarding()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        prefs = PrefsManager(this)

        viewPager = findViewById(R.id.viewPager)
        dotsIndicator = findViewById(R.id.dotsIndicator)
        btnPrimary = findViewById(R.id.btnPrimary)
        btnSkip = findViewById(R.id.btnSkip)

        viewPager.adapter = OnboardingAdapter(pages)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButtons(position)
            }
        })

        setupDots()
        updateButtons(0)

        btnPrimary.setOnClickListener {
            val current = viewPager.currentItem
            if (current < pages.size - 1) {
                viewPager.currentItem = current + 1
            } else {
                requestPermissions()
            }
        }

        btnSkip.setOnClickListener {
            requestPermissions()
        }
    }

    private fun setupDots() {
        dotsIndicator.removeAllViews()
        for (i in pages.indices) {
            val dot = View(this).apply {
                val size = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = if (i > 0) resources.getDimensionPixelSize(R.dimen.spacing_sm) else 0
                }
                background = getDrawable(R.drawable.bg_chip)
                alpha = if (i == 0) 1f else 0.3f
            }
            dotsIndicator.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            dotsIndicator.getChildAt(i).alpha = if (i == position) 1f else 0.3f
        }
    }

    private fun updateButtons(position: Int) {
        if (position == pages.size - 1) {
            btnPrimary.text = getString(R.string.onboarding_get_started)
            btnSkip.visibility = View.GONE
        } else {
            btnPrimary.text = getString(R.string.onboarding_next)
            btnSkip.visibility = View.VISIBLE
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            // Android 13+ granular permissions
            val mediaPermissions = listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            mediaPermissions.forEach { perm ->
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(perm)
                }
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        prefs.setOnboardingCompleted()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }
}

/**
 * Data class for onboarding page content.
 */
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

/**
 * ViewPager adapter for onboarding pages.
 */
class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivIllustration)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.image.setImageResource(page.imageRes)
        holder.title.text = page.title
        holder.description.text = page.description
    }

    override fun getItemCount() = pages.size
}
