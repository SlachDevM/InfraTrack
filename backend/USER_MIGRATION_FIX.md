# User Migration Fix - Enabled Field

## Issue Identified

The initial user migration for the new `enabled` field had a critical flaw:

```sql
UPDATE users SET enabled = TRUE WHERE enabled IS NULL OR enabled = FALSE
```

**Problem**: This query would reactivate users that were **intentionally disabled** (enabled = FALSE), which violates the principle that explicit account disablement should be preserved.

## Solution Implemented

Changed migration to only handle NULL values:

```sql
UPDATE users SET enabled = TRUE WHERE enabled IS NULL
```

**Rationale**:
- **NULL enabled**: Users from before this feature existed. Should default to enabled for backward compatibility
- **FALSE enabled**: Users explicitly disabled by administrators or security measures. Must remain disabled
- **TRUE enabled**: Users already enabled. Unaffected by migration

## Migration Scenarios

### Scenario 1: Existing User (NULL enabled)
- **Before**: `enabled = NULL`
- **After**: `enabled = TRUE` ✅ (migrated by UPDATE statement)
- **Reason**: Backward compatibility - existing users should remain enabled

### Scenario 2: Intentionally Disabled User (FALSE enabled)
- **Before**: `enabled = FALSE`
- **After**: `enabled = FALSE` ✅ (NOT touched by UPDATE statement)
- **Reason**: Security - explicitly disabled accounts remain disabled

### Scenario 3: Already Enabled User (TRUE enabled)
- **Before**: `enabled = TRUE`
- **After**: `enabled = TRUE` ✅ (unaffected)
- **Reason**: No change needed

## Column Default

```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE
```

The `DEFAULT TRUE` handles new users created by JPA without explicit enabled value:
- Ensures new entities default to enabled
- Prevents accidental creation of disabled accounts
- Provides safe fallback for any edge cases

## Test Coverage

Updated `DatabaseSchemaUpdaterTest.java` with 8 tests:

1. ✅ `onApplicationEvent_addsEnabledColumnIfNotExists()` - Column created with DEFAULT TRUE
2. ✅ `onApplicationEvent_migratesOnlyNullEnabledToTrue()` - Only NULL values migrated
3. ✅ `onApplicationEvent_preservesIntentionallyDisabledUsers()` - FALSE values not touched
4. ✅ `onApplicationEvent_addsFcmTokenColumn()` - FCM column added
5. ✅ `onApplicationEvent_addsPhotoColumns()` - Photo columns added
6. ✅ `onApplicationEvent_updateInProgressStatusConstraint()` - Job status constraint updated
7. Additional tests for backward compatibility and data preservation

**Test Results**: BUILD SUCCESS ✅

## Code Changes

**Modified Files**:
- `DatabaseSchemaUpdater.java` - Fixed migration query and added detailed comments
- `DatabaseSchemaUpdaterTest.java` - Enhanced tests to verify all three migration scenarios

**Commits**:
- Commit: `3ba3bde` - "fix: correct user migration to only enable NULL enabled values, not FALSE"
- Pushed to: `origin/main`

## Security Impact

✅ **Preserves Security Decisions**: Administrators' explicit user disablement is now protected from accidental reactivation

✅ **Backward Compatible**: Existing users from before this feature remain enabled

✅ **Future-Proof**: If users are disabled in the future, they won't be accidentally re-enabled by this migration

✅ **Data Integrity**: Only touches rows that need migration (NULL enabled), leaves intentional changes untouched

## Deployment Notes

- This fix is applied automatically on application startup via `DatabaseSchemaUpdater`
- No manual SQL scripts needed
- Safe to deploy multiple times (idempotent due to `ALTER TABLE ... IF NOT EXISTS`)
- No data loss or corruption risks
