using UnityEngine;

// Dialogue & Event System Extensions
public class RegionalEventExpansion : MonoBehaviour
{
    public void TriggerGreatSilence()
    {
       // When the "Great Silence" event happens (No MF available)
       Debug.Log("The Great Silence has begun. Zero Memory Fragments can be collected.");
       // Disable powers that cost MF
       // Spawn silence-specific enemies
    }
    
    public void StartRegionalFestival()
    {
        // E.g., Festival of Remembered Names in Ashen Sprawl
        Debug.Log("Festival properties active: Special merchants spawned, NPC dialogue changed.");
    }
}
