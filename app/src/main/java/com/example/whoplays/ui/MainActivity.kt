package com.example.whoplays.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.whoplays.R
import com.example.whoplays.ui.adapters.GameAdapter
import com.example.whoplays.ui.adapters.ParticipantAdapter
import com.example.whoplays.viewmodels.FeedViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val feedViewModel: FeedViewModel by viewModels()
    private lateinit var gameAdapter: GameAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var llEmptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupToolbar("", showLogo = true)

        val currentUserId = auth.currentUser?.uid ?: ""
        if (currentUserId.isNotEmpty()) {
            requestNotificationPermission()
            fetchCurrentLocation()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    lifecycleScope.launch {
                        userRepository.updateFcmToken(currentUserId, token)
                    }
                }
            }
        }

        val rvGames = findViewById<RecyclerView>(R.id.rvGames)
        val fabAddGame = findViewById<FloatingActionButton>(R.id.fabAddGame)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        llEmptyState = findViewById(R.id.llEmptyState)

        rvGames.layoutManager = LinearLayoutManager(this)
        gameAdapter = GameAdapter(
            emptyList(),
            currentUserId,
            onJoinClicked = { clickedGame ->
                feedViewModel.joinGame(clickedGame.gameId, currentUserId)
            },
            onLeaveClicked = { clickedGame ->
                showLeaveConfirmation(clickedGame, currentUserId)
            },
            onParticipantsClicked = { clickedGame ->
                showParticipantsDialog(clickedGame)
            },
            onChatClicked = { clickedGame ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("EXTRA_GAME_ID", clickedGame.gameId)
                    putExtra("EXTRA_SPORT_TYPE", clickedGame.sportType)
                }
                startActivity(intent)
            }
        )
        rvGames.adapter = gameAdapter

        feedViewModel.games.observe(this) { gamesList ->
            gameAdapter.updateData(gamesList)
            swipeRefresh.isRefreshing = false
            llEmptyState.visibility = if (gamesList.isEmpty()) View.VISIBLE else View.GONE
        }

        swipeRefresh.setOnRefreshListener {
            feedViewModel.fetchGames()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                feedViewModel.filterGames(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fabAddGame.setOnClickListener {
            val intent = Intent(this, CreateGameActivity::class.java)
            startActivity(intent)
        }

        fabAddGame.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Generate Dummy Data")
                .setMessage("Do you want to generate 30 dummy games for the next month?")
                .setPositiveButton("Generate") { _, _ ->
                    feedViewModel.generateDummyData()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    private fun requestNotificationPermission() {
        val permissions = mutableListOf<String>()
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != 
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
        }
    }

    private fun fetchCurrentLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    feedViewModel.setUserLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun showLeaveConfirmation(game: com.example.whoplays.models.Game, userId: String) {
        AlertDialog.Builder(this)
            .setTitle("Leave Game")
            .setMessage("Are you sure you want to leave this ${game.sportType} game?")
            .setPositiveButton("Yes") { _, _ ->
                feedViewModel.leaveGame(game.gameId, userId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        feedViewModel.fetchGames()
        invalidateOptionsMenu()
    }

    private fun showParticipantsDialog(game: com.example.whoplays.models.Game) {
        lifecycleScope.launch {
            val participants = feedViewModel.getParticipants(game.participantIds)
            
            val recyclerView = RecyclerView(this@MainActivity).apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = ParticipantAdapter(participants)
                setPadding(0, 20, 0, 20)
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Participants (${participants.size})")
                .setView(recyclerView)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
