# Kharchee Firestore Security Specification

## 1. Data Invariants
1. **Strict Multi-User Isolation**: Every document under `/users/{userId}` and its subcollections (`expenses`, `khata_friends`, `savings_goals`) is strictly owned by `userId` (`request.auth.uid == userId` and `data.userId == request.auth.uid`).
2. **Path Variable Hardening**: All document IDs (`userId`, `expenseId`, `friendId`, `goalId`) must match `^[a-zA-Z0-9_\-]+$` with length `<= 128` on single-document operations (`get`, `create`, `update`, `delete`).
3. **Immutable Audit Fields**: `userId` and `createdAt` cannot be modified after creation (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Server Timestamp Bound**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` values with `<= request.time`.
5. **Query Enforcer**: All `allow list` rules explicitly validate `request.auth.uid == userId && resource.data.userId == request.auth.uid`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `unauthDb.collection("users/alice/expenses").get()` -> Reject (`request.auth != null`).
2. **Cross-User Read**: `bobDb.doc("users/alice/expenses/exp1").get()` -> Reject (`request.auth.uid == userId`).
3. **Cross-User List**: `bobDb.collection("users/alice/expenses").get()` -> Reject.
4. **Spoofed Owner Field on Create**: `aliceDb.doc("users/alice/expenses/exp1").set({ userId: "bob", ... })` -> Reject (`data.userId == request.auth.uid`).
5. **Shadow / Ghost Field Injection**: Adding `isAdmin: true` to an expense or user settings payload -> Reject (`keys().hasOnly(...)`).
6. **Negative or Zero Expense Amount**: `amount: -50` or `amount: 0` -> Reject (`data.amount > 0`).
7. **Denial of Wallet Oversized Title**: `title` of 5,000 characters -> Reject (`data.title.size() <= 120`).
8. **Invalid Category Enum**: `category: "CryptoGambling"` -> Reject.
9. **Invalid PaymentMode Enum**: `paymentMode: "GoldBars"` -> Reject.
10. **Immutable `createdAt` Tampering on Update**: Modifying `createdAt` during an update -> Reject (`incoming().createdAt == existing().createdAt`).
11. **Immutable `userId` Transfer on Update**: Changing `userId` to another user on update -> Reject (`incoming().userId == existing().userId`).
12. **Future Timestamp Spoofing**: Setting `createdAt` or `updatedAt` in the future (`> request.time`) -> Reject (`<= request.time`).
