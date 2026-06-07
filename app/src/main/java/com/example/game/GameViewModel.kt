package com.example.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.player.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState.START)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player.asStateFlow()
    
    private val _platforms = MutableStateFlow(listOf(
        Platform(100f, 700f, 800f, 40f),
        Platform(300f, 550f, 200f, 20f),
        Platform(600f, 400f, 200f, 20f)
    ))
    val platforms: StateFlow<List<Platform>> = _platforms.asStateFlow()

    private var moveLeft = false
    private var moveRight = false

    fun startGame() {
        _gameState.value = GameState.PLAYING
        _player.value = PlayerState(x = 400f, y = 600f)
        moveLeft = false
        moveRight = false
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (_gameState.value == GameState.PLAYING) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now

                updatePhysics(dt, now)
                updateAttacks()
                delay(16)
            }
        }
    }

    fun setMoveLeft(active: Boolean) { moveLeft = active }
    fun setMoveRight(active: Boolean) { moveRight = active }

    fun jump() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        if (p.isGrounded) {
            _player.update { it.copy(vy = PlayerConfig.JUMP_POWER, isGrounded = false) }
        }
    }

    fun dash() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        val now = System.currentTimeMillis()
        if (!p.isDashing && now - p.lastDashTime > PlayerConfig.DASH_COOLDOWN_MS) {
            _player.update { 
                it.copy(
                    isDashing = true,
                    lastDashTime = now,
                    dashEndTime = now + PlayerConfig.DASH_DURATION_MS
                )
            }
        }
    }

    fun attack() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        if (!p.isAttacking) {
            _player.update { it.copy(isAttacking = true, attackProgress = 0f) }
        }
    }

    fun switchWeapon() {
        _player.update { p ->
            val allWeapons = PlayerWeapons.ALL_WEAPONS
            val currentIndex = allWeapons.indexOfFirst { it.name == p.activeWeapon.name }
            val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % allWeapons.size
            p.copy(activeWeapon = allWeapons[nextIndex])
        }
    }

    fun switchSkin() {
        _player.update { p ->
            val allSkins = PlayerAppearance.ALL_SKINS
            val currentIndex = allSkins.indexOfFirst { it.id == p.activeSkin.id }
            val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % allSkins.size
            p.copy(activeSkin = allSkins[nextIndex])
        }
    }

    private fun updatePhysics(dt: Float, now: Long) {
        _player.update { p ->
            var newVx = 0f
            var newDir = p.direction

            if (p.isDashing) {
                if (now > p.dashEndTime) {
                    return@update p.copy(isDashing = false)
                }
                newVx = if (p.direction == Direction.RIGHT) p.speed * PlayerConfig.DASH_SPEED_MULTIPLIER 
                        else -p.speed * PlayerConfig.DASH_SPEED_MULTIPLIER
            } else {
                if (moveLeft) {
                    newVx = -p.speed
                    newDir = Direction.LEFT
                } else if (moveRight) {
                    newVx = p.speed
                    newDir = Direction.RIGHT
                }
            }

            var newUy = p.vy + PlayerConfig.GRAVITY * dt
            var newY = p.y + newUy * dt
            var newX = p.x + newVx * dt
            var grounded = false

            // Basic floor to prevent falling forever
            if (newY > 800f) {
                newY = 800f
                newUy = 0f
                grounded = true
            }

            // Simple platform collision
            for (plat in _platforms.value) {
                if (newX > plat.x && newX < plat.x + plat.width) {
                    if (p.y <= plat.y && newY > plat.y) {
                        newY = plat.y
                        newUy = 0f
                        grounded = true
                    }
                }
            }


            p.copy(
                x = newX,
                y = newY,
                vx = newVx,
                vy = newUy,
                isGrounded = grounded,
                direction = newDir
            )
        }
    }

    private fun updateAttacks() {
        _player.update { p ->
            if (p.isAttacking) {
                // Fixed 60fps approximation steps
                val newProgress = p.attackProgress + (0.016f / (PlayerConfig.ATTACK_DURATION_MS / 1000f))
                if (newProgress >= 1f) {
                    p.copy(isAttacking = false, attackProgress = 0f)
                } else {
                    p.copy(attackProgress = newProgress)
                }
            } else {
                p
            }
        }
    }
}
