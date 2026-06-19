# Pending User Deactivation - State Transitions

## Problem Fixed

Previously, deactivating a pending user would NOT invalidate their activation token, so they would remain in `PENDING_ACTIVATION` status rather than transitioning to `DISABLED`. This was confusing and a security issue - the old activation link would still work.

## Solution

Deactivation now **invalidates all unused activation tokens** for pending users, ensuring the state transitions correctly:

### State Transitions

#### 1. Invite User (Create Pending)
```
enabled = false
activation token = VALID (24 hour expiration)
status = PENDING_ACTIVATION
```

#### 2. Deactivate Pending User
```
enabled = false
activation tokens = INVALIDATED (marked as used)
status = DISABLED  ← Changed from PENDING
```
- Old activation link no longer works
- User appears as DISABLED, not PENDING

#### 3. Resend Activation for Disabled Never-Activated User
```
enabled = false
activation token = NEW VALID TOKEN
status = PENDING_ACTIVATION ← Restored
```
- Only works for users who never set a password
- Generates fresh activation token
- Sends new activation email

#### 4. Activate Account (User Sets Password)
```
enabled = true  (set by ActivationService)
activation token = USED (marked used)
status = ACTIVE
```

#### 5. Deactivate Active User
```
enabled = false
status = DISABLED
```

#### 6. Reactivate Previously Active User
```
enabled = true (set by reactivateUser)
status = ACTIVE
```
- Requires user to have a password (was previously active)
- Never-activated users are rejected

## Implementation Details

### Deactivate Logic

```java
// Check current status to determine if we need to invalidate tokens
UserStatus currentStatus = computeStatus(user);

if (currentStatus == UserStatus.PENDING_ACTIVATION) {
    // Invalidate all unused activation tokens for this pending user
    List<AccountActivationToken> pendingTokens = tokenRepository.findAll().stream()
            .filter(t -> t.getUser().getId().equals(userId) && !t.isUsed())
            .toList();
    
    pendingTokens.forEach(t -> {
        t.setUsedAt(System.currentTimeMillis());  // Mark as used
    });
    tokenRepository.saveAll(pendingTokens);
}

// Set enabled=false to prevent login
user.setEnabled(false);
userRepository.save(user);

// Status will now compute as DISABLED (no valid tokens)
```

### Resend Activation Logic

```java
boolean isNeverActivated = user.getPassword() == null || user.getPassword().isEmpty();

// Allow resend for:
if (status == UserStatus.PENDING_ACTIVATION) {
    // Normal pending user
} else if (status == UserStatus.DISABLED && isNeverActivated) {
    // Deactivated-pending user (never set password)
} else {
    // Reject: ACTIVE or DISABLED-already-activated users
    throw new ResponseStatusException(...);
}

// Invalidate old tokens and generate new one
// ...create and send new activation token...
```

## Status Computation (No Changes)

```java
public UserStatus computeStatus(User user) {
    if (user.getEnabled()) {
        return UserStatus.ACTIVE;
    }
    
    // Check if user has a valid (unused and not expired) activation token
    boolean hasPendingToken = tokenRepository.findAll().stream()
            .anyMatch(t -> t.getUser().getId().equals(user.getId()) && t.isValid());
    
    return hasPendingToken ? UserStatus.PENDING_ACTIVATION : UserStatus.DISABLED;
}
```

## State Diagram

```
PENDING_ACTIVATION (invited)
    ├─ User activates account → ACTIVE
    ├─ Admin deactivates → DISABLED (tokens invalidated)
    └─ Token expires → DISABLED

DISABLED (deactivated or invitation expired)
    ├─ Admin resends activation (if never-activated) → PENDING_ACTIVATION
    ├─ Admin reactivates (if previously active) → ACTIVE
    └─ Nothing else (no self-service reactivation)

ACTIVE (logged in at least once)
    ├─ Admin deactivates → DISABLED
    └─ Admin reactivates → ACTIVE
```

## Security Implications

✅ **Invalidated Links**: Deactivating a pending user immediately invalidates their old invitation link
✅ **Clear State**: User cannot be both deactivated and pending
✅ **Resend Intent**: Admin must explicitly choose to resend (not automatic)
✅ **Audit Trail**: Password check distinguishes prior activation status
✅ **No Silent Activation**: Never-activated deactivated users cannot be accidentally reactivated

## Testing

All 99 tests pass with the updated logic:
- Deactivating pending user invalidates tokens
- Status transitions to DISABLED
- Old token cannot activate
- Resend works for disabled never-activated users
- Reactivate still rejects never-activated users
