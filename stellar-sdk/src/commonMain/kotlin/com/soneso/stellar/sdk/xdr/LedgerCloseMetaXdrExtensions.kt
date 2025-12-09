package com.soneso.stellar.sdk.xdr

fun LedgerCloseMetaXdr.ledgerSequence(): UInt = when (this) {
    is LedgerCloseMetaXdr.V0 -> value.ledgerHeader.header.ledgerSeq.value
    is LedgerCloseMetaXdr.V1 -> value.ledgerHeader.header.ledgerSeq.value
    is LedgerCloseMetaXdr.V2 -> value.ledgerHeader.header.ledgerSeq.value
}

fun LedgerCloseMetaXdr.ledgerCloseTime(): ULong = when (this) {
    is LedgerCloseMetaXdr.V0 -> value.ledgerHeader.header.scpValue.closeTime.value.value
    is LedgerCloseMetaXdr.V1 -> value.ledgerHeader.header.scpValue.closeTime.value.value
    is LedgerCloseMetaXdr.V2 -> value.ledgerHeader.header.scpValue.closeTime.value.value
}
