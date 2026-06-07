package com.example.game.enemy

import com.example.game.Platform
import com.example.game.player.Direction
import com.example.game.player.PlayerState
import kotlin.math.*

/**
 * Clean AI and collision physics simulation logic for modular adversaries.
 */
object EnemyAI {

    /**
     * Updates an enemy's state machine, coordinates, platform collisions, and attack timers.
     */
    fun updateEnemy(
        enemy: Enemy,
        player: PlayerState,
        platforms: List<Platform>,
        gravity: Float,
        dt: Float,
        worldWidth: Float,
        worldHeight: Float,
        timeMs: Long,
        onShootBolt: (ex: Float, ey: Float) -> Unit
    ): Enemy {
        if (enemy.aiState == EnemyAIState.DEFEATED) return enemy

        val bp = EnemyConfig.getBlueprint(enemy.type)
        
        // Handle stun states
        if (enemy.isStunned && timeMs < enemy.stunUntil) {
            // Apply simple gravity, but do not process sensory AI walking
            var ey = enemy.y + gravity * dt
            var ex = enemy.x
            
            // Platform land collision
            platforms.forEach { plt ->
                if (enemy.y + enemy.radius <= plt.y + 10f && ey + enemy.radius >= plt.y && ex > plt.x && ex < plt.x + plt.width) {
                    ey = plt.y - enemy.radius
                }
            }
            return enemy.copy(y = ey)
        }

        var ex = enemy.x
        var ey = enemy.y
        var evy = gravity
        var edir = enemy.direction
        var newState = enemy.aiState

        val distToPlayer = distance(player.x, player.y, ex, ey)

        // --- 1. AI Decision Tree State Machine ---
        when (newState) {
            EnemyAIState.IDLE, EnemyAIState.PATROLLING -> {
                if (distToPlayer < bp.aggroRange) {
                    newState = EnemyAIState.AGGRO
                } else if (newState == EnemyAIState.IDLE && Math.random() < 0.02) {
                    newState = EnemyAIState.PATROLLING
                    edir = if (Math.random() > 0.5) Direction.LEFT else Direction.RIGHT
                }
            }
            EnemyAIState.AGGRO -> {
                if (distToPlayer > bp.maxTetherRange) {
                    newState = EnemyAIState.RETURNING
                }
            }
            EnemyAIState.RETURNING -> {
                val distToSpawn = distance(ex, ey, enemy.spawnX, enemy.spawnY)
                if (distToSpawn < 20f || ex == enemy.spawnX) {
                    newState = EnemyAIState.IDLE
                    ex = enemy.spawnX
                    ey = enemy.spawnY
                } else if (distToPlayer < bp.aggroRange) {
                    newState = EnemyAIState.AGGRO
                }
            }
            else -> {
                newState = EnemyAIState.IDLE
            }
        }

        // --- 2. Movement Logic by Enemy Type ---
        if (enemy.type != EnemyType.ABYSSAL_ORB && enemy.type != EnemyType.CHRONO_SENTINEL) {
            // Ground-dwelling physics
            ey += evy * dt

            when (newState) {
                EnemyAIState.PATROLLING -> {
                    // Walk slowly back and forth
                    val walkSpeed = enemy.speed * 0.5f
                    ex += if (edir == Direction.LEFT) -walkSpeed * dt else walkSpeed * dt
                    
                    // Turn around if reaching world boundaries or spawn distance limits
                    if (abs(ex - enemy.spawnX) > 150f || ex < 10f || ex > worldWidth - 10f) {
                        edir = if (edir == Direction.LEFT) Direction.RIGHT else Direction.LEFT
                    }
                }
                EnemyAIState.AGGRO -> {
                    // Direct pursuit of player on X axis
                    edir = if (player.x < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                }
                EnemyAIState.RETURNING -> {
                    // Walk back to spawn spot
                    edir = if (enemy.spawnX < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                }
                else -> {}
            }
        } else {
            // Flying units don't have heavy gravity
            evy = 0f
            val floatCycle = sin(timeMs.toDouble() / 500.0).toFloat()

            when (newState) {
                EnemyAIState.IDLE -> {
                    // Relaxed bobbing in mid-air
                    ey = enemy.spawnY + floatCycle * 20f
                }
                EnemyAIState.PATROLLING -> {
                    ey = enemy.spawnY + floatCycle * 25f
                    val walkSpeed = enemy.speed * 0.4f
                    ex += if (edir == Direction.LEFT) -walkSpeed * dt else walkSpeed * dt
                    if (abs(ex - enemy.spawnX) > 200f) {
                        edir = if (edir == Direction.LEFT) Direction.RIGHT else Direction.LEFT
                    }
                }
                EnemyAIState.AGGRO -> {
                    // Track player positions gently
                    edir = if (player.x < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * 0.5f * dt else enemy.speed * 0.5f * dt
                    
                    // Hover above head level to fire from a superior angle
                    val targetY = player.y - player.radius * 3.5f
                    ey += (targetY - ey) * 1.5f * dt
                }
                EnemyAIState.RETURNING -> {
                    // Fly back to spawn anchor
                    edir = if (enemy.spawnX < ex) Direction.LEFT else Direction.RIGHT
                    ex += if (edir == Direction.LEFT) -enemy.speed * dt else enemy.speed * dt
                    ey += (enemy.spawnY - ey) * dt
                }
                else -> {}
            }
        }

        // --- 3. Platform & Boundary Collisions ---
        platforms.forEach { plt ->
            // Land on top of platforms (only for ground enemies falling down)
            if (evy > 0f && enemy.y + enemy.radius <= plt.y + 10f && ey + enemy.radius >= plt.y && ex > plt.x && ex < plt.x + plt.width) {
                ey = plt.y - enemy.radius
                evy = 0f
            }
            // Lateral platform barrier bumping
            if (ex - enemy.radius < plt.x + plt.width && ex + enemy.radius > plt.x && ey > plt.y && ey < plt.y + plt.height) {
                if (ex > plt.x + plt.width / 2f) {
                    ex = plt.x + plt.width + enemy.radius
                    edir = Direction.RIGHT
                } else {
                    ex = plt.x - enemy.radius
                    edir = Direction.LEFT
                }
            }
        }

        // Clip coordinate boundaries safely
        ex = ex.coerceIn(enemy.radius, worldWidth - enemy.radius)

        // --- 4. Weapon Emitters with Shoot Timers ---
        var nextLastShot = enemy.lastShotTime
        if (newState == EnemyAIState.AGGRO && (enemy.type == EnemyType.ABYSSAL_ORB || enemy.type == EnemyType.CHRONO_SENTINEL)) {
            val cooldown = if (enemy.type == EnemyType.CHRONO_SENTINEL) (bp.shootCooldownMs * 1.25f).toLong() else bp.shootCooldownMs
            if (timeMs - enemy.lastShotTime > cooldown) {
                onShootBolt(ex, ey)
                nextLastShot = timeMs
            }
        }

        return enemy.copy(
            x = ex,
            y = ey,
            direction = edir,
            aiState = newState,
            lastShotTime = nextLastShot,
            isStunned = false
        )
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}
