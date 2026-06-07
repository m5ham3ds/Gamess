package com.example.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.api.GeminiApiClient
import com.example.api.TempleRiddle
import com.example.game.enemy.Enemy
import com.example.game.enemy.EnemyType
import com.example.game.player.Direction
import com.example.game.player.PlayerState
import com.example.game.world.GameRegion
import com.example.game.world.WorldConfig
import com.example.game.world.Platform
import com.example.game.world.Hazard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    ORACLE_CONVERSATION,
    GAME_OVER,
    VICTORY,
    CHRONICLES
}

data class Projectile(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float = 6f,
    val isPlayerOwned: Boolean = false
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: androidx.compose.ui.graphics.Color,
    val alpha: Float = 1.0f,
    val size: Float = 4f,
    val life: Int = 30 // frames
)

data class OracleRelic(
    val x: Float,
    val y: Float,
    val radius: Float = 24f,
    val isUsed: Boolean = false
)

class GameViewModel : ViewModel() {

    // --- GAME STATE ---
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // --- WORLD & REGIONS ---
    private val _currentRegion = MutableStateFlow<GameRegion>(WorldConfig.regions[WorldConfig.REGION_ASH]!!)
    val currentRegion: StateFlow<GameRegion> = _currentRegion.asStateFlow()

    val worldWidth: Float get() = _currentRegion.value.width
    val worldHeight: Float get() = _currentRegion.value.height

    // --- CUSTOMIZER / SETTINGS ENGINE ---
    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _hudScale = MutableStateFlow(1.0f)
    val hudScale: StateFlow<Float> = _hudScale.asStateFlow()

    private val _controlButtonScale = MutableStateFlow(1.0f)
    val controlButtonScale: StateFlow<Float> = _controlButtonScale.asStateFlow()

    // --- ENTITIES STATE ---
    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player.asStateFlow()

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies: StateFlow<List<Enemy>> = _enemies.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles: StateFlow<List<Projectile>> = _projectiles.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _relic = MutableStateFlow<OracleRelic?>(null)
    val relic: StateFlow<OracleRelic?> = _relic.asStateFlow()

    private val _isSlashing = MutableStateFlow(false)
    val isSlashing: StateFlow<Boolean> = _isSlashing.asStateFlow()

    // --- ORACLE / RIDDLE STATE (CHOICES-BASED) ---
    private val _oracleQuestion = MutableStateFlow("جاري الاستعلام من عراف المعبد...")
    val oracleQuestion: StateFlow<String> = _oracleQuestion.asStateFlow()

    private val _oracleChoices = MutableStateFlow<List<String>>(emptyList())
    val oracleChoices: StateFlow<List<String>> = _oracleChoices.asStateFlow()

    private val _oracleSelectedIndex = MutableStateFlow(-1)
    val oracleSelectedIndex: StateFlow<Int> = _oracleSelectedIndex.asStateFlow()

    private val _oracleFeedback = MutableStateFlow<String?>(null)
    val oracleFeedback: StateFlow<String?> = _oracleFeedback.asStateFlow()

    private val _isOracleLoading = MutableStateFlow(false)
    val isOracleLoading: StateFlow<Boolean> = _isOracleLoading.asStateFlow()

    private var currentOracleRiddle: TempleRiddle? = null

    // --- CONTROLLER INPUTS ---
    var movingLeft = false
    var movingRight = false
    var movingInteract = false

    private var gameLoopJob: Job? = null
    private var slashTimeoutJob: Job? = null

    init {
        // Prepare initial settings
        resetSettingsToDefault()
    }

    fun startNewGame() {
        _player.value = PlayerState(
            x = 150f,
            y = 500f,
            hp = 100f,
            maxHp = 100f,
            energy = 50f,
            maxEnergy = 100f,
            forgetfulness = 30f,
            level = 1,
            currency = 50,
            score = 0,
            memoryFragments = 0
        )
        _gameState.value = GameState.PLAYING
        loadRegion(WorldConfig.REGION_ASH, enterFromLeft = true)
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                if (_gameState.value == GameState.PLAYING) {
                    updatePhysics()
                }
                delay(16) // roughly 60 fps
            }
        }
    }

    // --- UPGRADES & SETTINGS CUSTOMIZATIONS ---
    fun setSoundVolume(volume: Float) {
        _soundVolume.value = volume
    }

    fun setHudScale(scale: Float) {
        _hudScale.value = scale
    }

    fun setControlButtonScale(scale: Float) {
        _controlButtonScale.value = scale
    }

    fun resetSettingsToDefault() {
        _soundVolume.value = 0.8f
        _hudScale.value = 1.0f
        _controlButtonScale.value = 1.0f
    }

    fun upgradeVitality() {
        val cost = 30
        val p = _player.value
        if (p.currency >= cost) {
            _player.value = p.copy(
                currency = p.currency - cost,
                maxHp = p.maxHp + 20f,
                hp = p.hp + 20f
            )
            spawnUpgradeSparks()
        }
    }

    fun upgradeSoul() {
        val cost = 20
        val p = _player.value
        if (p.currency >= cost) {
            _player.value = p.copy(
                currency = p.currency - cost,
                maxEnergy = p.maxEnergy + 20f,
                energy = p.energy + 20f
            )
            spawnUpgradeSparks()
        }
    }

    private fun spawnUpgradeSparks() {
        val p = _player.value
        val list = _particles.value.toMutableList()
        repeat(25) {
            list.add(
                Particle(
                    x = p.x,
                    y = p.y,
                    vx = (Math.random() * 8 - 4).toFloat(),
                    vy = (Math.random() * -10 - 2).toFloat(),
                    color = BlightGold,
                    size = 5f
                )
            )
        }
        _particles.value = list
    }

    // --- REGION LOAD STACK ---
    fun loadRegion(regionId: String, enterFromLeft: Boolean) {
        val region = WorldConfig.regions[regionId] ?: return
        _currentRegion.value = region

        // Clear active projectils & old particles
        _projectiles.value = emptyList()
        _particles.value = emptyList()

        // Create a central Relic for the Oracle on this map
        _relic.value = OracleRelic(x = region.width / 2f, y = 710f)

        // Respawn all enemies based on region templates
        // We ALWAYS re-instantiate them. So if players left and returned, they respawn!
        val activeEnemies = region.enemyTemplates.map { template ->
            Enemy(
                id = UUID.randomUUID().toString(),
                x = template.x,
                y = template.y,
                hp = template.maxHp,
                maxHp = template.maxHp,
                type = template.type,
                startX = template.x,
                startY = template.y
            )
        }
        _enemies.value = activeEnemies

        // Re-position player inside bounds
        val startX = if (enterFromLeft) region.spawnXLeft else region.spawnXRight
        _player.value = _player.value.copy(
            x = startX,
            y = 300f, // Drop from top gently
            vx = 0f,
            vy = 0f,
            isGrounded = false
        )

        // Spawn entry particles
        val list = mutableListOf<Particle>()
        repeat(20) {
            list.add(
                Particle(
                    x = startX,
                    y = 350f,
                    vx = (Math.random() * 6 - 3).toFloat(),
                    vy = (Math.random() * 6 - 3).toFloat(),
                    color = EchoesBlue
                )
            )
        }
        _particles.value = list
    }

    // --- PHYSICAL ENGINE UPDATE LOOP ---
    private fun updatePhysics() {
        val p = _player.value
        val region = _currentRegion.value

        // Moving factors
        var vx = p.vx
        if (movingLeft) {
            vx = -6f
        } else if (movingRight) {
            vx = 6f
        } else {
            vx *= 0.8f // drag friction
            if (abs(vx) < 0.15f) vx = 0f
        }

        // Apply constant gravity
        var vy = p.vy + 0.65f
        if (vy > 14f) vy = 14f // terminal velocity clamp

        // Update positions
        var nextX = p.x + vx
        var nextY = p.y + vy

        // Collision constraints with boundaries
        if (nextY > region.height + 60f) {
            // Fell out of bounds / Abyss Void
            decreasePlayerHp(25f)
            // Respawn player at nearest spawn node
            nextX = region.spawnXLeft
            nextY = 200f
            vy = 0f
            vx = 0f
        }

        // --- PLATFORM COLLISIONS ---
        var grounded = false
        region.platforms.forEach { platform ->
            // Overlapping on X-axis check
            val playerLeft = nextX - p.radius
            val playerRight = nextX + p.radius
            val platLeft = platform.x
            val platRight = platform.x + platform.width

            if (playerRight > platLeft && playerLeft < platRight) {
                // Moving down check (landing)
                val feetY = p.y + p.radius
                if (feetY <= platform.y && nextY + p.radius >= platform.y) {
                    if (platform.isBouncy) {
                        vy = -18f // mega jump bounce pad!
                        // Spawn bounce particles
                        triggerBounceSparks(nextX, platform.y)
                    } else {
                        nextY = platform.y - p.radius
                        vy = 0f
                        grounded = true
                    }
                }
            }
        }

        // Map transitions (If player is at left boundary or right boundary)
        if (nextX < 40f) {
            if (region.leftNodeRegionId != null) {
                loadRegion(region.leftNodeRegionId, enterFromLeft = false)
                return
            } else {
                nextX = 40f
            }
        } else if (nextX > region.width - 40f) {
            if (region.rightNodeRegionId != null) {
                loadRegion(region.rightNodeRegionId, enterFromLeft = true)
                return
            } else {
                nextX = region.width - 40f
            }
        }

        // --- HAZARD COLLISION ---
        region.hazards.forEach { hazard ->
            val pLeft = nextX - p.radius
            val pRight = nextX + p.radius
            val pTop = nextY - p.radius
            val pBottom = nextY + p.radius

            val hLeft = hazard.x
            val hRight = hazard.x + hazard.width
            val hTop = hazard.y
            val hBottom = hazard.y + hazard.height

            if (pRight > hLeft && pLeft < hRight && pBottom > hTop && pTop < hBottom) {
                // Hazard overlap! Take damage & bounce player
                decreasePlayerHp(hazard.damage)
                vy = -10f
                vx = if (p.direction == Direction.LEFT) 6f else -6f // recoil bounce
                // Spawn warning sparks
                triggerSparks(nextX, nextY, VitalityRed)
            }
        }

        val dir = if (vx < 0) Direction.LEFT else if (vx > 0) Direction.RIGHT else p.direction

        // Update player model
        _player.value = p.copy(
            x = nextX,
            y = nextY,
            vx = vx,
            vy = vy,
            direction = dir,
            isGrounded = grounded
        )

        // --- UPDATE ENEMIES AI & PHYSICS ---
        val curEnemies = _enemies.value.map { enemy ->
            var ex = enemy.x
            var ey = enemy.y
            var evx = enemy.vx
            var evy = enemy.vy
            var edir = enemy.direction

            when (enemy.type) {
                EnemyType.SHADOW_STALKER -> {
                    // Simple left to right patrolling
                    if (edir == Direction.LEFT) {
                        evx = -2.5f
                        if (ex < enemy.startX - enemy.patrolDistance) {
                            edir = Direction.RIGHT
                        }
                    } else {
                        evx = 2.5f
                        if (ex > enemy.startX + enemy.patrolDistance) {
                            edir = Direction.LEFT
                        }
                    }
                    ex += evx
                }
                EnemyType.NEON_SPIDER -> {
                    // Fast scurrying crawls
                    if (edir == Direction.LEFT) {
                        evx = -4.5f
                        if (ex < enemy.startX - enemy.patrolDistance * 1.5f || ex < 100) {
                            edir = Direction.RIGHT
                        }
                    } else {
                        evx = 4.5f
                        if (ex > enemy.startX + enemy.patrolDistance * 1.5f || ex > region.width - 100) {
                            edir = Direction.LEFT
                        }
                    }
                    ex += evx
                }
                EnemyType.ABYSSAL_ORB -> {
                    // Hover in place with slight vertical drift
                    val time = System.currentTimeMillis()
                    ey = enemy.startY + Math.sin(time / 450.0).toFloat() * 15f
                    
                    // Periodic shooting!
                    val cooldown = 2400L
                    if (time - enemy.lastShotTime > cooldown) {
                        val bulletVx = if (p.x < ex) -5f else 5f
                        shootEnemyProjectile(ex, ey, bulletVx, 0f)
                        enemy.copy(lastShotTime = time)
                    }
                }
                EnemyType.CHRONO_SENTINEL -> {
                    // Follows player x loosely, hovers high and fires downwards
                    val diffX = p.x - ex
                    evx = if (diffX > 0) 1.8f else -1.8f
                    ex += evx
                    
                    val time = System.currentTimeMillis()
                    ey = enemy.startY - 40f + Math.sin(time / 300.0).toFloat() * 10f

                    // Periodic homing direct blast downwards
                    val cooldown = 2800L
                    if (time - enemy.lastShotTime > cooldown) {
                        shootEnemyProjectile(ex, ey, 0f, 6f)
                        enemy.copy(lastShotTime = time)
                    }
                }
                EnemyType.VOID_DEVOURER -> {
                    // Patrols and hops periodically using a simple timer
                    val time = System.currentTimeMillis()
                    if (edir == Direction.LEFT) {
                        evx = -3f
                        if (ex < enemy.startX - enemy.patrolDistance) edir = Direction.RIGHT
                    } else {
                        evx = 3f
                        if (ex > enemy.startX + enemy.patrolDistance) edir = Direction.LEFT
                    }
                    ex += evx

                    if (time % 180 == 0L && enemy.vy == 0f) {
                        evy = -8f
                    }
                    evy += 0.4f
                    if (evy > 10f) evy = 10f
                    
                    // Check local ground y (rough estimation resting on ground platforms)
                    val targetFloor = 750f - enemy.radius
                    if (ey + evy >= targetFloor) {
                        ey = targetFloor
                        evy = 0f
                    } else {
                        ey += evy
                    }
                }
            }

            // Check if enemy projectile collide with player
            enemy.copy(
                x = ex,
                y = ey,
                vx = evx,
                vy = evy,
                direction = edir
            )
        }
        _enemies.value = curEnemies

        // --- UPDATE ACTIVE PROJECTILES ---
        val pList = mutableListOf<Projectile>()
        _projectiles.value.forEach { proj ->
            val nx = proj.x + proj.vx
            val ny = proj.y + proj.vy
            
            var keep = true
            // Collide with boundary or screens
            if (nx < 0 || nx > region.width || ny < 0 || ny > region.height) {
                keep = false
            }

            if (keep) {
                if (proj.isPlayerOwned) {
                    // Collide with enemies
                    var hitIndex = -1
                    _enemies.value.forEachIndexed { idx, enemy ->
                        val dx = nx - enemy.x
                        val dy = ny - enemy.y
                        if (dx*dx + dy*dy < (enemy.radius + proj.radius) * (enemy.radius + proj.radius)) {
                            hitIndex = idx
                            keep = false
                        }
                    }
                    if (hitIndex != -1) {
                        damageEnemy(hitIndex, 20f)
                    }
                } else {
                    // Collide with player
                    val dx = nx - p.x
                    val dy = ny - p.y
                    if (dx*dx + dy*dy < (p.radius + proj.radius) * (p.radius + proj.radius)) {
                        decreasePlayerHp(12f)
                        keep = false
                        triggerSparks(p.x, p.y, VitalityRed)
                    }
                }
            }

            if (keep) {
                pList.add(proj.copy(x = nx, y = ny))
            }
        }
        _projectiles.value = pList

        // --- UPDATE PARTICLES LIFE ---
        _particles.value = _particles.value.map { part ->
            part.copy(
                x = part.x + part.vx,
                y = part.y + part.vy,
                alpha = part.alpha * 0.95f,
                life = part.life - 1
            )
        }.filter { it.life > 0 }
    }

    private fun triggerBounceSparks(x: Float, y: Float) {
        val list = _particles.value.toMutableList()
        repeat(12) {
            list.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Math.random() * 6 - 3).toFloat(),
                    vy = (Math.random() * -8 - 2).toFloat(),
                    color = EchoesBlue
                )
            )
        }
        _particles.value = list
    }

    private fun triggerSparks(x: Float, y: Float, color: androidx.compose.ui.graphics.Color) {
        val list = _particles.value.toMutableList()
        repeat(10) {
            list.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Math.random() * 4 - 2).toFloat(),
                    vy = (Math.random() * 4 - 2).toFloat(),
                    color = color
                )
            )
        }
        _particles.value = list
    }

    // --- PLAYER ACTION COMBO SYSTEMS ---
    fun onSlashAttack() {
        if (_isSlashing.value) return
        _isSlashing.value = true

        val p = _player.value
        val range = 90f
        
        // Find if any enemies are within range and directional sight
        _enemies.value.forEachIndexed { idx, enemy ->
            val dist = abs(enemy.x - p.x)
            val isWithinHeightY = abs(enemy.y - p.y) < 50f
            if (dist < range && isWithinHeightY) {
                // Direction sights constraint
                val isLookingAtEnemy = (p.direction == Direction.RIGHT && enemy.x > p.x) ||
                        (p.direction == Direction.LEFT && enemy.x < p.x)
                if (isLookingAtEnemy) {
                    damageEnemy(idx, 25f + (p.level * 4f))
                }
            }
        }

        // Slash timeout
        slashTimeoutJob?.cancel()
        slashTimeoutJob = viewModelScope.launch {
            delay(180)
            _isSlashing.value = false
        }
    }

    fun handleJump() {
        val p = _player.value
        if (p.isGrounded) {
            _player.value = p.copy(vy = -12.5f, isGrounded = false)
            triggerSparks(p.x, p.y + p.radius, OutlineGray)
        }
    }

    fun onDashAction() {
        val p = _player.value
        val cost = 15f
        if (p.energy >= cost) {
            val dashVx = if (p.direction == Direction.LEFT) -18f else 18f
            _player.value = p.copy(
                vx = dashVx,
                energy = p.energy - cost
            )
            triggerSparks(p.x, p.y, EchoesBlue)
        }
    }

    fun useMemoryPower() {
        val p = _player.value
        val cost = 20f
        if (p.energy >= cost && !p.soulShieldActive) {
            _player.value = p.copy(
                energy = p.energy - cost,
                soulShieldActive = true
            )
            triggerSparks(p.x, p.y, Color(0xFFB470E0))
        }
    }

    private fun shootEnemyProjectile(x: Float, y: Float, vx: Float, vy: Float) {
        val list = _projectiles.value.toMutableList()
        list.add(Projectile(x, y, vx, vy, isPlayerOwned = false))
        _projectiles.value = list
    }

    // --- GAME HEALTH & SOUL DEPRECIATION ---
    private fun decreasePlayerHp(amount: Float) {
        val p = _player.value
        if (p.soulShieldActive) {
            _player.value = p.copy(soulShieldActive = false)
            triggerSparks(p.x, p.y, EchoesBlue) // shield break visual
            return
        }

        val nextHp = (p.hp - amount).coerceAtLeast(0f)
        _player.value = p.copy(
            hp = nextHp,
            // Soul/energy builds up slightly on taking damage to balance game combat!
            energy = (p.energy + 8f).coerceAtHeight(p.maxEnergy)
        )

        // Check death
        if (nextHp <= 0f) {
            _gameState.value = GameState.GAME_OVER
        }
    }

    private fun Float.coerceAtHeight(max: Float): Float {
        return if (this > max) max else this
    }

    private fun damageEnemy(index: Int, amount: Float) {
        val list = _enemies.value.toMutableList()
        if (index < 0 || index >= list.size) return
        val enemy = list[index]
        val nextHp = (enemy.hp - amount).coerceAtLeast(0f)

        triggerSparks(enemy.x, enemy.y, VitalityRed)

        if (nextHp <= 0f) {
            // Defeated! Spawn rewards (coins and score)
            val p = _player.value
            _player.value = p.copy(
                currency = p.currency + 10,
                score = p.score + 100,
                memoryFragments = p.memoryFragments + 1,
                // Refills some Soul/energy on defeating enemy!
                energy = (p.energy + 15f).coerceAtHeight(p.maxEnergy)
            )
            // Spawn victory sparks
            repeat(15) {
                _particles.value = _particles.value + Particle(
                    enemy.x, enemy.y,
                    (Math.random() * 8 - 4).toFloat(),
                    (Math.random() * 8 - 4).toFloat(),
                    BlightGold
                )
            }
            list.removeAt(index)
        } else {
            list[index] = enemy.copy(hp = nextHp)
        }
        _enemies.value = list
    }

    // --- TEMPLE RELIC INTERACTION & CHRONICLES ---
    fun onOracleInteract() {
        val p = _player.value
        val r = _relic.value ?: return

        // Proximity check (must be within 100 pixels of the center portal/relic)
        val dx = p.x - r.x
        val dy = p.y - r.y
        if (dx*dx + dy*dy < 100f * 100f) {
            loadOracleRiddle()
            _gameState.value = GameState.ORACLE_CONVERSATION
        }
    }

    private fun loadOracleRiddle() {
        _isOracleLoading.value = true
        _oracleFeedback.value = null
        _oracleSelectedIndex.value = -1
        _oracleChoices.value = emptyList()

        viewModelScope.launch {
            val riddle = GeminiApiClient.fetchOracleRiddle(_player.value.level)
            currentOracleRiddle = riddle
            _oracleQuestion.value = riddle.riddle
            _oracleChoices.value = riddle.choices
            _isOracleLoading.value = false
        }
    }

    fun selectOracleChoice(index: Int) {
        _oracleSelectedIndex.value = index
        _oracleFeedback.value = null
    }

    fun submitSelectedOracleAnswer() {
        val selectedIdx = _oracleSelectedIndex.value
        val riddle = currentOracleRiddle ?: return
        if (selectedIdx == -1) return

        if (selectedIdx == riddle.correctIndex) {
            // Correct choice! Cleanse the temple
            val p = _player.value
            _player.value = p.copy(
                currency = p.currency + 50,
                score = p.score + 500,
                level = p.level + 1,
                forgetfulness = (p.forgetfulness - 15f).coerceAtLeast(0f)
            )
            _oracleFeedback.value = "مبارك الإجابة صحيحة! تم تطهير صدى المعبد واكتسبت ٥٠ غرضاً ذهبياً وارتفع مستواك."
            _relic.value = _relic.value?.copy(isUsed = true)
        } else {
            // Wrong choice! Curse the temple
            val p = _player.value
            _player.value = p.copy(
                forgetfulness = (p.forgetfulness + 20f).coerceAtMost(100f)
            )
            _oracleFeedback.value = "للأسف الإجابة خاطئة! شابت صخور المعبد لعنة النسيان العميقة."
            decreasePlayerHp(15f)
        }
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED || _gameState.value == GameState.ORACLE_CONVERSATION) {
            _gameState.value = GameState.PLAYING
        }
    }

    fun backToMainMenu() {
        _gameState.value = GameState.MENU
    }

    fun openSettings() {
        // Can be triggered from Menu
    }

    fun openChronicles() {
        _gameState.value = GameState.CHRONICLES
    }

    fun closeChronicles() {
        _gameState.value = GameState.MENU
    }
}
