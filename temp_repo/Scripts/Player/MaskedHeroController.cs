using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[RequireComponent(typeof(Rigidbody2D))]
[RequireComponent(typeof(Animator))]
public class MaskedHeroController : MonoBehaviour
{
    [Header("Movement Stats")]
    public float moveSpeed = 8f;
    public float jumpForce = 14f;
    public float burstLeapForce = 18f;
    public float wallSlideSpeed = 2f;
    
    [Header("Combat & Memory Stats")]
    public int health = 100;
    public int maxHealth = 100;
    
    [Header("References")]
    public Transform groundCheck;
    public float groundCheckRadius = 0.2f;
    public LayerMask groundLayer;
    
    private Rigidbody2D rb;
    private Animator anim;
    private MemorySystem memorySystem;
    private SatchelInventory inventory;
    
    // State variables
    private float moveInput;
    private bool isGrounded;
    private bool isWallSliding;
    private bool isAttacking;

    void Start()
    {
        rb = GetComponent<Rigidbody2D>();
        anim = GetComponent<Animator>();
        memorySystem = FindObjectOfType<MemorySystem>();
        inventory = GetComponent<SatchelInventory>();
    }

    void Update()
    {
        CheckSurroundings();
        HandleInput();
        UpdateAnimations();
    }

    void FixedUpdate()
    {
        if (!isAttacking)
        {
            Move();
        }
    }

    private void CheckSurroundings()
    {
        isGrounded = Physics2D.OverlapCircle(groundCheck.position, groundCheckRadius, groundLayer);
        // Note: Wall check logic would go here for wall jumping/sliding
    }

    private void HandleInput()
    {
        moveInput = Input.GetAxisRaw("Horizontal");

        // Jumping
        if (Input.GetButtonDown("Jump") && isGrounded && !isAttacking)
        {
            Jump();
        }

        // Basic Combat (No MF cost)
        if (Input.GetButtonDown("Fire1") && !isAttacking)
        {
            StartCoroutine(BasicAttackSequence());
        }

        // Special Powers (Consumes Memory Fragments)
        if (Input.GetKeyDown(KeyCode.Q))
        {
            UseMaskShardBlast();
        }

        // Use Item from Satchel
        if (Input.GetKeyDown(KeyCode.E))
        {
            inventory?.UseSmokeBomb();
        }
    }

    private void Move()
    {
        rb.velocity = new Vector2(moveInput * moveSpeed, rb.velocity.y);
        
        // Flip Character visual based on direction
        if (moveInput > 0)
            transform.localScale = new Vector3(1, 1, 1);
        else if (moveInput < 0)
            transform.localScale = new Vector3(-1, 1, 1);
    }

    private void Jump()
    {
        rb.velocity = new Vector2(rb.velocity.x, jumpForce);
        anim.SetTrigger("Jump");
        // Play jump spatial audio here
    }

    private IEnumerator BasicAttackSequence()
    {
        isAttacking = true;
        rb.velocity = Vector2.zero; // Stop moving while attacking
        anim.SetTrigger("Attack");
        
        // Logic for Precision Strike hitbox here
        
        yield return new WaitForSeconds(0.4f); // Animation duration
        isAttacking = false;
    }

    private void UseMaskShardBlast()
    {
        if (memorySystem != null && memorySystem.memoryFragments >= 3)
        {
            // Requires 3 MF, Adds 4 FM
            memorySystem.UsePowerMemory("Mask Shard Blast", 3, 4);
            anim.SetTrigger("CastPowerfulMagic");
            
            // Logic to instantiate visual Shard Blast effect that damages enemies
            Debug.Log("Mask Shard Blast Fired! World Forgetfulness increased...");
        }
    }

    private void UpdateAnimations()
    {
        anim.SetFloat("Speed", Mathf.Abs(moveInput));
        anim.SetBool("IsGrounded", isGrounded);
    }
}
