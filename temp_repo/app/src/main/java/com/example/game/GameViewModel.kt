package com.example.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameViewModel : ViewModel() {

    val memoryTree = listOf(
        MemoryLeaf("mem_pulse", "Memory Pulse", "A small blast using old records. Deals AoE damage.", MemoryLeafType.COMBAT, mfCost = 1, fmGain = 1),
        MemoryLeaf("mem_echo", "Echo Recall", "Summon a reflection of the past to fight alongside you temporarily.", MemoryLeafType.COMBAT, mfCost = 2, fmGain = 2, requiredLeafId = "mem_pulse"),
        MemoryLeaf("mem_shard", "Mask Shard Blast", "Fire concentrated fragments from your face. Very powerful.", MemoryLeafType.COMBAT, mfCost = 3, fmGain = 4, requiredLeafId = "mem_echo"),
        MemoryLeaf("mem_stride", "Ghost Stride", "Improve your dash to ignore damage completely and travel further.", MemoryLeafType.MOVEMENT, mfCost = 2, fmGain = 1),
        MemoryLeaf("mem_names", "Borrowed Names", "Become forgotten faster but gain incredible power temporarily.", MemoryLeafType.DEFENSE, mfCost = 2, fmGain = 1)
    )

    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentRegion = MutableStateFlow(GameRegion.ASHEN_SPRAWL)
    val currentRegion: StateFlow<GameRegion> = _currentRegion.asStateFlow()

    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player.asStateFlow()

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())
    val platforms: StateFlow<List<Platform>> = _platforms.asStateFlow()

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies: StateFlow<List<Enemy>> = _enemies.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles: StateFlow<List<Projectile>> = _projectiles.asStateFlow()

    private val _shards = MutableStateFlow<List<CoreShard>>(emptyList())
    val shards: StateFlow<List<CoreShard>> = _shards.asStateFlow()

    private val _relic = MutableStateFlow<OracleRelic?>(null)
    val relic: StateFlow<OracleRelic?> = _relic.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _oraclePrompt = MutableStateFlow("")
    val oraclePrompt: StateFlow<String> = _oraclePrompt.asStateFlow()

    private val _oracleMessage = MutableStateFlow("مرحباً بك أيها الرحالة الصامت. لقد كنت بانتظار وصولك...")
    val oracleMessage: StateFlow<String> = _oracleMessage.asStateFlow()

    private val _isOracleLoading = MutableStateFlow(false)
    val isOracleLoading: StateFlow<Boolean> = _isOracleLoading.asStateFlow()

    private val _oracleRiddle = MutableStateFlow("أنا أسير بلا قدمين، وأدخل إلى القلوب دون استخراج إذن، ولا يعيدني إلا صمت مطبق. من أنا؟")
    val oracleRiddle: StateFlow<String> = _oracleRiddle.asStateFlow()

    private val _oracleFeedback = MutableStateFlow<String?>(null)
    val oracleFeedback: StateFlow<String?> = _oracleFeedback.asStateFlow()

    private var gameLoopJob: Job? = null
    val worldWidth = 3000f
    val worldHeight = 1500f
    private var lastTimeMillis = 0L

    private var slashIndicatorTime = 0L
    private val _isSlashing = MutableStateFlow(false)
    val isSlashing: StateFlow<Boolean> = _isSlashing.asStateFlow()

    var movingLeft = false
    var movingRight = false

    private val fallbackRiddles = listOf(
        "أنا أسير بلا قدمين، وأدخل إلى القلوب دون استخراج إذن، ولا يعيدني إلا صمت مطبق. من أنا؟" to "الصدى",
        "يمشي ويكتب بغير رأس، فإذا قُطع رأسه صار مقتدراً فصيحاً. ما هو؟" to "القلم",
        "له عين واحدة ولكنه لا يرى بها شيئاً. ما هو؟" to "الإبرة",
        "طويل الساقين يطير في الأجواء، مكلل بالثوب الأنيق والتاج، ينام في البراري وتحت السحب. من أنا؟" to "الرحالة",
        "خفيف كالريشة تماماً، لكن حتى أقوى رجل لا يمكنه الإمساك بي لأكثر من دقائق معدودة. ما أنا؟" to "النفس"
    )
    private var riddleIndex = 0

    init {
        setupLevel()
    }

    private fun setupLevel() {
        val level = _player.value.level
        _enemies.value = emptyList()
        _projectiles.value = emptyList()
        _particles.value = emptyList()
        _shards.value = emptyList()

        val newPlatforms = mutableListOf<Platform>()
        newPlatforms.add(Platform(0f, worldHeight - 50f, worldWidth, 50f)) 
        
        var currentX = 200f
        while(currentX < worldWidth - 200f) {
            val y = worldHeight - 150f - (Math.random() * 400).toFloat()
            val w = 150f + (Math.random() * 300).toFloat()
            newPlatforms.add(Platform(currentX, y, w, 40f))
            currentX += w + 80f + (Math.random() * 200).toFloat()
        }
        
        newPlatforms.add(Platform(0f, 0f, 50f, worldHeight))
        newPlatforms.add(Platform(worldWidth - 50f, 0f, 50f, worldHeight))

        _platforms.value = newPlatforms

        _relic.value = OracleRelic(worldWidth - 200f, worldHeight - 150f, isUsed = false)

        val newEnemies = mutableListOf<Enemy>()
        val enemyCount = 6 + (level * 3)
        for (i in 0 until enemyCount) {
            val plt = newPlatforms[1 + (Math.random() * (newPlatforms.size - 3)).toInt()]
            val ex = plt.x + plt.width / 2f
            val ey = plt.y - 80f

            val type = when {
                i % 3 == 0 -> EnemyType.ABYSSAL_ORB
                i % 4 == 0 -> EnemyType.VOID_DEVOURER
                else -> EnemyType.SHADOW_STALKER
            }

            val hp = when (type) {
                EnemyType.SHADOW_STALKER -> 30f + (level * 5f)
                EnemyType.ABYSSAL_ORB -> 25f + (level * 4f)
                EnemyType.VOID_DEVOURER -> 80f + (level * 10f)
            }

            val speed = when (type) {
                EnemyType.SHADOW_STALKER -> 120f + (level * 10f)
                EnemyType.ABYSSAL_ORB -> 60f + (level * 5f)
                EnemyType.VOID_DEVOURER -> 80f + (level * 5f)
            }

            newEnemies.add(
                Enemy(
                    id = UUID.randomUUID().toString(),
                    x = ex,
                    y = ey,
                    spawnX = ex,
                    spawnY = ey,
                    type = type,
                    hp = hp,
                    maxHp = hp,
                    speed = speed,
                    direction = if (Math.random() > 0.5) Direction.LEFT else Direction.RIGHT
                )
            )
        }
        _enemies.value = newEnemies

        triggerMistySpellEffect(100f, worldHeight - 100f, Color(0xFF00E5FF))
    }

    fun startNewGame() {
        val p = _player.value
        _player.value = p.copy(
            x = 100f,
            y = worldHeight - 150f,
            vy = 0f,
            hp = p.maxHp,
            energy = p.maxEnergy,
            lives = 3,
            level = 1,
            score = 0,
            currency = 0
        )
        _gameState.value = GameState.PLAYING
        setupLevel()
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastTimeMillis = System.currentTimeMillis()
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value == GameState.PLAYING) {
                val now = System.currentTimeMillis()
                var dt = (now - lastTimeMillis) / 1000f
                if (dt > 0.05f) dt = 0.05f // Cap dt to avoid huge jumps on lag spikes
                if (dt <= 0f) dt = 0.016f
                lastTimeMillis = now
                updateGamePhysics(dt)
                delay(12) // Slightly less than 16 to try to catch up with exact 60fps rate
            }
        }
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
            stopGameLoop()
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            _gameState.value = GameState.PLAYING
            startGameLoop()
        }
    }

    fun backToMainMenu() {
        stopGameLoop()
        _gameState.value = GameState.MENU
    }

    fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun handleJump() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        if (p.isGrounded) {
            _player.update { it.copy(vy = p.jumpPower, isGrounded = false) }
            for (i in 0..8) {
                spawnParticle(p.x, p.y + p.radius, (Math.random() - 0.5f).toFloat() * 100f, -50f, Color(0xFF00E5FF), 5f, 0.8f, 0.05f)
            }
        }
    }

    fun onSlashAttack() {
        if (_gameState.value != GameState.PLAYING) return
        val now = System.currentTimeMillis()
        _isSlashing.value = true
        slashIndicatorTime = now

        viewModelScope.launch {
            delay(150)
            _isSlashing.value = false
        }

        val p = _player.value
        val attackReach = 100f
        val ax = when (p.direction) {
            Direction.LEFT -> p.x - attackReach
            Direction.RIGHT -> p.x + attackReach
            else -> p.x
        }
        val ay = p.y

        for (i in 0..12) {
            val vx = if (p.direction == Direction.LEFT) -300f else if (p.direction == Direction.RIGHT) 300f else (Math.random() - 0.5f).toFloat() * 100f
            val vy = (Math.random() - 0.5f).toFloat() * 100f
            spawnParticle(ax, ay, vx, vy, Color(0xFF00E5FF), 7f, 1f, 0.08f)
        }

        _enemies.update { list ->
            list.map { enemy ->
                val dist = distance(ax, ay, enemy.x, enemy.y)
                if (dist <= 140f) {
                    triggerHitFlash(enemy.x, enemy.y, Color(0xFFFFB300))
                    enemy.copy(hp = enemy.hp - p.attackDamage)
                } else enemy
            }.filter {
                if (it.hp <= 0f) {
                    addScore(50)
                    triggerExplosion(it.x, it.y, Color(0xFFFF4081))
                    _player.update { pl -> pl.copy(currency = pl.currency + 5, memoryFragments = pl.memoryFragments + (1..3).random()) }
                    false
                } else true
            }
        }

        _player.update {
            it.copy(energy = (it.energy + 10f).coerceAtMost(it.maxEnergy))
        }

        checkVictoryConditions()
    }

    fun onSoulPulse() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        if (p.energy < 40f) {
            triggerHitFlash(p.x, p.y, Color(0xAAFF4081))
            return
        }

        _player.update { it.copy(energy = it.energy - 40f) }

        for (angle in 0..360 step 15) {
            val rad = Math.toRadians(angle.toDouble())
            val vx = (cos(rad) * 400f).toFloat()
            val vy = (sin(rad) * 400f).toFloat()
            spawnParticle(p.x, p.y, vx, vy, Color(0xFF00E5FF), 10f, 1.0f, 0.04f, lifetime = 35)
        }

        _enemies.update { list ->
            list.map { enemy ->
                val d = distance(p.x, p.y, enemy.x, enemy.y)
                if (d < 300f) {
                    triggerHitFlash(enemy.x, enemy.y, Color(0xFF00E5FF))
                    enemy.copy(hp = enemy.hp - 40f)
                } else enemy
            }.filter {
                if (it.hp <= 0f) {
                    addScore(80)
                    triggerExplosion(it.x, it.y, Color(0xFF00B0FF))
                    _player.update { pl -> pl.copy(currency = pl.currency + 10, memoryFragments = pl.memoryFragments + (2..5).random()) }
                    false
                } else true
            }
        }
        checkVictoryConditions()
    }

    fun onDashAction() {
        if (_gameState.value != GameState.PLAYING) return
        val now = System.currentTimeMillis()
        val p = _player.value
        if (now - p.lastDashTime < p.dashCooldown) return

        _player.update {
            val dx = when (it.direction) {
                Direction.LEFT -> -1f
                Direction.RIGHT -> 1f
                else -> if (movingLeft) -1f else 1f
            }

            val dashDistance = 250f
            var nx = it.x + dx * dashDistance
            
            _platforms.value.forEach { plt ->
                if (nx > plt.x && nx < plt.x + plt.width && it.y > plt.y && it.y < plt.y + plt.height) {
                    nx = if (dx > 0) plt.x - it.radius else plt.x + plt.width + it.radius
                }
            }
            
            it.copy(
                x = nx,
                isDashing = true,
                lastDashTime = now,
                dashEndTime = now + 250,
                dashCooldown = 1500
            ) 
        }

        for (i in 0..15) {
            val vx = (Math.random() - 0.5f).toFloat() * 100f
            val vy = (Math.random() - 0.5f).toFloat() * 100f
            spawnParticle(_player.value.x, _player.value.y, vx, vy, Color(0xFFFFFFFD), 6f, 0.9f, 0.05f)
        }
    }

    fun onOracleInteract() {
        if (_gameState.value != GameState.PLAYING) return
        val p = _player.value
        val currentRelic = _relic.value ?: return

        if (currentRelic.isUsed) return

        val dist = distance(p.x, p.y, currentRelic.x, currentRelic.y)
        if (dist <= 150f) {
            _gameState.value = GameState.ORACLE_CONVERSATION
            stopGameLoop()
            fetchAIOrcaleRiddle()
        }
    }

    private fun fetchAIOrcaleRiddle() {
        _isOracleLoading.value = true
        _oracleFeedback.value = null

        val currentFallback = fallbackRiddles[riddleIndex % fallbackRiddles.size]
        _oracleRiddle.value = currentFallback.first

        viewModelScope.launch {
            val prompt = """
                You are the mystical, dark Eldritch Oracle from the ancient ruins of the Obsidian Core. 
                The traveler 'The Silent Wanderer' represents a warrior in a white mask, black coat, and glowing eyes.
                We are at level ${_player.value.level}.
                Formulate a short, rhythmic, mysterious riddle in Arabic (and English translate below it) related to darkness, masks, ancient magic, shadows, or cores. 
                Keep it poetic and captivating. Also wait for their response. 
                Do NOT provide the solution in the riddle! Max 3 sentences.
            """.trimIndent()

            val aiResult = GeminiApiClient.askOracle(prompt, currentFallback.first)
            _oracleRiddle.value = aiResult
            _isOracleLoading.value = false
        }
    }

    fun submitOracleAnswer(answer: String) {
        if (answer.trim().isEmpty()) return
        _isOracleLoading.value = true

        viewModelScope.launch {
            val currentFallback = fallbackRiddles[riddleIndex % fallbackRiddles.size]
            val prompt = """
                As the mysterious Eldorian Temple Oracle, evaluate if the traveler's answer: "$answer" matches or is conceptually correct/smart for this riddle: "${_oracleRiddle.value}".
                If you generated this riddle, you know the answer. If this is a fallback riddle, the answer is: "${currentFallback.second}".
                Respond purely in Arabic, telling him poetically if he is correct or not. 
                IMPORTANT: Start your response with exactly “[ACCEPTED]” if they are correct, or “[DECLINED]” if they are incorrect.
                Example Output style:
                [ACCEPTED] مبارك عليك أيها المرتحل الصامت، لقد أجبت بفراسة الأجداد! قوتك تضاعفت الآن!
            """.trimIndent()

            val responseText = GeminiApiClient.askOracle(prompt, "[ACCEPTED] أحسنت القول، إجابتك ذكية ومبروكة.")
            _isOracleLoading.value = false

            if (responseText.contains("[ACCEPTED]", ignoreCase = true)) {
                riddleIndex++
                _oracleFeedback.value = responseText.replace("[ACCEPTED]", "").trim()
                _player.update {
                    it.copy(
                        hp = it.maxHp,
                        energy = it.maxEnergy,
                        soulShieldActive = true,
                        shieldExpiryTime = System.currentTimeMillis() + 15000L,
                        score = it.score + 150,
                        currency = it.currency + 50
                    )
                }
                _relic.update { it?.copy(isUsed = true) }
            } else {
                _oracleFeedback.value = responseText.replace("[DECLINED]", "").trim()
            }
        }
    }

    private fun updateGamePhysics(dt: Float) {
        val now = System.currentTimeMillis()
        val gravity = 2200f // Tighter, faster fall like Hollow Knight

        val p = _player.value
        
        var nx = p.x
        var ny = p.y
        var nvy = p.vy + gravity * dt
        var isGrounded = false
        var ndir = p.direction

        if (p.isDashing) {
            if (now > p.dashEndTime) {
                _player.update { it.copy(isDashing = false) }
            } else {
                nvy = 0f 
            }
        } else {
            if (movingLeft) {
                nx -= p.speed * dt
                ndir = Direction.LEFT
            } else if (movingRight) {
                nx += p.speed * dt
                ndir = Direction.RIGHT
            }
        }

        ny += nvy * dt

        val r = p.radius
        _platforms.value.forEach { plt ->
            // vertical box coll
            if (nvy > 0 && p.y + r <= plt.y + 10f && ny + r >= plt.y && nx > plt.x && nx < plt.x + plt.width) {
                ny = plt.y - r
                nvy = 0f
                isGrounded = true
            }
            // horizontal box coll
            if (nx + r > plt.x && nx - r < plt.x + plt.width && ny > plt.y && ny - r < plt.y + plt.height) {
                if (nx < plt.x) nx = plt.x - r
                else if (nx > plt.x + plt.width) nx = plt.x + plt.width + r
            }
        }

        if (ny > worldHeight + 200f) {
            handlePlayerDeath()
            return
        }

        val isShieldActive = if (p.soulShieldActive && now > p.shieldExpiryTime) false else p.soulShieldActive

        _player.update { it.copy(x = nx, y = ny, vy = nvy, isGrounded = isGrounded, direction = ndir, soulShieldActive = isShieldActive) }

        val updatedEnemies = _enemies.value.map { enemy ->
            var ex = enemy.x
            var ey = enemy.y
            var evy = pltGravity(enemy, gravity, dt)
            var egnd = false
            var edir = enemy.direction
            
            val aggroRadius = 450f
            val maxTether = 650f
            var newState = enemy.state

            if (newState == EnemyState.IDLE) {
                if (distance(p.x, p.y, ex, ey) < aggroRadius) newState = EnemyState.AGGRO
            } else if (newState == EnemyState.AGGRO) {
                if (distance(p.x, p.y, ex, ey) > maxTether) newState = EnemyState.RETURNING
            } else if (newState == EnemyState.RETURNING) {
                if (distance(enemy.spawnX, enemy.spawnY, ex, ey) < 20f || (ex == enemy.spawnX)) {
                    newState = EnemyState.IDLE
                    ex = enemy.spawnX
                } else if (distance(p.x, p.y, ex, ey) < aggroRadius) {
                    newState = EnemyState.AGGRO
                }
            }

            if (enemy.type != EnemyType.ABYSSAL_ORB) {
                ey += evy * dt
                if (newState == EnemyState.IDLE) {
                    // Stay still
                } else if (newState == EnemyState.AGGRO) {
                    edir = if (p.x < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                } else if (newState == EnemyState.RETURNING) {
                    if (distance(ex, ey, enemy.spawnX, enemy.spawnY) > 5f) {
                        edir = if (enemy.spawnX < ex) Direction.LEFT else Direction.RIGHT
                        ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                    }
                }
            } else {
                evy = 0f
                if (newState == EnemyState.IDLE) {
                    ey += (sin(now / 500.0) * 20.0 * dt).toFloat()
                    if (ey > enemy.spawnY) ey -= enemy.speed * 0.5f * dt
                    if (ey < enemy.spawnY) ey += enemy.speed * 0.5f * dt
                } else if (newState == EnemyState.AGGRO) {
                    edir = if (p.x < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * 0.5f * dt else enemy.speed * 0.5f * dt
                    ey += (sin(now / 500.0) * 50.0 * dt).toFloat()
                } else if (newState == EnemyState.RETURNING) {
                    edir = if (enemy.spawnX < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                    if (ey > enemy.spawnY) ey -= enemy.speed * dt
                    if (ey < enemy.spawnY) ey += enemy.speed * dt
                }
            }

            _platforms.value.forEach { plt ->
                if (evy > 0 && enemy.y + enemy.radius <= plt.y + 10f && ey + enemy.radius >= plt.y && ex > plt.x && ex < plt.x + plt.width) {
                    ey = plt.y - enemy.radius
                    evy = 0f
                    egnd = true
                }
                if (ex - enemy.radius < plt.x && ey > plt.y && ey < plt.y + plt.height) {
                    ex = plt.x + enemy.radius
                }
                if (ex + enemy.radius > plt.x + plt.width && ey > plt.y && ey < plt.y + plt.height) {
                    ex = plt.x + plt.width - enemy.radius
                }
            }

            var nextLastShot = enemy.lastShotTime
            if (newState == EnemyState.AGGRO && enemy.type == EnemyType.ABYSSAL_ORB && now - enemy.lastShotTime > 2000L) {
                shootAbyssalBolt(ex, ey, p.x, p.y)
                nextLastShot = now
            }

            enemy.copy(x = ex, y = ey, direction = edir, state = newState, lastShotTime = nextLastShot)
        }
        _enemies.value = updatedEnemies

        val updatedProjectiles = _projectiles.value.map { proj ->
            proj.copy(x = proj.x + proj.vx * dt, y = proj.y + proj.vy * dt)
        }.filter { proj ->
            proj.x in -50f..worldWidth + 50f && proj.y in -50f..worldHeight + 50f
        }

        val remainingProjectiles = mutableListOf<Projectile>()
        for (proj in updatedProjectiles) {
            var hitOccurred = false
            if (proj.isPlayerOwned) {
                _enemies.update { list ->
                    list.map { enemy ->
                        if (distance(proj.x, proj.y, enemy.x, enemy.y) < enemy.radius) {
                            hitOccurred = true
                            triggerHitFlash(enemy.x, enemy.y, Color(0xFFFFB300))
                            enemy.copy(hp = enemy.hp - proj.damage)
                        } else enemy
                    }.filter {
                        if (it.hp <= 0f) {
                            addScore(50)
                            triggerExplosion(it.x, it.y, Color(0xFFFF4081))
                            _player.update { pl -> pl.copy(currency = pl.currency + 5, memoryFragments = pl.memoryFragments + (1..3).random()) }
                            false
                        } else true
                    }
                }
            } else {
                val distPlayer = distance(proj.x, proj.y, p.x, p.y)
                if (distPlayer < p.radius + proj.radius) {
                    hitOccurred = true
                    damagePlayer(proj.damage)
                }
            }
            if (!hitOccurred) remainingProjectiles.add(proj)
        }
        _projectiles.value = remainingProjectiles

        _enemies.value.forEach { enemy ->
            if (distance(p.x, p.y, enemy.x, enemy.y) < p.radius + enemy.radius) {
                if (!p.isDashing) {
                    damagePlayer(0.8f)
                    if (Math.random() < 0.2) triggerHitFlash(p.x, p.y, Color(0xFFFF4081))
                }
            }
        }

        _player.update { it.copy(energy = (it.energy + 2f * dt).coerceAtMost(it.maxEnergy)) }

        val updatedParticles = _particles.value.map { part ->
            part.copy(
                x = part.x + part.vx * dt,
                y = part.y + part.vy * dt,
                alpha = (part.alpha - part.decay).coerceAtLeast(0f),
                lifetime = part.lifetime - 1
            )
        }.filter { it.alpha > 0f && it.lifetime > 0 }
        _particles.value = updatedParticles
        
        checkVictoryConditions()
    }
    
    private fun pltGravity(enemy: Enemy, gravity: Float, dt: Float): Float {
        return gravity
    }

    private fun shootAbyssalBolt(ex: Float, ey: Float, px: Float, py: Float) {
        val dx = px - ex
        val dy = py - ey
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > 0.1f) {
            val speed = 250f
            val vx = (dx / dist) * speed
            val vy = (dy / dist) * speed
            val newProj = Projectile(x = ex, y = ey, vx = vx, vy = vy, isPlayerOwned = false, radius = 10f, damage = 15f)
            _projectiles.update { it + newProj }
        }
    }

    private fun damagePlayer(amount: Float) {
        val p = _player.value
        if (p.soulShieldActive || p.isDashing) return
        val nextHp = (p.hp - amount).coerceAtLeast(0f)
        _player.update { it.copy(hp = nextHp) }
        if (nextHp <= 0f) handlePlayerDeath()
    }

    private fun handlePlayerDeath() {
        val p = _player.value
        val livesLeft = p.lives - 1
        triggerExplosion(p.x, p.y, Color(0xFFE5003D))

        if (livesLeft >= 0) {
            _player.update {
                it.copy(
                    lives = livesLeft,
                    hp = it.maxHp,
                    energy = it.maxEnergy,
                    x = 100f,
                    y = worldHeight - 150f,
                    vy = 0f
                )
            }
            triggerMistySpellEffect(100f, worldHeight - 150f, Color(0xFF00E5FF))
        } else {
            _gameState.value = GameState.GAME_OVER
            stopGameLoop()
        }
    }

    private fun checkVictoryConditions() {
        if (_enemies.value.isEmpty() && _relic.value?.isUsed == true) {
            val currentLevel = _player.value.level
            if (currentLevel >= 5) {
                _gameState.value = GameState.VICTORY
                stopGameLoop()
            } else {
                _gameState.value = GameState.SHOP 
                stopGameLoop()
            }
        }
    }

    fun proceedToNextLevel() {
        _player.update { it.copy(level = it.level + 1) }
        _gameState.value = GameState.PLAYING
        setupLevel()
        startGameLoop()
    }

    fun openShop() {
        _gameState.value = GameState.SHOP
    }
    
    fun upgrade(type: String) {
        val p = _player.value
        when (type) {
            "hp" -> {
                if (p.currency >= 30) {
                    _player.update { it.copy(maxHp = it.maxHp + 20f, hp = it.maxHp + 20f, currency = it.currency - 30) }
                }
            }
            "damage" -> {
                if (p.currency >= 40) {
                    _player.update { it.copy(attackDamage = it.attackDamage + 10f, currency = it.currency - 40) }
                }
            }
            "energy" -> {
                if (p.currency >= 20) {
                    _player.update { it.copy(maxEnergy = it.maxEnergy + 20f, energy = it.maxEnergy + 20f, currency = it.currency - 20) }
                }
            }
        }
    }

    private fun addScore(amt: Int) {
        _player.update { it.copy(score = it.score + amt) }
    }

    fun spawnParticle(x: Float, y: Float, vx: Float, vy: Float, color: Color, size: Float, alpha: Float, decay: Float, lifetime: Int = 40) {
        val p = Particle(x, y, vx, vy, color, size, alpha, decay, lifetime)
        _particles.update { it + p }
    }

    private fun triggerExplosion(x: Float, y: Float, color: Color) {
        for (i in 0..18) {
            val angle = Math.random() * Math.PI * 2
            val speed = 200f + Math.random() * 200f
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            spawnParticle(x, y, vx, vy, color, 8f, 1.0f, 0.04f, lifetime = 30)
        }
    }

    private fun triggerHitFlash(x: Float, y: Float, color: Color) {
        for (i in 0..6) {
            val vx = (Math.random() - 0.5f).toFloat() * 150f
            val vy = (Math.random() - 0.5f).toFloat() * 150f
            spawnParticle(x, y, vx, vy, color, 6f, 0.8f, 0.06f, lifetime = 15)
        }
    }

    private fun triggerMistySpellEffect(x: Float, y: Float, color: Color) {
        for (i in 0..30) {
            val vx = (Math.random() - 0.5f).toFloat() * 100f
            val vy = (Math.random() - 0.5f).toFloat() * 100f
            spawnParticle(x, y, vx, vy, color, 12f, 0.7f, 0.02f, lifetime = 60)
        }
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    fun openChronicles() {
        _gameState.value = GameState.CHRONICLES
    }

    fun closeChronicles() {
        _gameState.value = GameState.MENU
    }

    fun openMemoryTree() {
        _gameState.value = GameState.MEMORY_TREE
    }

    fun closeMemoryTree() {
        _gameState.value = GameState.MENU
    }

    fun recoverMemory(leafId: String) {
        val leaf = memoryTree.find { it.id == leafId } ?: return
        val currentP = _player.value
        
        if (currentP.memoryFragments >= leaf.mfCost) {
            // Check requirement
            if (leaf.requiredLeafId == null || currentP.unlockedMemories.contains(leaf.requiredLeafId)) {
                _player.value = currentP.copy(
                    memoryFragments = currentP.memoryFragments - leaf.mfCost,
                    forgetfulness = (currentP.forgetfulness + leaf.fmGain).coerceAtMost(100),
                    unlockedMemories = currentP.unlockedMemories + leafId
                )
            }
        }
    }

    fun useMemoryPower() {
        val p = _player.value
        // If "mem_echo" is unlocked, we can spawn a companion maybe? Or just damage enemies.
        // For simplicity, if "mem_pulse" is unlocked, do AoE damage
        val dmg = if (p.unlockedMemories.contains("mem_shard")) 100f
                  else if (p.unlockedMemories.contains("mem_echo")) 60f
                  else if (p.unlockedMemories.contains("mem_pulse")) 30f
                  else 0f
                  
        if (dmg > 0 && p.memoryFragments >= 1) { // Costs 1 MF to use currently equipped power
            _player.value = p.copy(
                memoryFragments = p.memoryFragments - 1,
                forgetfulness = (p.forgetfulness + 2).coerceAtMost(100)
            )
            triggerExplosion(p.x, p.y, Color(0xFFB470E0))
            // Damage enemies
            _enemies.update { list ->
                list.map { enemy ->
                    if (distance(p.x, p.y, enemy.x, enemy.y) < 300f) {
                        triggerHitFlash(enemy.x, enemy.y, Color(0xFFB470E0))
                        enemy.copy(hp = enemy.hp - dmg)
                    } else enemy
                }.filter {
                    if (it.hp <= 0f) {
                        addScore(100)
                        triggerExplosion(it.x, it.y, Color(0xFFB470E0))
                        false
                    } else true
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
