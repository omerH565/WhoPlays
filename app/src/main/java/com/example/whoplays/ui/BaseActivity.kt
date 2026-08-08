package com.example.whoplays.ui

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.whoplays.R
import com.example.whoplays.repositories.UserRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity() {

    protected val userRepository = UserRepository()
    protected val auth = FirebaseAuth.getInstance()
    protected var toolbar: MaterialToolbar? = null

    fun setupToolbar(title: String, showBackButton: Boolean = false, showLogo: Boolean = false) {
        toolbar = findViewById(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            val brandingLayout = it.findViewById<android.view.View>(R.id.llToolbarBranding)
            
            if (showLogo) {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                brandingLayout?.visibility = android.view.View.VISIBLE
            } else {
                supportActionBar?.setDisplayShowTitleEnabled(true)
                supportActionBar?.title = title
                brandingLayout?.visibility = android.view.View.GONE
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        val profileItem = menu?.findItem(R.id.action_profile)
        
        if (auth.currentUser == null || this is LoginActivity || this is RegisterActivity || this is OnboardingActivity) {
            profileItem?.isVisible = false
            return true
        }

        val actionView = profileItem?.actionView
        val profileIcon = actionView?.findViewById<ImageView>(R.id.ivToolbarProfile)
        
        actionView?.setOnClickListener {
            if (this !is ProfileActivity) {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid != null && profileIcon != null) {
            profileIcon.setImageResource(android.R.drawable.ic_menu_gallery)
            
            CoroutineScope(Dispatchers.IO).launch {
                val user = userRepository.getUser(uid)
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        val imageUrl = user?.profileImageUrl
                        Glide.with(this@BaseActivity)
                            .load(if (!imageUrl.isNullOrEmpty()) imageUrl else android.R.drawable.ic_menu_gallery)
                            .circleCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(profileIcon)
                    }
                }
            }
        }
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
