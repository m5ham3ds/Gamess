using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class WorldForgetfulnessManager : MonoBehaviour
{
    public MemorySystem memorySystem;
    
    [Header("Dynamic World Elements")]
    public GameObject[] corruptedMemories; // Dark entities that spawn at high FM
    public NPCController[] friendlyNPCs;   // NPCs that will forget the player
    public GameObject[] memoryPathways;    // Secret paths that disappear if forgotten
    
    private int currentForgetfulnessLevel = 0;

    void Start()
    {
        if (memorySystem == null)
            memorySystem = FindObjectOfType<MemorySystem>();
    }

    void Update()
    {
        if (memorySystem != null && memorySystem.forgetfulnessLevel != currentForgetfulnessLevel)
        {
            currentForgetfulnessLevel = memorySystem.forgetfulnessLevel;
            UpdateWorldState();
        }
    }

    private void UpdateWorldState()
    {
        // Stage 1: Minor forgetfulness (FM >= 10)
        if (currentForgetfulnessLevel >= 10 && currentForgetfulnessLevel < 20)
        {
            ChangeNPCDialoguesToConfused();
        }
        // Stage 2: Severe forgetfulness, world becomes hostile (FM >= 20)
        else if (currentForgetfulnessLevel >= 20)
        {
            SpawnCorruptedMemories();
            HideMemoryPathways();
        }
    }

    private void ChangeNPCDialoguesToConfused()
    {
        foreach (var npc in friendlyNPCs)
        {
            // NPCs lose their memory of the hero, changing the narrative context
            npc.SetState(NPCState.ForgottenHero);
            Debug.Log($"NPC {npc.npcName} says: 'Wait... who are you? Have we met?'");
        }
    }

    private void SpawnCorruptedMemories()
    {
        // The world compensates for missing memory fragments by spawning shadows
        foreach (var entity in corruptedMemories)
        {
            entity.SetActive(true);
        }
    }

    private void HideMemoryPathways()
    {
        // When the world forgets the hero, they lose access to memory-based environments
        foreach (var path in memoryPathways)
        {
            path.SetActive(false);
        }
    }
}

public enum NPCState
{
    Normal,
    ForgottenHero,
    Hostile
}

public class NPCController : MonoBehaviour
{
    public string npcName;
    private NPCState currentState = NPCState.Normal;

    public void SetState(NPCState newState)
    {
        currentState = newState;
    }
}
