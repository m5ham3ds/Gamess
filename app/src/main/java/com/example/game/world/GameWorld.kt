package com.example.game.world

import com.example.game.enemy.EnemyType

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isBouncy: Boolean = false
)

data class Hazard(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val damage: Float = 15f
)

data class EnemyTemplate(
    val x: Float,
    val y: Float,
    val type: EnemyType,
    val maxHp: Float = 40f
)

data class GameRegion(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val bgHex: Long,
    val width: Float = 1600f,
    val height: Float = 800f,
    val platforms: List<Platform>,
    val hazards: List<Hazard>,
    val enemyTemplates: List<EnemyTemplate>,
    val leftNodeRegionId: String? = null,
    val rightNodeRegionId: String? = null,
    val spawnXLeft: Float = 120f,
    val spawnXRight: Float = 1450f
)

object WorldConfig {
    val REGION_ASH = "REGION_ASH"
    val REGION_RUINS = "REGION_RUINS"
    val REGION_CATHEDRAL = "REGION_CATHEDRAL"
    val REGION_ABYSS = "REGION_ABYSS"

    val regions = mapOf(
        REGION_ASH to GameRegion(
            id = REGION_ASH,
            nameAr = "غابة الرماد المنسية",
            nameEn = "Forgotten Ash Forest",
            bgHex = 0xFF0B0F13,
            width = 1600f,
            height = 800f,
            platforms = listOf(
                Platform(0f, 750f, 1600f, 50f), // Ground
                Platform(250f, 580f, 200f, 30f),
                Platform(550f, 480f, 200f, 30f),
                Platform(850f, 580f, 250f, 30f),
                Platform(1150f, 450f, 200f, 30f)
            ),
            hazards = listOf(
                Hazard(600f, 730f, 150f, 20f, damage = 10f) // Spikes on floor
            ),
            enemyTemplates = listOf(
                EnemyTemplate(350f, 540f, EnemyType.SHADOW_STALKER),
                EnemyTemplate(950f, 540f, EnemyType.NEON_SPIDER),
                EnemyTemplate(650f, 400f, EnemyType.ABYSSAL_ORB)
            ),
            rightNodeRegionId = REGION_RUINS
        ),
        REGION_RUINS to GameRegion(
            id = REGION_RUINS,
            nameAr = "أطلال الحصن المنهار",
            nameEn = "Crumbling Ruins Keep",
            bgHex = 0xFF14131A,
            width = 1600f,
            height = 800f,
            platforms = listOf(
                Platform(0f, 750f, 600f, 50f), // Left floor
                Platform(1000f, 750f, 600f, 50f), // Right floor
                Platform(300f, 550f, 180f, 30f),
                Platform(600f, 420f, 400f, 30f, isBouncy = true), // Bouncy void pad
                Platform(1100f, 550f, 180f, 30f)
            ),
            hazards = listOf(
                Hazard(600f, 740f, 400f, 15f, damage = 20f) // Deep Lava pit in middle
            ),
            enemyTemplates = listOf(
                EnemyTemplate(200f, 710f, EnemyType.NEON_SPIDER),
                EnemyTemplate(1300f, 710f, EnemyType.VOID_DEVOURER),
                EnemyTemplate(800f, 350f, EnemyType.CHRONO_SENTINEL)
            ),
            leftNodeRegionId = REGION_ASH,
            rightNodeRegionId = REGION_CATHEDRAL
        ),
        REGION_CATHEDRAL to GameRegion(
            id = REGION_CATHEDRAL,
            nameAr = "محيط كاتدرائية الصدى الروحي",
            nameEn = "Echo Cathedral Perimeter",
            bgHex = 0xFF0D1418,
            width = 1600f,
            height = 800f,
            platforms = listOf(
                Platform(0f, 750f, 1600f, 50f),
                Platform(200f, 600f, 150f, 30f),
                Platform(450f, 480f, 150f, 30f),
                Platform(700f, 380f, 200f, 30f),
                Platform(1000f, 480f, 150f, 30f),
                Platform(1250f, 600f, 150f, 30f)
            ),
            hazards = listOf(
                Hazard(400f, 730f, 100f, 20f, damage = 12f),
                Hazard(1100f, 730f, 100f, 20f, damage = 12f)
            ),
            enemyTemplates = listOf(
                EnemyTemplate(500f, 440f, EnemyType.SHADOW_STALKER),
                EnemyTemplate(800f, 320f, EnemyType.ABYSSAL_ORB),
                EnemyTemplate(1100f, 440f, EnemyType.CHRONO_SENTINEL)
            ),
            leftNodeRegionId = REGION_RUINS,
            rightNodeRegionId = REGION_ABYSS
        ),
        REGION_ABYSS to GameRegion(
            id = REGION_ABYSS,
            nameAr = "منفذ الهاوية الأبدية",
            nameEn = "The Eternal Abyss Gate",
            bgHex = 0xFF040608,
            width = 1600f,
            height = 800f,
            platforms = listOf(
                Platform(0f, 750f, 400f, 50f),
                Platform(1200f, 750f, 400f, 50f),
                Platform(400f, 620f, 200f, 30f),
                Platform(1000f, 620f, 200f, 30f),
                Platform(650f, 480f, 300f, 30f)
            ),
            hazards = listOf(
                Hazard(400f, 740f, 800f, 15f, damage = 25f) // Full spikes abyss center
            ),
            enemyTemplates = listOf(
                EnemyTemplate(750f, 420f, EnemyType.VOID_DEVOURER),
                EnemyTemplate(500f, 570f, EnemyType.SHADOW_STALKER),
                EnemyTemplate(1050f, 570f, EnemyType.CHRONO_SENTINEL)
            ),
            leftNodeRegionId = REGION_CATHEDRAL
        )
    )
}
