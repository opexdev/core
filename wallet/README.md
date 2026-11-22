# Wallet transaction history

`POST /v2/transaction` and the admin history endpoint return:

| Field | Meaning |
| --- | --- |
| `id` | Unique UUID of the user's history row; use as the UI row key/reference. |
| `walletTransactionId` | Wallet transfer ID (`user_transaction.tx_id`); sender and recipient rows can share it. |
| `operationId` | Shared business operation reference, linking a trade's settlements and fees. |
| `financialActionId` | Individual Accountant action UUID, when the transfer comes from Accountant. |
| `date` | Actual posting time, shared with the wallet transfer. |

New history rows preserve the UUID and posting time supplied by the wallet domain.
Flyway migration `V18__user_transaction_correlation.sql` adds the operation/action references and an operation lookup index.

History orders operations by their earliest posting time, then earliest transfer ID, in the requested ascending/descending direction. Within each operation, movements appear in posting order with `FEE` rows last. Stable IDs break timestamp ties. Group anchors use all rows for that owner/operation, so currency/category/time filtering does not change the operation's sorting position. Sorting happens in SQL before row-based pagination; a page boundary can still split an operation.

Keep the API order in the frontend and display `id` as the unique row reference; use `operationId` to identify related rows. Displayed dates and saved balance snapshots retain their original posting meaning even though rows are grouped for readability.

Legacy rows retain their UUID and wallet transfer ID. No operation/action references are inferred or backfilled. Rows without an operation reference remain ordered by posting time and transfer ID.
