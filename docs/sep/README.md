# SEP Implementations

The SDK provides support for Stellar Ecosystem Proposals (SEPs) to enable interoperability with the Stellar ecosystem.

## What are SEPs?

Stellar Ecosystem Proposals (SEPs) are standards that define how services, applications, and organizations interact with the Stellar network. They ensure consistent implementation of common patterns like domain verification, authentication, and asset anchoring.

## Implemented SEPs

| SEP | Title | Documentation |
|-----|-------|---------------|
| SEP-1 | Stellar TOML | [sep-01.md](sep-01.md) |
| SEP-9 | Standard KYC Fields | [sep-09.md](sep-09.md) |
| SEP-10 | Web Authentication | [sep-10.md](sep-10.md) |
| SEP-12 | KYC API | [sep-12.md](sep-12.md) |
| SEP-38 | Anchor RFQ API | [sep-38.md](sep-38.md) |

## Compatibility Matrices

Detailed field-by-field coverage for each SEP implementation:

| SEP | Coverage | Matrix |
|-----|----------|--------|
| SEP-1 | 100% (71/71 fields) | [SEP-0001 Compatibility Matrix](../../compatibility/sep/SEP-0001_COMPATIBILITY_MATRIX.md) |
| SEP-9 | 100% (76/76 fields) | [SEP-0009 Compatibility Matrix](../../compatibility/sep/SEP-0009_COMPATIBILITY_MATRIX.md) |
| SEP-10 | 100% (31/31 features) | [SEP-0010 Compatibility Matrix](../../compatibility/sep/SEP-0010_COMPATIBILITY_MATRIX.md) |
| SEP-12 | 100% (28/28 fields) | [SEP-0012 Compatibility Matrix](../../compatibility/sep/SEP-0012_COMPATIBILITY_MATRIX.md) |
| SEP-38 | 100% (63/63 fields) | [SEP-0038 Compatibility Matrix](../../compatibility/sep/SEP-0038_COMPATIBILITY_MATRIX.md) |

## Future SEP Implementations

Additional SEP implementations will be documented here as they are added to the SDK.

**Planned SEPs**:
- SEP-5: Key Derivation Methods for Stellar Accounts
- SEP-6: Deposit and Withdrawal API
- SEP-24: Hosted Deposit and Withdrawal

---

**Last Updated**: 2025-12-17
