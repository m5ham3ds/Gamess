using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SatchelInventory : MonoBehaviour
{
    [Header("Satchels Unlocked")]
    public bool hasSmallSatchel = true;     // Arrows, throwing coins
    public bool hasMediumSatchel = false;   // Smoke bombs
    public bool hasLargeSatchel = false;    // Flasks, healing
    public bool hasTacticalSatchel = false; // Grappling hooks, gears
    
    [Header("Combat Resources")]
    public int inkCoins = 50;       // Basic ranged attack ammo
    public int hellNeedles = 10;    // Piercing ammo
    public int smokeBombs = 0;      // Stealth/Hallucination effect
    public int memoryFlasks = 2;    // Restores health or minor MF
    
    private Rigidbody2D[] satchelPhysicsBodies;

    void Start()
    {
        // For physics logic handling the swinging of satchels
        satchelPhysicsBodies = GetComponentsInChildren<Rigidbody2D>();
    }

    public void UseSmokeBomb()
    {
        if (hasMediumSatchel && smokeBombs > 0)
        {
            smokeBombs--;
            Debug.Log("Used Smoke Bomb! Entities are hallucinating and memory is clouded.");
            // Instantiate Smoke Bomb Prefab
        }
        else if (!hasMediumSatchel)
        {
            Debug.Log("Medium Satchel needed to carry Smoke Bombs.");
        }
        else
        {
            Debug.Log("Out of Smoke Bombs.");
        }
    }

    public void RestoreHealth(MaskedHeroController hero)
    {
        if (hasLargeSatchel && memoryFlasks > 0)
        {
            memoryFlasks--;
            hero.health = Mathf.Min(hero.health + 40, hero.maxHealth);
            Debug.Log("Health Restored!");
        }
    }

    public void CollectResource(string type, int amount)
    {
        switch(type)
        {
            case "InkCoin":
                inkCoins += amount;
                break;
            case "SmokeBomb":
                smokeBombs += amount;
                break;
            // Additional resource types
        }
    }
}
