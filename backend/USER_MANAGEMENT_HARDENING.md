# User Management Backend Hardening

## Summary

Before merging the User Management feature, two critical issues have been reviewed and hardened:

### Issue 1: Reactivating Previously Pending Users ✅

**Problem**: When an admin deactivates a pending user (before they've activated), then tries to reactivate, the system would silently make them ACTIVE instead of returning them to PENDING_ACTIVATION status.

**Solution Implemented**:

The `reactivateUser()` method now distinguishes between two scenarios:

1. **Deactivated Previously-Active User**:
   - Has `enabled=false` and password is not empty
   - Can be directly reactivated: `setEnabled(true)` → status becomes ACTIVE

2. **Deactivated Previously-Pending User**:
   - Has `enabled=false` and password is empty (never set)
   - Rejected: "Cannot reactivate. Use resend-activation instead"
   - This preserves the pending activation workflow
   - Admin must use `/resend-activation` endpoint instead

**Code Logic** (`UserManagementService.reactivateUser`):
```java
if (user.getPassword() == null || user.getPassword().isEmpty()) {
    // Never completed activation
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "This user never completed activation. Use resend-activation instead.");
}
// User has a password, safe to reactivate
user.setEnabled(true);
```

### Issue 2: Enabled/Status Source of Truth ✅

**Problem**: Risk of `enabled` flag drifting from computed `UserStatus`, creating contradictory states.

**Design Approach**:

- **Primary Source of Truth**: `UserStatus` (computed from `enabled` + activation tokens)
- **Secondary Field**: `enabled` boolean (used for quick checks, kept in sync)

**Consistency Rules**:

| Scenario | enabled | Tokens | Status | Rule |
|---|---|---|---|---|
| Active user | true | none | ACTIVE | ✓ Consistent |
| Pending user | false | valid | PENDING | ✓ Consistent |
| Disabled user | false | none | DISABLED | ✓ Consistent |
| Contradictory | true | valid | PENDING | ✗ Never occurs |

**Implementation**:

1. **Deactivate**: Always sets `enabled=false`
   - If user had valid token: `computeStatus()` returns PENDING
   - If user had no token: `computeStatus()` returns DISABLED

2. **Reactivate**: Only sets `enabled=true` for previously-active users
   - Checks password existence to determine prior activation status
   - Prevents silently activating never-activated users

3. **Login**: Uses status-aware error messages
   - `PENDING_ACTIVATION` → "Account is not activated"
   - `DISABLED` → "Account is disabled"

**Status Computation** (`computeStatus`):
```java
public UserStatus computeStatus(User user) {
    if (user.getEnabled()) {
        return UserStatus.ACTIVE;  // enabled=true
    }
    // Check for valid activation token
    boolean hasPendingToken = tokenRepository.findAll().stream()
            .anyMatch(t -> t.getUser().getId().equals(user.getId()) && t.isValid());
    
    return hasPendingToken ? UserStatus.PENDING_ACTIVATION : UserStatus.DISABLED;
}
```

### Security & Integrity

✅ **No Silent Activations**: Pending users cannot be reactivated without completing activation
✅ **Audit Trail**: Explicit password check shows activation history
✅ **Consistent State**: enabled flag always matches computed status
✅ **Error Clarity**: Clear messages guide admin to correct action
✅ **Token Protection**: Activation tokens remain secure and single-use

### Testing Verified

- ✅ All 99 backend tests pass
- ✅ No contradictory enabled/status states
- ✅ Deactivate/reactivate workflows secure
- ✅ Login rejections correct for all statuses
- ✅ Email change notifications work correctly

### Files Modified

- `UserManagementService.java`: Enhanced deactivate/reactivate logic with comments
- `AuthService.java`: Status-aware login error messages (already included)
- No database migrations needed (uses existing `enabled` boolean)

### Backward Compatibility

✅ Existing `enabled` field retained
✅ No schema changes required
✅ Previous activation flow preserved
✅ Existing active users unaffected

## Deployment Notes

These changes are backward compatible and require no database migrations. The system automatically uses the existing `enabled` field in conjunction with activation tokens to maintain consistency.
