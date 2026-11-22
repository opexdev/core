# Event Log financial capture

## Sources

- `richTrade`: the authoritative financial execution source, stored once in `opex_rich_trades` with the full Accountant payload and Kafka source position.
- `accounting_transactions`: actual wallet transfers, including trade settlements, fees, referrals and rewards. Financial-action UUIDs link settlements and fees to executions.

For new matching trades, `RichTrade.executionReference` equals `AccountingTransactionEvent.operationId` on both settlement transfers and both fee transfers. Accountant persists this reference on financial actions and preserves it through processing, retries, archival, and HTTP/Kafka Wallet requests. Each movement retains its own `financialActionId` and `walletTransactionRef`. Fee transfers use `type=FEE` and `recordRole=FEE_SETTLEMENT` on `accounting_transactions`; no dedicated fee topic is required. Older actions may have no operation reference.
- `swap`: one complete OTC conversion stored in `opex_swaps`; customer OUT/IN views are derived when read.
- `deposit` and `withdraw_request`: full immutable lifecycle snapshots in `opex_funding_operations`, separate from wallet movements.
- Order events, raw Kafka archival and dead letters retain their audit and operational roles.

Matching `TradeEvent` messages no longer have a separate typed consumer/table writer here. Raw archival can retain their original payloads. Existing legacy database tables are not dropped or backfilled.

## Structure

- `eventlog-core/trade`: complete `RichTradeRecord`, persistence ports, and read-time participant mapping.
- Kafka port: complete `RichTradeEvent` transport contract.
- PostgreSQL port: `RichTradeModel`, repository and idempotent persister. Original matching trade numbers are informational; execution reference and Kafka position identify captures.
- App: listeners and one persisted accounting feed. No generated/mock transactions or external exchange adapters are mixed into this feed.

Maker/taker commissions on a RichTrade are assessed amounts. Fee transfers remain separate facts proving collection. Participant accounting rows are derived when read, not stored as duplicate execution records. Monetary columns use unconstrained PostgreSQL `NUMERIC`.

## Swap/OTC capture

`opex_swaps` preserves the complete Wallet swap payload: `swap:<reservationReference>` operation ID, reservation database ID and reference, quoted rate, quote and expiry times, actual source/destination amounts and assets, both transfer IDs and references, customer/recipient/system parties, all four wallet IDs/types, normal/admin transfer category, quote creator and execution admin, description, completion time and status. Kafka topic/partition/offset identify the source capture. Numeric values use unconstrained `NUMERIC`; timestamps are normalized to microseconds before persistence so identical replays compare consistently.

The unique operation ID and source position prevent duplicate captures. A changed payload under an existing identity is rejected. The operation and its two customer views share `operationId`; each view keeps its own `walletTransactionRef` and links the other via `relatedTransactionId`. Actual transfers remain separate `SWAP_SETTLEMENT` facts from `accounting_transactions`; the operation feed excludes those facts to avoid counting the same conversion twice. Raw feed includes them for reconciliation. Existing stored `SWAP_LEG` records remain readable and duplicate view identities are collapsed.

The current OTC implementation does not perform a separate fee transfer or provide an explicit fee breakdown. Capture preserves the quoted rate and actual amounts; it does not infer a fee or spread from rounding differences. Incomplete historical swap messages remain unresolved audit facts, excluded from operation totals. No backfill or external-system capture is added. Durable publication and broader delivery hardening remain separate work.

## Deposits and withdrawals

`opex_funding_operations` stores one full snapshot per lifecycle event, including operation UUID and database ID, user, status, currency, gross/principal amounts, assessed customer fee, transfer category, funding method, ON_CHAIN/OFF_CHAIN type, acting admin, gateway/network, known source/destination addresses, destination amount/asset, business/payment references, request/finalized wallet transaction IDs, parties and wallet IDs/types, notes, status reason, attachment reference, OTP requirement count, and creation/transition/completion times. Deposit accepted fees and source symbols are retained where provided. Kafka source position and capture time remain in the persisted model. Monetary fields are unconstrained `NUMERIC`; timestamps are normalized to microseconds before inserting and comparing replays.

Each snapshot is immutable. A later status does not overwrite the earlier actor, timestamps or payment references. Identical replays are accepted and conflicting payloads under the same lifecycle identity are rejected. `type + eventId` and Kafka source position identify captures; `operationId` connects status history and actual wallet movements.

These fields have distinct meanings:

| Field | Meaning |
| --- | --- |
| `transferCategory` | Accounting action such as DEPOSIT, DEPOSIT_MANUALLY, WITHDRAW_REQUEST, WITHDRAW_ACCEPT or WITHDRAW_MANUALLY. WITHDRAW_ACCEPT is the wallet settlement performed when the withdrawal is finalized. |
| `transferMethod` | Funding reason/channel such as CARD, SHEBA, IPG, BANK_DEPOSIT, MANUALLY, VOUCHER or REWARD. Reward-funded payment credits remain DEPOSIT. |
| `operationType` | ON_CHAIN or OFF_CHAIN. |
| `status` | Lifecycle state. Admin acceptance is ACCEPTED/pending; finalization is DONE/completed. |
| `applicator` | Acting admin for the captured transition, when one exists. The customer is retained separately in `userId`. |

Normal withdrawals capture REQUESTED, CREATED, ACCEPTED, DONE, CANCELED and REJECTED transitions as they occur. Manual admin withdrawals capture a direct DONE operation with their actual amount, wallet link and admin; they do not invent a prior hold transaction. Invalid deposits remain audit snapshots without a completed credit. Existing payout references are captured from internal records; no external system is queried.

Wallet transaction rows and generic accounting movement messages preserve `operationId`, `transferMethod` and `applicator`. Wallet migration `V19__funding_capture_metadata.sql` adds those fields and persists funding gateway IDs. Fee amounts in lifecycle snapshots describe the fee assessment; no separate fee settlement is invented where the source performs only a gross transfer.

The normal operation feed derives funding views from the latest captured state as of the requested period end: DONE deposits and ACCEPTED/DONE withdrawals. Canceled/rejected withdrawals and invalid deposits stay out of those totals. Deposit movement facts are excluded from operation totals to avoid counting the lifecycle credit twice. The raw feed retains the lifecycle and movement facts for reconciliation.

Full-detail internal endpoints:

- `GET /internal/eventlog/funding/events?from=...&to=...&limit=...` returns full lifecycle snapshots within the range. Like the existing event feed, range reads are capped at 10,000 rows; this is not an unrestricted bulk export endpoint.
- `GET /internal/eventlog/funding/operations/{DEPOSIT|WITHDRAW}/{operationId}` returns the complete captured lifecycle history of one operation.

These endpoints retain detail for later accounting-report projections. Excel layouts, historical USDT valuation enrichment, historical backfill and durable publication are separate work.
