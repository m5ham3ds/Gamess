package com.example.game.enemy

/**
 * Categorization of different adversary forces in the game.
 * Adding new enemy classes is as easy as adding a new entry to this enum and configuring its stats.
 */
enum class EnemyType {
    SHADOW_STALKER, // Walks back and forth, aggressive when close
    ABYSSAL_ORB,    // Floating orb that shoots energy projectiles towards player
    VOID_DEVOURER,  // High HP mini-boss that strikes slow but hard
    NEON_SPIDER,    // Quick wall/ground patrolling mechanical scout with yellow shields
    CHRONO_SENTINEL // Flying drone that pulses sonic waves
}
